package ipfs;

import ipfs.message.*;
import org.jgrapht.alg.interfaces.PartitioningAlgorithm;
import org.jgrapht.alg.util.Pair;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.*;

import static ipfs.IPFSUtilities.*;

public class IPFS implements CDProtocol, EDProtocol {
    // peer node ID -> (bytes sent to this peer, bytes received from this peer)
    private final Map<Long, Pair<Long, Long>> debtMap;
    private final Map<String, FileChunk> storage;
    /**
     * Initializes IPFS
     * @param prefix
     */
    public IPFS(String prefix) {
        debtMap = new HashMap<>();
        storage = new HashMap();
    }

    /**
     * Triggered by the cycle-driven
     * @param node
     *          the node on which this component is run
     * @param protocolID
     *          the id of this protocol in the protocol array
     */
    @Override
    public void nextCycle(Node node, int protocolID) {
        Node dest;
        IPFSMessage message;
        MessageType type = getRandomRequestType();
        if (type == MessageType.ADD) {
            dest = getRandomNode();
            Pair<Long, Long> debt = getDebtInfo(dest.getID());
            message = new AddFileMessage(node, new FileChunk(), debt.getFirst(), debt.getSecond());
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else if (type == MessageType.DELETE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Pair<Long, Long> debt = getDebtInfo(dest.getID());
            message = new DeleteFileMessage(node, chunkId, debt.getFirst(), debt.getSecond());
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else if (type == MessageType.RETRIEVE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Pair<Long, Long> debt = getDebtInfo(dest.getID());
            message = new RetrieveFileMessage(node, chunkId, debt.getFirst(), debt.getSecond());
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else if (type == MessageType.UPDATE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Pair<Long, Long> debt = getDebtInfo(dest.getID());
            message = new UpdateFileMessage(node, chunkId, new FileChunk(), debt.getFirst(), debt.getSecond());
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        IPFSMessage message = (IPFSMessage) event;
        Long byteSent = debtMap.get(message.getSender().getID()).getFirst();
        Long byteRecv = debtMap.get(message.getSender().getID()).getSecond();

        boolean bitSwap = validDebt(byteSent, byteRecv);

        if (message instanceof AddFileMessage) {
            // Check debt ratio
            if (! debtMap.containsKey(message.getSender().getID())) {
                debtMap.put(message.getSender().getID(), new Pair<>(0L, 0L));
            }

            if (bitSwap) {
                // Add file to local and global content addressing table
                FileChunk fileChunk = ((AddFileMessage) message).getChunkToSave();
                storage.put(fileChunk.getId(), fileChunk);
                // TODO: decentralized IO will not reflect in global content addressing immediately;
                //  To add content addressing table updating mechanism
                globalContentAddressingTable.put(fileChunk.getId(), node);
                debtMap.get(message.getSender()).setSecond(byteSent + 1);

                // Send response message
                EDSimulator.add(getLatency(node, message.getSender()), new AddFileResponse(node, MessageType.OPERATION_COMPLETED), message.getSender(), pid);
            } else {
                EDSimulator.add(getLatency(node, message.getSender()), new AddFileResponse(node, MessageType.OPERATION_REJECTED), message.getSender(), pid);
            }

        } else if (message instanceof DeleteFileMessage) {
            DeleteFileMessage deleteFileMessage = (DeleteFileMessage) message;
            String fileChunkId = deleteFileMessage.getChunkId();

            // Delete in both local storage and global content addressing table
            storage.remove(fileChunkId);
            globalContentAddressingTable.remove(fileChunkId);

            // Send response message
            EDSimulator.add(getLatency(node, message.getSender()), new DeleteFileResponse(node, MessageType.OPERATION_COMPLETED), message.getSender(), pid);

        } else if (message instanceof RetrieveFileMessage) {

        } else if (message instanceof UpdateFileMessage) {
            System.out.println("Processing updating");
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object clone() {
        Object newProtocol;
        try{
            newProtocol = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return newProtocol;
    }

    private Pair<Long, Long> getDebtInfo(Long nodeId) {
        if (debtMap.containsKey(nodeId)) {
            return debtMap.get(nodeId);
        } else {
            Pair<Long, Long> emptyPair = new Pair<>(0L, 0L);
            debtMap.put(nodeId, emptyPair);
            return emptyPair;
        }
    }
}

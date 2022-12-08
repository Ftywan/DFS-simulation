package ipfs;

import ipfs.message.*;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.HashMap;
import java.util.Map;

import static ipfs.IPFSUtilities.*;

public class IPFS implements CDProtocol, EDProtocol {
    private static final String PARAM_DROP = "drop";
    private final double dropRate;
    private Map<String, FileChunk> storage;
    private Map<Long, Ledger> debtMap; // peer node ID -> (bytes sent to this peer, bytes received from this peer)

    public IPFS(String prefix) {
        debtMap = new HashMap<>();
        storage = new HashMap();
        dropRate = Configuration.getDouble(prefix + "." + PARAM_DROP);
    }

    /**
     * Triggered by the cycle-driven
     *
     * @param node       the node on which this component is run
     * @param protocolID the id of this protocol in the protocol array
     */
    @Override
    public void nextCycle(Node node, int protocolID) {
        Node dest;
        IPFSMessage message;
        MessageType type = getRandomRequestType();

        if (type == MessageType.ADD) {
            dest = getRandomNode();
            Ledger debt = getDebtInfo(dest.getID());
            message = new AddFileMessage(node, new FileChunk(), debt.getByteSent(), debt.getByteRecv());

        } else if (type == MessageType.DELETE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Ledger debt = getDebtInfo(dest.getID());
            message = new DeleteFileMessage(node, chunkId, debt.getByteSent(), debt.getByteRecv());

        } else if (type == MessageType.RETRIEVE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Ledger debt = getDebtInfo(dest.getID());
            message = new RetrieveFileMessage(node, chunkId, debt.getByteSent(), debt.getByteRecv());
        } else {
            throw new UnsupportedOperationException();
        }

        // random drop
        if (!dropped(node, dest, dropRate)) {
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);
            globalRequestStatus.put(message, MessageStatus.FLYING);
        } else {
            globalRequestStatus.put(message, MessageStatus.DROPPED);
        }

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        IPFSMessage message = (IPFSMessage) event;

        // Handle file operation requests
        if (fileOperations.contains(message.getType())) {
            Ledger ledger = message.getLedger();
            boolean bitSwap = validDebt(ledger);
            IPFSMessage resp;

            if (!debtMap.containsKey(message.getSender().getID())) {
                debtMap.put(message.getSender().getID(), new Ledger(0, 0));
            }

            if (message instanceof AddFileMessage) {
                // Check debt ratio
                if (bitSwap) {
                    // Add file to local and global content addressing table
                    FileChunk fileChunk = ((AddFileMessage) message).getChunkToSave();
                    storage.put(fileChunk.getId(), fileChunk);
                    // TODO:    decentralized IO will not reflect in global content addressing immediately;
                    //          To add content addressing table updating mechanism
                    globalContentAddressingTable.put(fileChunk.getId(), node);
                    debtMap.get(message.getSender().getID()).incrementByteRecv(1);

                    resp = new AddFileResponse(node, MessageType.OPERATION_COMPLETED, message);
                } else {
                    resp = new AddFileResponse(node, MessageType.OPERATION_REJECTED, message);
                }

            } else if (message instanceof DeleteFileMessage) {
                if (bitSwap) {
                    DeleteFileMessage deleteFileMessage = (DeleteFileMessage) message;
                    String fileChunkId = deleteFileMessage.getChunkId();
                    // Delete in both local storage and global content addressing table
                    storage.remove(fileChunkId);
                    globalContentAddressingTable.remove(fileChunkId);

                    resp = new DeleteFileResponse(node, MessageType.OPERATION_COMPLETED, message);
                } else {
                    resp = new DeleteFileResponse(node, MessageType.OPERATION_REJECTED, message);
                }


            } else if (message instanceof RetrieveFileMessage) {
                if (bitSwap) {
                    RetrieveFileMessage retrieveFileMessage = (RetrieveFileMessage) message;
                    String chunkId = retrieveFileMessage.getChunkId();

                    FileChunk chunk = storage.get(chunkId);
                    debtMap.get(message.getSender().getID()).incrementByteSent(1);
                    resp = new RetrieveFileResponse(node, MessageType.OPERATION_COMPLETED, chunk, message);
                } else {
                    resp = new RetrieveFileResponse(node, MessageType.OPERATION_REJECTED, null, message);
                }
            } else {
                throw new UnsupportedOperationException();
            }

            // Simulating dropping
            // Send response back to the file operation requester
            if (!dropped(node, message.getSender(), dropRate)) {
                EDSimulator.add(getLatency(node, message.getSender()), resp, message.getSender(), pid);
            } else {
                assert (globalRequestStatus.containsKey(message));
                globalRequestStatus.put(message, MessageStatus.DROPPED);
            }
        }

        // Handle file operation responses
        else if (message instanceof IPFSResponse) {
            globalRequestStatus.put(((IPFSResponse) message).getRequestMessage(), message.getType() == MessageType.OPERATION_COMPLETED ? MessageStatus.SUCCESS : MessageStatus.REJECTED);

            if (message instanceof AddFileResponse) {
                debtMap.get(message.getSender().getID()).incrementByteSent(1);
            } else if (message instanceof DeleteFileResponse) {
                // No changes in debt ledger for deletion operations
            } else if (message instanceof RetrieveFileResponse) {
                debtMap.get(message.getSender().getID()).incrementByteRecv(1);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public Object clone() {
        Object newProtocol;
        try {
            newProtocol = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        ((IPFS) newProtocol).storage = new HashMap<>();
        ((IPFS) newProtocol).debtMap = new HashMap<>();

        return newProtocol;
    }

    // Utils============================================================================================================
    private Ledger getDebtInfo(Long nodeId) {
        if (!debtMap.containsKey(nodeId)) {
            debtMap.put(nodeId, new Ledger(0, 0));
        }

        return debtMap.get(nodeId);
    }

    public void addToStorage(FileChunk fileChunk) {
        this.storage.put(fileChunk.getId(), fileChunk);
    }
}

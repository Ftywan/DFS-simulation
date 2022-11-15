package ipfs;

import ipfs.message.*;
import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.HashMap;
import java.util.Map;

import static ipfs.IPFSUtilities.*;

public class IPFS implements CDProtocol, EDProtocol {
    // TODO: implement interface method for initializer
    final Map<String, FileChunk> storage;
    private final Map<Long, Ledger> debtMap; // peer node ID -> (bytes sent to this peer, bytes received from this peer)

    /**
     * Initializes IPFS
     *
     * @param prefix
     */
    public IPFS(String prefix) {
        debtMap = new HashMap<>();
        storage = new HashMap();
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
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else if (type == MessageType.DELETE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Ledger debt = getDebtInfo(dest.getID());
            message = new DeleteFileMessage(node, chunkId, debt.getByteSent(), debt.getByteRecv());
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else if (type == MessageType.RETRIEVE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Ledger debt = getDebtInfo(dest.getID());
            message = new RetrieveFileMessage(node, chunkId, debt.getByteSent(), debt.getByteRecv());
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else if (type == MessageType.UPDATE) {
            String chunkId = getRandomFileIdInSystem();
            dest = globalContentAddressingTable.get(chunkId);
            Ledger debt = getDebtInfo(dest.getID());
            message = new UpdateFileMessage(node, chunkId, new FileChunk(), debt.getByteSent(), debt.getByteRecv());
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);

        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        IPFSMessage message = (IPFSMessage) event;

        // Handle file operation requests
        if (fileOperations.contains(message.getType())) {
            Ledger ledger = message.getLedger();
            boolean bitSwap = validDebt(ledger.getByteSent(), ledger.getByteRecv());
            IPFSMessage resp;

            if (message instanceof AddFileMessage) {
                // Check debt ratio
                if (bitSwap) {
                    // Add file to local and global content addressing table
                    FileChunk fileChunk = ((AddFileMessage) message).getChunkToSave();
                    storage.put(fileChunk.getId(), fileChunk);
                    // TODO: decentralized IO will not reflect in global content addressing immediately;
                    //  To add content addressing table updating mechanism
                    globalContentAddressingTable.put(fileChunk.getId(), node);
                    debtMap.get(message.getSender().getID()).incrementByteRecv(1);

                    resp = new AddFileResponse(node, MessageType.OPERATION_COMPLETED);
                } else {
                    resp = new AddFileResponse(node, MessageType.OPERATION_REJECTED);
                }

            } else if (message instanceof DeleteFileMessage) {
                if (bitSwap) {
                    DeleteFileMessage deleteFileMessage = (DeleteFileMessage) message;
                    String fileChunkId = deleteFileMessage.getChunkId();
                    // Delete in both local storage and global content addressing table
                    storage.remove(fileChunkId);
                    globalContentAddressingTable.remove(fileChunkId);

                    resp = new DeleteFileResponse(node, MessageType.OPERATION_COMPLETED);
                } else {
                    resp = new DeleteFileResponse(node, MessageType.OPERATION_REJECTED);
                }


            } else if (message instanceof RetrieveFileMessage) {
                if (bitSwap) {
                    RetrieveFileMessage retrieveFileMessage = (RetrieveFileMessage) message;
                    String chunkId = retrieveFileMessage.getChunkId();

                    FileChunk chunk = storage.get(chunkId);
                    debtMap.get(message.getSender().getID()).incrementByteSent(1);
                    resp = new RetrieveFileResponse(message.getSender(), MessageType.OPERATION_COMPLETED, chunk);
                } else {
                    resp = new RetrieveFileResponse(message.getSender(), MessageType.OPERATION_REJECTED, null);
                }

            } else if (message instanceof UpdateFileMessage) {
                System.out.println("Processing updating");
                if (bitSwap) {
                    UpdateFileMessage updateFileMessage = (UpdateFileMessage) message;
                    String chunkId = updateFileMessage.getChunkId();
                    FileChunk newContent = updateFileMessage.getChunkToUpdate();

                    storage.put(chunkId, newContent);
                    globalContentAddressingTable.put(newContent.getId(), node);
                    debtMap.get(message.getSender().getID()).incrementByteRecv(1);

                    resp = new UpdateFileResponse(message.getSender(), MessageType.OPERATION_COMPLETED);
                } else {
                    resp = new UpdateFileResponse(message.getSender(), MessageType.OPERATION_REJECTED);
                }
            } else {
                throw new UnsupportedOperationException();
            }

            // Send response back to the file operation requester
            EDSimulator.add(getLatency(node, message.getSender()), resp, message.getSender(), pid);
        }

        // Handle file operation responses
        else {
            if (message instanceof AddFileResponse) {
                debtMap.get(message.getSender().getID()).incrementByteSent(1);
            } else if (message instanceof DeleteFileResponse) {
                // No changes in debt ledger for deletion operations
            } else if (message instanceof RetrieveFileResponse) {
                debtMap.get(message.getSender().getID()).incrementByteRecv(1);
            } else if (message instanceof UpdateFileMessage) {
                debtMap.get(message.getSender().getID()).incrementByteSent(1);
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

        return newProtocol;
    }

    private Ledger getDebtInfo(Long nodeId) {
        if (debtMap.containsKey(nodeId)) {
            return debtMap.get(nodeId);
        } else {
            Ledger emptyDebt = new Ledger(0, 0);
            debtMap.put(nodeId, emptyDebt);
            return emptyDebt;
        }
    }
}

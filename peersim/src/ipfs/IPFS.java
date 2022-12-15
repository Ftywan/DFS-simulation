package ipfs;

import ipfs.message.*;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.*;

import static ipfs.IPFSUtilities.*;

public class IPFS implements CDProtocol, EDProtocol {
    private static final String PARAM_DROP = "drop";
    private static final String PARAM_SINGLE_REQUEST_PERCENTAGE = "singleRequestPercentage";
    private static final String PARAM_ADD_PERCENTAGE = "addPercentage";
    private static final String PARAM_DELETE_PERCENTAGE = "deletePercentage";
    private final double singleOperationPercentage;
    private final double addPercentage;
    private final double deletePercentage;
    private final double dropRate;
    private final double DHTFactor = 0.879;
    private Map<String, FileChunk> storage;
    private Map<Long, Ledger> debtMap; // peer node ID -> Ledger(bytes sent to this peer, bytes received from this peer)

    public IPFS(String prefix) {
        debtMap = new HashMap<>();
        storage = new HashMap<>();
        dropRate = Configuration.getDouble(prefix + "." + PARAM_DROP);
        singleOperationPercentage = Configuration.getDouble(prefix + "." + PARAM_SINGLE_REQUEST_PERCENTAGE);
        addPercentage = Configuration.getDouble(prefix + "." + PARAM_ADD_PERCENTAGE);
        deletePercentage = Configuration.getDouble(prefix + "." + PARAM_DELETE_PERCENTAGE);
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
        MessageType type = getRandomRequestType(addPercentage, deletePercentage);
        int numOfRequest = getRandomRequestNumber(singleOperationPercentage);

        if (type == MessageType.ADD) {
            dest = getRandomNode();
            Ledger debt = getDebtInfo(dest.getID());
            message = new AddFileMessage(node, numOfRequest, debt);

        } else if (type == MessageType.DELETE) {
            dest = getRandomNode();
            List<String> chunkIds = getRandomNFileIdsInSameNode(dest, numOfRequest, protocolID);
            Ledger debt = getDebtInfo(dest.getID());
            message = new DeleteFileMessage(node, chunkIds, debt);

        } else if (type == MessageType.RETRIEVE) {
            dest = getRandomNode();
            List<String> chunkIds = getRandomNFileIdsInSameNode(dest, numOfRequest, protocolID);
            Ledger debt = getDebtInfo(dest.getID());
            message = new RetrieveFileMessage(node, chunkIds, debt);
        } else {
            throw new UnsupportedOperationException();
        }

        // random drop
        if (!dropped(node, dest, dropRate)) {
            EDSimulator.add(getLatency(node, dest), message, dest, protocolID);
            globalRequestStatus.put(message, MessageStatus.FLYING);
            startTimestamp.put(message, CommonState.getTime());
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
                    List<FileChunk> fileChunks = ((AddFileMessage) message).getChunksToSave();
                    for (FileChunk fileChunk : fileChunks) {
                        storage.put(fileChunk.getId(), fileChunk);
                        // TODO:    decentralized IO will not reflect in global content addressing immediately;
                        //          To add content addressing table updating mechanism
                        globalContentAddressingTable.put(fileChunk.getId(), node);
                        debtMap.get(message.getSender().getID()).incrementByteRecv(1);
                    }
                    resp = new AddFileResponse(node, MessageType.OPERATION_COMPLETED, message);
                } else {
                    resp = new AddFileResponse(node, MessageType.OPERATION_REJECTED, message);
                }

            } else if (message instanceof DeleteFileMessage) {
                if (bitSwap) {
                    DeleteFileMessage deleteFileMessage = (DeleteFileMessage) message;
                    List<String> fileChunkIds = ((DeleteFileMessage) message).getChunkIds();
                    for (String fileChunkId : fileChunkIds) {
                        // Delete in both local storage and global content addressing table
                        storage.remove(fileChunkId);
                        globalContentAddressingTable.remove(fileChunkId);
                    }
                    resp = new DeleteFileResponse(node, MessageType.OPERATION_COMPLETED, message);
                } else {
                    resp = new DeleteFileResponse(node, MessageType.OPERATION_REJECTED, message);
                }

            } else if (message instanceof RetrieveFileMessage) {
                if (bitSwap) {
                    List<String> fileChunkIds = ((RetrieveFileMessage) message).getChunkIds();
                    List<FileChunk> results = new ArrayList<>();
                    for (String chunkId : fileChunkIds) {
                        FileChunk chunk = storage.get(chunkId);
                        results.add(chunk);
                        debtMap.get(message.getSender().getID()).incrementByteSent(1);
                    }
                    resp = new RetrieveFileResponse(node, MessageType.OPERATION_COMPLETED, results, message);
                } else {
                    resp = new RetrieveFileResponse(node, MessageType.OPERATION_REJECTED, null, message);
                }
            } else {
                throw new UnsupportedOperationException();
            }

            // Simulating dropping
            // Send response back to the file operation requester
            if (!dropped(node, message.getSender(), dropRate)) {
                long latency = getLatency(node, message.getSender());
                if (!(message instanceof RetrieveFileMessage)) {
                    latency = Math.round(latency / (1 - DHTFactor));
                }
                EDSimulator.add(latency, resp, message.getSender(), pid);
            } else {
                assert (globalRequestStatus.containsKey(message));
                globalRequestStatus.put(message, MessageStatus.DROPPED);
            }
        }

        // Handle file operation responses
        else if (message instanceof IPFSResponse) {
            globalRequestStatus.put(((IPFSResponse) message).getRequestMessage(), message.getType() == MessageType.OPERATION_COMPLETED ? MessageStatus.SUCCESS : MessageStatus.REJECTED);
            if (message.getType() == MessageType.OPERATION_COMPLETED) {
                endTimestamp.put(((IPFSResponse) message).getRequestMessage(), CommonState.getTime());
            }
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

    public List<String> getNFileIds(int n) {
        List<String> ids = new ArrayList<>(storage.keySet());
        n = Math.min(n, ids.size() - 1);
        n = Math.max(0, n);
        Collections.shuffle(ids);
        return ids.subList(0, n);
    }
}

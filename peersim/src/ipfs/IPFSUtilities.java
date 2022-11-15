package ipfs;

import ipfs.message.MessageType;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import peersim.config.Configuration;
import peersim.core.*;
import peersim.transport.Transport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IPFSUtilities implements Control {
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_LINKABLE = "linkable";

    private final Graph<Node, DefaultWeightedEdge> abstractGraph;
    public static Map<String, Node> globalContentAddressingTable = new HashMap<>();
    public static Set<Long> deadNode = new HashSet<>();
    public static Set<MessageType> fileOperations = new HashSet<>();
    static {
        fileOperations.add(MessageType.ADD);
        fileOperations.add(MessageType.DELETE);
        fileOperations.add(MessageType.RETRIEVE);
        fileOperations.add(MessageType.UPDATE);
    }

    private final int transport;
    private final int linkable;

    private static DijkstraShortestPath<Node, DefaultWeightedEdge> shortestPaths;

    public IPFSUtilities(String prefix) {
        transport = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        linkable = Configuration.getPid(prefix + "." + PAR_LINKABLE);
        abstractGraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public boolean execute() {
        // Construct the abstract network graph
        for (int i = 0; i < Network.size(); i++) {
            Node node = Network.get(i);
            abstractGraph.addVertex(node);
        }

        // Edges can only be added after vertices are added
        for (int i = 0; i < Network.size(); i ++) {
            Node node = Network.get(i);
            Linkable linkable = (Linkable) node.getProtocol(this.linkable);
            for (int j = 0; j < linkable.degree(); j++) {
                Transport transport = (Transport) node.getProtocol(this.transport);
                Node neighbor = linkable.getNeighbor(j);
                abstractGraph.addEdge(node, neighbor, new DefaultWeightedEdge());
                abstractGraph.setEdgeWeight(node, neighbor, transport.getLatency(node, neighbor));
            }
        }

        shortestPaths = new DijkstraShortestPath<>(abstractGraph);

        return false;
    }

    public GraphPath<Node, DefaultWeightedEdge> getShortestPath(Node from, Node to) {
        return shortestPaths.getPaths(from).getPath(to);
    }

    public static long getLatency(Node from, Node to) {
        System.out.println(shortestPaths.getPathWeight(from, to));
        return (long) shortestPaths.getPathWeight(from, to);
    }

    public static Node getRandomNode() {
        int nodeId = CommonState.r.nextInt(Network.size());
        return Network.get(nodeId);
    }

    public static String getRandomFileIdInSystem() {
        String[] fileIds = globalContentAddressingTable.keySet().toArray(new String[0]);
        return fileIds[CommonState.r.nextInt(globalContentAddressingTable.size())];
    }

//    public static IPFSMessage getRandomOperation(Node sender, Node target, Pair<Long, Long> debt) {
//        int i = CommonState.r.nextInt(fileOperations.size());
//        MessageType operation = fileOperations.toArray(new MessageType[0])[i];
//        IPFSMessage message;
//        switch(operation) {
//            case ADD:
//                FileChunk newFileChunk = new FileChunk();
//                message = new AddFileMessage(sender, newFileChunk, debt.getFirst(), debt.getSecond());
//                break;
//            case DELETE:
//                String[] deleteIds = globalContentAddressingTable.keySet().toArray(new String[0]);
//                String deleteId = deleteIds[CommonState.r.nextInt(globalContentAddressingTable.size())];
//                message = new DeleteFileMessage(sender, deleteId, debt.getFirst(), debt.getSecond());
//                break;
//            case RETRIEVE:
//                String[] retrieveIds = globalContentAddressingTable.keySet().toArray(new String[0]);
//                String retrieveId = retrieveIds[CommonState.r.nextInt(globalContentAddressingTable.size())];
//                message = new RetrieveFileMessage(sender, retrieveId);
//                break;
//            default:
//                String[] updateIds = globalContentAddressingTable.keySet().toArray(new String[0]);
//                String updateId = updateIds[CommonState.r.nextInt(globalContentAddressingTable.size())];
//                FileChunk updatedFileChunk = new FileChunk();
//                message = new UpdateFileMessage(sender, updateId, updatedFileChunk);
//                break;
//        }
//        return message;
//    }

    public static MessageType getRandomRequestType() {
        int i = CommonState.r.nextInt(fileOperations.size());
        MessageType operation = fileOperations.toArray(new MessageType[0])[i];
        return operation;
    }
    public static boolean validDebt(Long byteSent, Long byteReceived) {
        double debtRatio = byteSent / (byteReceived + 1);
        double probability = 1 - (1 / (1 + Math.exp(6 - 3 * debtRatio)));

        return CommonState.r.nextDouble() <= probability;
    }
}

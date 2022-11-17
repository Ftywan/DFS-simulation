package ipfs;

import ipfs.message.IPFSMessage;
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
    public static Map<String, Node> globalContentAddressingTable = new HashMap<>();
    public static Set<Long> deadNode = new HashSet<>();
    public static Set<MessageType> fileOperations = new HashSet<>();
    public static HashMap<IPFSMessage, MessageStatus> globalRequestStatus;
    private static DijkstraShortestPath<Node, DefaultWeightedEdge> shortestPaths;

    static {
        fileOperations.add(MessageType.ADD);
        fileOperations.add(MessageType.DELETE);
        fileOperations.add(MessageType.RETRIEVE);
//        fileOperations.add(MessageType.UPDATE);
    }

    private final Graph<Node, DefaultWeightedEdge> abstractGraph;
    private final int transport;
    private final int linkable;

    public IPFSUtilities(String prefix) {
        transport = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        linkable = Configuration.getPid(prefix + "." + PAR_LINKABLE);
        abstractGraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        globalRequestStatus = new HashMap<>();
    }

    //    Randomization Utils ==============================================================================================
    public static Node getRandomNode() {
        int nodeId;
        Node node;

        do {
            nodeId = CommonState.r.nextInt(Network.size());
            node = Network.get(nodeId);
        } while (!node.isUp());

        return node;
    }

    public static String getRandomFileIdInSystem() {
        String[] fileIds = globalContentAddressingTable.keySet().toArray(new String[0]);
        return fileIds[CommonState.r.nextInt(globalContentAddressingTable.size())];
    }

    public static MessageType getRandomRequestType() {
        int i = CommonState.r.nextInt(fileOperations.size());
        MessageType operation = fileOperations.toArray(new MessageType[0])[i];
        return operation;
    }

    public static long getLatency(Node from, Node to) {
        return (long) shortestPaths.getPathWeight(from, to);
    }

    //    Bitswap===========================================================================================================
    public static boolean validDebt(Ledger ledger) {
        double debtRatio = (double) ledger.getByteSent() / (ledger.getByteRecv() + 1);
        double probability = 1 - (1 / (1 + Math.exp(6 - 3 * debtRatio)));

        return CommonState.r.nextDouble() <= probability;
    }

    //    Network Topology =================================================================================================
    public boolean execute() {
        // Construct the abstract network graph, excluding those which are not up
        for (int i = 0; i < Network.size(); i++) {
            if (Network.get(i).isUp()) {
                Node node = Network.get(i);
                abstractGraph.addVertex(node);
            }
        }

        // Add edges and set the weights
        for (int i = 0; i < Network.size(); i++) {
            Node node = Network.get(i);
            if (!node.isUp()) {
                continue;
            }
            Linkable linkable = (Linkable) node.getProtocol(this.linkable);
            for (int j = 0; j < linkable.degree(); j++) {
                Transport transport = (Transport) node.getProtocol(this.transport);
                Node neighbor = linkable.getNeighbor(j);
                if (!neighbor.isUp()) {
                    continue;
                }
                abstractGraph.addEdge(node, neighbor, new DefaultWeightedEdge());
                abstractGraph.setEdgeWeight(node, neighbor, transport.getLatency(node, neighbor));
            }
        }

        // Generate the shortest paths among up nodes using Dijkstra
        shortestPaths = new DijkstraShortestPath<>(abstractGraph);

        return false;
    }

    public GraphPath<Node, DefaultWeightedEdge> getShortestPath(Node from, Node to) {
        return shortestPaths.getPaths(from).getPath(to);
    }
}

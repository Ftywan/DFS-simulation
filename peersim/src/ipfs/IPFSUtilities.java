package ipfs;

import ipfs.message.IPFSMessage;
import ipfs.message.MessageStatus;
import ipfs.message.MessageType;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import peersim.config.Configuration;
import peersim.core.*;
import peersim.transport.Transport;

import java.util.*;

public class IPFSUtilities implements Control {
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_LINKABLE = "linkable";
    public static Map<String, Node> globalContentAddressingTable = new HashMap<>();
    public static Set<Long> deadNode = new HashSet<>();
    public static Set<MessageType> fileOperations = new HashSet<>();
    public static HashMap<IPFSMessage, MessageStatus> globalRequestStatus;
    public static HashMap<IPFSMessage, Long> startTimestamp;
    public static HashMap<IPFSMessage, Long> endTimestamp;
    public static List<Long> distributionOrderNodeIds;
    private static DijkstraShortestPath<Node, DefaultWeightedEdge> shortestPaths;

    static {
        fileOperations.add(MessageType.ADD);
        fileOperations.add(MessageType.DELETE);
        fileOperations.add(MessageType.RETRIEVE);
    }

    private final Graph<Node, DefaultWeightedEdge> abstractGraph;
    private final int transport;
    private final int linkable;

    public IPFSUtilities(String prefix) {
        transport = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        linkable = Configuration.getPid(prefix + "." + PAR_LINKABLE);
        abstractGraph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        globalRequestStatus = new HashMap<>();
        startTimestamp = new HashMap<>();
        endTimestamp = new HashMap<>();
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

    public static List<String> getRandomNFileIdsInSameNode(Node target, int n, int storageProtocolId) {
        return ((IPFS) target.getProtocol(storageProtocolId)).getNFileIds(n);
    }

    public static MessageType getRandomRequestType(double addPercentage, double deletePercentage) {
        MessageType operation;
        double i = CommonState.r.nextDouble();
        if (i < addPercentage) {
            operation = MessageType.ADD;
        } else if (i < deletePercentage + addPercentage) {
            operation = MessageType.DELETE;
        } else {
            operation = MessageType.RETRIEVE;
        }
        return operation;
    }

    /**
     * @param singleFilePercentage the probability mass of X<1
     * @return the ceiling value of a random variable following exponential distribution
     */
    public static int getRandomRequestNumber(double singleFilePercentage) {
        assert singleFilePercentage != 1.0;
        // quantile formula: X = -ln(1-p)/lambda; X = 1 here
        double lambda = -Math.log(1 - singleFilePercentage);
        // random sample using exponential distribution
        return (int) Math.ceil(Math.log(1 - CommonState.r.nextDouble()) / (-lambda));
    }

    public static long getLatency(Node from, Node to) {
        return (long) shortestPaths.getPathWeight(from, to);
    }

    public static boolean dropped(Node from, Node to, double dropRate) {
        int pathSections = shortestPaths.getPath(from, to).getLength();
        double successRate = Math.pow((1 - dropRate), pathSections);
        return CommonState.r.nextDouble() > successRate;
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
}

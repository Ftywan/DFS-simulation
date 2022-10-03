package ipfs;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class NetworkWiring extends WireGraph {
    private static final String PAR_ALPHA = "alpha";
    private static final String PAR_COORDINATES_PROT = "coord_protocol";

    private final double alpha;
    private final int coordinatePid;

    public NetworkWiring(String prefix) {
        super(prefix);
        alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA, 0.5);
        coordinatePid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
    }

    @Override
    public void wire(Graph g) {
        int[] hops = new int[Network.size()];

        for (int i= 0; i < Network.size(); i ++) {
            Node n = (Node) g.getNode(i);

            int candidate_idx = 0;
            double min = Double.POSITIVE_INFINITY;

            for (int j = 0; j < i; j ++) {
                Node parent = (Node) g.getNode(j);
                double parentHopsDistance = hops[j];
                double value = 0;
            }
        }
    }
}

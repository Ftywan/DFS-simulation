package ipfs;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.transport.UnreliableTransport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ipfs.IPFSUtilities.distributionOrderNodeIds;

/**
 * Initializer class for the entre network, with all the nodes
 */
public class NetworkInitializer implements Control {

    private static final String PARAM_PROTOCOL = "protocol";
    private static final String PARAM_NUM_CHUNK = "numChunk";
    private static final double NODE_DOWN_RATE = 0.01;

    private final int IPFSProtocolId;
    private final int numOfChunk;

    public NetworkInitializer(String prefix) {
        IPFSProtocolId = Configuration.getPid(prefix + "." + PARAM_PROTOCOL);
        numOfChunk = Configuration.getInt(prefix + "." + PARAM_NUM_CHUNK);
    }

    @Override
    public boolean execute() {
        // Initialize dead nodes
        while (IPFSUtilities.deadNode.size() < Network.size() * NODE_DOWN_RATE) {
            int deadNodeId = CommonState.r.nextInt(Network.size());
            Network.get(deadNodeId).setFailState(Fallible.DOWN);
            IPFSUtilities.deadNode.add(Network.get(deadNodeId).getID());
        }

        // Initialize global file distribution following the normal distribution
        // Mean value is network size/2 and sigma value is size/6
        long mean = Math.round(Network.size() / 2.0);
        long sigma = Math.round(Network.size() / 6.0);
        List<Long> distributionOrder = new ArrayList<>();
        for (int i = 0; i < Network.size(); i ++) {
            distributionOrder.add(Network.get(i).getID());
        }
        distributionOrderNodeIds = distributionOrder;

        for (int i = 0; i < numOfChunk; i++) {
            FileChunk chunk = new FileChunk();
            int assignedNodeId;
            do {
                assignedNodeId = (int) Math.round(CommonState.r.nextGaussian(mean, sigma));
                assignedNodeId = Math.max(assignedNodeId, 0);
                assignedNodeId = Math.min(assignedNodeId, Network.size() - 1);
            } while (!Network.get(assignedNodeId).isUp());
            IPFSUtilities.globalContentAddressingTable.put(chunk.getId(), Network.get(assignedNodeId));
            ((IPFS) Network.get(assignedNodeId).getProtocol(IPFSProtocolId)).addToStorage(chunk);
        }
        return false;
    }
}

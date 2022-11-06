package ipfs;

import peersim.config.Configuration;
import peersim.core.*;

/**
 * Initializer class for the entre network, with all the nodes
 */
public class NetworkInitializer implements Control {

    private static final String PARAM_PROTOCOL ="protocol";
    private static final String PARAM_TRANSPORT = "transport";
    private static final String PARAM_NUM_CHUNK = "numChunk";
    private static final double NODE_DOWN_RATE = 0.05;

    private int IPFSProtocolId;
    private int transportProtocolId;
    private int numOfChunk;

    public NetworkInitializer(String prefix) {
        IPFSProtocolId = Configuration.getPid(prefix+"."+ PARAM_PROTOCOL);
        transportProtocolId = Configuration.getPid(prefix+"."+ PARAM_TRANSPORT);
        numOfChunk = Configuration.getInt(prefix + "." + PARAM_NUM_CHUNK);
    }
    @Override
    public boolean execute() {
        // Initialize dead nodes
        while (IPFSUtilities.deadNode.size() < Network.size() * NODE_DOWN_RATE) {
            int deadNodeId = CommonState.r.nextInt(Network.size());
            IPFSUtilities.deadNode.add(Network.get(deadNodeId).getID());
        }

        // Initialize global file distribution
        for (int i = 0; i < numOfChunk; i ++) {
            FileChunk chunk = new FileChunk();
            int assignedNodeId = CommonState.r.nextInt(Network.size());
            IPFSUtilities.globalContentAddressingTable.put(chunk.getId(), Network.get(assignedNodeId).getID());
            ((IPFS) Network.get(assignedNodeId).getProtocol(IPFSProtocolId)).storage.add(chunk);
        }

        // TODO: potentially add a more customized logic to link the nodes
        return false;
    }
}

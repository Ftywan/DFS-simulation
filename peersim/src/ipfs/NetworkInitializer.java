package ipfs;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;

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

        // Initialize global file distribution
        for (int i = 0; i < numOfChunk; i++) {
            FileChunk chunk = new FileChunk();
            int assignedNodeId;
            do {
                assignedNodeId = CommonState.r.nextInt(Network.size());
            } while (! Network.get(assignedNodeId).isUp());
            IPFSUtilities.globalContentAddressingTable.put(chunk.getId(), Network.get(assignedNodeId));
            ((IPFS) Network.get(assignedNodeId).getProtocol(IPFSProtocolId)).addToStorage(chunk);
        }

        // Initialize the event schedule
//        for (int i = 0; i < Network.size(); i++) {
//            Node node = getRandomNode();
//            IPFSMessage message = new IPFSMessage(node, MessageType.ADD);
//            EDSimulator.add(1000, message, node, );
//        }
        return false;
    }
}

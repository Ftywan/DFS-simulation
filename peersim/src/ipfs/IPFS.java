package ipfs;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IPFS implements CDProtocol, EDProtocol {
    private int bytesSent;
    private int bytesReceived;
    private boolean alive;
    Set<FileChunk> storage;

    /**
     * Initializes IPFS
     * @param prefix
     */
    public IPFS(String prefix) {
        bytesSent = 0;
        bytesReceived = 0;
        storage = new HashSet<>();
    }

    /**
     * Triggered by the cycle-driven
     * @param node
     *          the node on which this component is run
     * @param protocolID
     *          the id of this protocol in the protocol array
     */
    @Override
    public void nextCycle(Node node, int protocolID) {
        if (! IPFSUtilities.deadNode.contains(node.getIndex()))
        Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(protocolID));
        int targetChunkId = CommonState.r.nextInt(IPFSUtilities.globalContentAddressingTable.size());
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    public static void init() {

    }

    @Override
    public Object clone() {
        Object newProtocol;
        try{
            newProtocol = super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return newProtocol;
    }
}

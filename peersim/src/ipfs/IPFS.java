package ipfs;

import ipfs.message.*;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ipfs.IPFSUtilities.*;

public class IPFS implements CDProtocol, EDProtocol {
    private int bytesSent;
    private int bytesReceived;
    Set<FileChunk> storage;
    private int pid;
    /**
     * Initializes IPFS
     * @param prefix
     */
    public IPFS(String prefix) {
        bytesSent = 0;
        bytesReceived = 0;
        storage = new HashSet<>();
//        pid = Configuration.getPid(prefix);
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
        Node dest;
        // randomize an IO event
        IPFSMessage message = getRandomOperation(node);
        if (message instanceof AddFileMessage) {
            dest = getRandomNode();
            EDSimulator.add(getLatency(message.getSender(), dest), message, dest, protocolID);

        } else if (message instanceof DeleteFileMessage) {
            dest = globalContentAddressingTable.get(((DeleteFileMessage) message).getChunkId());
            EDSimulator.add(getLatency(message.getSender(), dest), message, dest, protocolID);

        } else if (message instanceof RetrieveFileMessage) {
            dest = globalContentAddressingTable.get(((RetrieveFileMessage) message).getChunkId());
            EDSimulator.add(getLatency(message.getSender(), dest), message, dest, protocolID);

        } else if (message instanceof UpdateFileMessage) {
            dest = getRandomNode();
            EDSimulator.add(getLatency(message.getSender(), dest), message, dest, protocolID);

        } else {
            throw new UnsupportedOperationException();
        }
//        if (! IPFSUtilities.deadNode.contains(node.getIndex())){}
//        Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(protocolID));
//        int targetChunkId = CommonState.r.nextInt(IPFSUtilities.globalContentAddressingTable.size());
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        IPFSMessage message = (IPFSMessage) event;
        if (message instanceof AddFileMessage) {
        } else if (message instanceof DeleteFileMessage) {

        } else if (message instanceof RetrieveFileMessage) {

        } else if (message instanceof UpdateFileMessage) {

        } else {
            throw new UnsupportedOperationException();
        }
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

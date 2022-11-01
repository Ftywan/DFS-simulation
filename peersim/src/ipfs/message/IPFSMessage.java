package ipfs.message;

import peersim.core.Node;

/**
 * The message template in IPFS.
 * A message servers as an event in the simulation.
 */
public class IPFSMessage {
    private final Node sender;
    private final MessageType type;


    public IPFSMessage(Node sender, MessageType type) {
        this.sender = sender;
        this.type = type;
    }
}

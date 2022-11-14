package ipfs.message;

import lombok.Getter;
import peersim.core.Node;

/**
 * The message template in IPFS.
 * A message servers as an event in the simulation.
 */
public class IPFSMessage {
    @Getter
    private final Node sender;
    private final MessageType type;
    private long byteSent;
    private long byteRecv;

    /**
     * For Request
     * @param sender
     * @param type
     * @param byteSent
     * @param byteRecv
     */
    public IPFSMessage(Node sender, MessageType type, Long byteSent, Long byteRecv) {
        this.sender = sender;
        this.type = type;
        this.byteSent = byteSent;
        this.byteRecv = byteRecv;
    }

    /**
     * For Response
     * @param sender
     * @param type
     */
    public IPFSMessage(Node sender, MessageType type) {
        this.sender = sender;
        this.type = type;
    }
}

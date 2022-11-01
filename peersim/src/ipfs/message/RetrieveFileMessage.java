package ipfs.message;

import peersim.core.Node;

public class RetrieveFileMessage extends IPFSMessage {
    private final String chunkId;
    public RetrieveFileMessage(Node sender, String chunkId) {
        super(sender, MessageType.RETRIEVE);
        this.chunkId = chunkId;
    }
}

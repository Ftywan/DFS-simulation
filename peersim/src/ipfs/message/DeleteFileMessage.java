package ipfs.message;

import peersim.core.Node;

public class DeleteFileMessage extends IPFSMessage {
    private final String chunkId;

    public DeleteFileMessage(Node sender, String chunkId) {
        super(sender, MessageType.DELETE);
        this.chunkId = chunkId;
    }
}

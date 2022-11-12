package ipfs.message;

import lombok.Getter;
import peersim.core.Node;

public class DeleteFileMessage extends IPFSMessage {
    @Getter
    private final String chunkId;

    public DeleteFileMessage(Node sender, String chunkId) {
        super(sender, MessageType.DELETE);
        this.chunkId = chunkId;
    }
}

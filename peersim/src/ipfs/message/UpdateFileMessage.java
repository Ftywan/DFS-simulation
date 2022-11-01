package ipfs.message;

import ipfs.FileChunk;
import peersim.core.Node;

public class UpdateFileMessage extends IPFSMessage {
    private final FileChunk chunkToUpdate;
    public UpdateFileMessage(Node sender, FileChunk fileChunk) {
        super(sender, MessageType.UPDATE);
        this.chunkToUpdate = fileChunk;
    }
}

package ipfs.message;

import ipfs.FileChunk;
import peersim.core.Node;

public class UpdateFileMessage extends IPFSMessage {
    private final FileChunk chunkToUpdate;
    private final String chunkId;
    public UpdateFileMessage(Node sender, String chunkId, FileChunk fileChunk) {
        super(sender, MessageType.UPDATE);
        this.chunkId = chunkId;
        this.chunkToUpdate = fileChunk;
    }
}

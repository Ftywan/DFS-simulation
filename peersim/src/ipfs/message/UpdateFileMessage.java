package ipfs.message;

import ipfs.FileChunk;
import peersim.core.Node;

public class UpdateFileMessage extends IPFSMessage {
    private final FileChunk chunkToUpdate;
    private final String chunkId;
    public UpdateFileMessage(Node sender, String chunkId, FileChunk fileChunk, Long byteSent, Long byteRecv) {
        super(sender, MessageType.UPDATE, byteSent, byteRecv);
        this.chunkId = chunkId;
        this.chunkToUpdate = fileChunk;
    }
}

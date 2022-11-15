package ipfs.message;

import ipfs.FileChunk;
import lombok.Getter;
import peersim.core.Node;

public class UpdateFileMessage extends IPFSMessage {
    @Getter
    private final FileChunk chunkToUpdate;
    @Getter
    private final String chunkId;

    public UpdateFileMessage(Node sender, String chunkId, FileChunk fileChunk, Long byteSent, Long byteRecv) {
        super(sender, MessageType.UPDATE, byteSent, byteRecv);
        this.chunkId = chunkId;
        this.chunkToUpdate = fileChunk;
    }
}

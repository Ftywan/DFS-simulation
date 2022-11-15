package ipfs.message;

import ipfs.FileChunk;
import lombok.Getter;
import peersim.core.Node;

public class AddFileMessage extends IPFSMessage {
    @Getter
    private final FileChunk chunkToSave;

    public AddFileMessage(Node sender, FileChunk chunk, Long byteSent, Long byteRecv) {
        super(sender, MessageType.ADD, byteSent, byteRecv);
        this.chunkToSave = chunk;
    }
}

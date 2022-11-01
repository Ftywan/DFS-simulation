package ipfs.message;

import ipfs.FileChunk;
import peersim.core.Node;

public class AddFileMessage extends IPFSMessage{
    private final FileChunk chunkToSave;

    public AddFileMessage(Node sender, FileChunk chunk) {
        super(sender, MessageType.ADD);
        this.chunkToSave = chunk;
    }
}

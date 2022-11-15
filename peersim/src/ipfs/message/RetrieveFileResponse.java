package ipfs.message;

import ipfs.FileChunk;
import lombok.Getter;
import peersim.core.Node;

public class RetrieveFileResponse extends IPFSMessage {
    @Getter
    private final FileChunk fileChunk;
    public RetrieveFileResponse(Node sender, MessageType type, FileChunk fileChunk) {
        super(sender, type);
        this.fileChunk = fileChunk;
    }
}

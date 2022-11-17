package ipfs.message;

import ipfs.FileChunk;
import lombok.Getter;
import peersim.core.Node;

public class RetrieveFileResponse extends IPFSResponse {
    @Getter
    private final FileChunk fileChunk;

    public RetrieveFileResponse(Node sender, MessageType type, FileChunk fileChunk, IPFSMessage requestMessage) {
        super(sender, type, requestMessage);
        this.fileChunk = fileChunk;
    }
}

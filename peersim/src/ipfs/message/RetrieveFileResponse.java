package ipfs.message;

import ipfs.FileChunk;
import lombok.Getter;
import peersim.core.Node;

import java.util.List;

public class RetrieveFileResponse extends IPFSResponse {
    @Getter
    private final List<FileChunk> fileChunks;

    public RetrieveFileResponse(Node sender, MessageType type, List<FileChunk> fileChunks, IPFSMessage requestMessage) {
        super(sender, type, requestMessage);
        this.fileChunks = fileChunks;
    }
}

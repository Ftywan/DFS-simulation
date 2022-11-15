package ipfs.message;

import lombok.Getter;
import peersim.core.Node;

public class RetrieveFileMessage extends IPFSMessage {
    @Getter
    private final String chunkId;

    public RetrieveFileMessage(Node sender, String chunkId, Long byteSent, Long byteRecv) {
        super(sender, MessageType.RETRIEVE, byteSent, byteRecv);
        this.chunkId = chunkId;
    }
}

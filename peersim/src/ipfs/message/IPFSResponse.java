package ipfs.message;

import lombok.Getter;
import peersim.core.Node;

public class IPFSResponse extends IPFSMessage {
    @Getter
    IPFSMessage requestMessage;

    public IPFSResponse(Node sender, MessageType type, IPFSMessage requestMessage) {
        super(sender, type);
        this.requestMessage = requestMessage;
    }
}

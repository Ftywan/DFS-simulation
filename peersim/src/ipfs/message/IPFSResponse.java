package ipfs.message;

import ipfs.IPFS;
import lombok.Getter;
import peersim.core.Node;

public class IPFSResponse extends IPFSMessage{
    @Getter
    IPFSMessage requestMessage;

    public IPFSResponse(Node sender, MessageType type, IPFSMessage requestMessage) {
        super(sender, type);
        this.requestMessage = requestMessage;
    }
}

package ipfs.message;

import peersim.core.Node;

public class AddFileResponse extends IPFSResponse {
    public AddFileResponse(Node sender, MessageType type, IPFSMessage requestMessage) {
        super(sender, type, requestMessage);
    }
}

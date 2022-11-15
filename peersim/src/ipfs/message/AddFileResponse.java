package ipfs.message;

import peersim.core.Node;

public class AddFileResponse extends IPFSMessage{
    public AddFileResponse(Node sender, MessageType type) {
        super(sender, type);
    }
}

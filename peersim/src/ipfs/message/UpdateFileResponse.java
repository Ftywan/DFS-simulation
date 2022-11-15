package ipfs.message;

import peersim.core.Node;

public class UpdateFileResponse extends IPFSMessage {
    public UpdateFileResponse(Node sender, MessageType type) {
        super(sender, type);
    }
}

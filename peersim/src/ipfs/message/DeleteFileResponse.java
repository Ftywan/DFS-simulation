package ipfs.message;

import peersim.core.Node;

public class DeleteFileResponse extends IPFSMessage{
    public DeleteFileResponse(Node sender, MessageType type) {
        super(sender, type);
    }
}

package ipfs.message;

import lombok.Getter;
import peersim.core.Node;

public class DeleteFileResponse extends IPFSResponse {

    public DeleteFileResponse(Node sender, MessageType type, IPFSMessage requestMessage) {
        super(sender, type, requestMessage);
    }
}

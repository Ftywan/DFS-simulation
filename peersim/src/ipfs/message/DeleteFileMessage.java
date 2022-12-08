package ipfs.message;

import ipfs.Ledger;
import lombok.Getter;
import peersim.core.Node;

import java.util.List;

public class DeleteFileMessage extends IPFSMessage {
    @Getter
    private final List<String> chunkIds;

    public DeleteFileMessage(Node sender, List<String> chunkIds, Ledger ledger) {
        super(sender, MessageType.DELETE, ledger);
        this.chunkIds = chunkIds;
    }
}

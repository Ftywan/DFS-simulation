package ipfs.message;

import ipfs.Ledger;
import lombok.Getter;
import peersim.core.Node;

import java.util.List;

public class RetrieveFileMessage extends IPFSMessage {
    @Getter
    private final List<String> chunkIds;

    public RetrieveFileMessage(Node sender, List<String> chunkIds, Ledger ledger) {
        super(sender, MessageType.RETRIEVE, ledger);
        this.chunkIds = chunkIds;
    }

    @Override
    public int getRequestCount() {
        return chunkIds.size();
    }
}

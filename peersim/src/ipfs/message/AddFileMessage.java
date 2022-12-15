package ipfs.message;

import ipfs.FileChunk;
import ipfs.Ledger;
import lombok.Getter;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

public class AddFileMessage extends IPFSMessage {
    @Getter
    private final List<FileChunk> chunksToSave;

    public AddFileMessage(Node sender, int numOfChunk, Ledger ledger) {
        super(sender, MessageType.ADD, ledger);
        chunksToSave = new ArrayList<>();
        for (int i = 0; i < numOfChunk; ++i) {
            chunksToSave.add(new FileChunk());
        }
    }

    @Override
    public int getRequestCount() {
        return chunksToSave.size();
    }
}

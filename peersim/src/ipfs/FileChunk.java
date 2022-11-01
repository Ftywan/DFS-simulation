package ipfs;

import lombok.Getter;

import java.util.UUID;

public class FileChunk {
    @Getter
    private final String id;

    public FileChunk() {
        id = UUID.randomUUID().toString();
    }
}

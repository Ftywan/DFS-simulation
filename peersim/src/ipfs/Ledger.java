package ipfs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ledger {
    private long byteSent;
    private long byteRecv;

    public Ledger(long byteSent, long byteRecv) {
        this.byteSent = byteSent;
        this.byteRecv = byteRecv;
    }

    public void incrementByteSent(long increment) {
        this.byteSent += increment;
    }

    public void incrementByteRecv(long increment) {
        this.byteRecv += increment;
    }
}

package ipfs;

import java.util.HashMap;
import java.util.Map;

public class FileSystemStatus {
    // TODO: distributed hashtable
    public static Map<String, Long> globalContentAddressingTable = new HashMap<>();
}

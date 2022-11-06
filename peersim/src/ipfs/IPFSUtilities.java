package ipfs;

import ipfs.message.MessageType;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.transport.Transport;

import javax.print.attribute.HashPrintServiceAttributeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IPFSUtilities implements Control {
    private static final String PAR_TRANSPORT = "transport";

    // TODO: routing table

    public static Map<String, Long> globalContentAddressingTable = new HashMap<>();
    public static Set<Long> deadNode = new HashSet<>();

    public static Set<MessageType> fileOperations = new HashSet<>();
    static {
        fileOperations.add(MessageType.ADD);
        fileOperations.add(MessageType.DELETE);
        fileOperations.add(MessageType.RETRIEVE);
        fileOperations.add(MessageType.UPDATE);
    }

    private final int transport;
    public IPFSUtilities(String prefix) {
        transport = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
    }

    public boolean execute() {

    }
}

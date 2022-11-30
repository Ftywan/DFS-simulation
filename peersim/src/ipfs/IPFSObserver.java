package ipfs;

import ipfs.message.IPFSMessage;
import ipfs.message.MessageStatus;
import peersim.config.Configuration;
import peersim.core.Control;

import java.util.Map;

import static ipfs.IPFSUtilities.globalRequestStatus;

/**
 * Observer class to monitor the overall status and generate simulation results
 */
public class IPFSObserver implements Control {
    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PARAM_PROTOCOL = "protocol";
    private static final String PARAM_NETSIZE = "size";
    private static final String PARAM_DROPRATE = "droprate";
    private static final String PARAM_MINDELAY = "mindelay";
    private static final String PARAM_MAXDELAY = "maxdelay";
    /**
     * Protocol identifier, obtained from config property
     */
    private final int IPFSProtocolId;
    private final int netSize;
    private final double dropRate;
    private final int minDelay;
    private final int maxDelay;

    /**
     * The basic constructor that reads the configuration file.
     *
     * @param prefix the configuration prefix for this class
     */
    public IPFSObserver(String prefix) {
        IPFSProtocolId = Configuration.getPid(prefix + "." + PARAM_PROTOCOL);
        netSize = Configuration.getInt(prefix + "." + PARAM_NETSIZE);
        dropRate = Configuration.getDouble(prefix + "." + PARAM_DROPRATE);
        minDelay = Configuration.getInt(prefix + "." + PARAM_MINDELAY);
        maxDelay = Configuration.getInt(prefix + "." + PARAM_MAXDELAY);

        System.out.println("A new instance");
    }

    /**
     * Method to print system metrics of IPFS and
     *
     * @return
     */
    @Override
    public boolean execute() {
//        System.out.println("Observing");
        int failCount = 0;
        int successCount = 0;
        int flying = 0;
        int dropped = 0;

        for (Map.Entry<IPFSMessage, MessageStatus> entry : globalRequestStatus.entrySet()) {
            if (entry.getValue() == MessageStatus.FLYING) {
                flying++;
            } else if (entry.getValue() == MessageStatus.SUCCESS) {
                successCount++;
            } else if (entry.getValue() == MessageStatus.DROPPED) {
                dropped++;
            } else if (entry.getValue() == MessageStatus.REJECTED) {
                failCount++;
            }
        }

        System.out.println("success: " + successCount + " flying: " + flying + " dropped: " + dropped + " failed: " + failCount);
        return false;
    }
}

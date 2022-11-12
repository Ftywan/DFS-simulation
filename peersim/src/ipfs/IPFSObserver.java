package ipfs;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Observer class to monitor the overall status and generate simulation results
 */
public class IPFSObserver implements Control {
    /**
     *	The protocol to operate on.
     *	@config
     */
    private static final String PARAM_PROTOCOL ="protocol";

    /**
     *	Protocol identifier, obtained from config property
     */
    private final int IPFSProtocolId;

    /**
     *	The basic constructor that reads the configuration file.
     *	@param prefix the configuration prefix for this class
     */
    public IPFSObserver(String prefix) {
        IPFSProtocolId = Configuration.getPid(prefix + "." + PARAM_PROTOCOL);
    }

    /**
     * Method to print system metrics of IPFS and
     * @return
     */
    @Override
    public boolean execute() {
        System.out.println("haha");
        return false;
    }
}

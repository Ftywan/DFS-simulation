package ipfs;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class NetworkInitializer implements Control {

    private static final String PAR_PROT = "protocol";
    private static int pid;

    public NetworkInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    @Override
    public boolean execute() {
        Node root = Network.get(0);
        Coordinate coordinate = (Coordinate) root.getProtocol(pid);
        coordinate.setX(0.5);
        coordinate.setY(0.5);

        Node node;
        for (int i = 0; i < Network.size(); i ++) {
            node = Network.get(i);
            coordinate = (Coordinate) node.getProtocol(pid);
            coordinate.setX(CommonState.r.nextDouble());
            coordinate.setY(CommonState.r.nextDouble());
        }
        return false;
    }
}

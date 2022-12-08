package ipfs;

import ipfs.message.MessageStatus;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.util.IncrementalStats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static String PATH;
    /**
     * Protocol identifier, obtained from config property
     */
    private final int IPFSProtocolId;
    private final int netSize;
    private final double dropRate;
    private final int minDelay;
    private final int maxDelay;

    File output;
    FileWriter writer;

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

        PATH = "exp/" + netSize + "-" + dropRate + "-" + minDelay + "-" + maxDelay + "" + ".csv";

        String[] header = {"time", "success", "flying", "dropped", "failed"};
        try {
            Files.writeString(Path.of(PATH), String.join(",", header) + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to print system metrics of IPFS and
     *
     * @return
     */
    @Override
    public boolean execute() {
        IncrementalStats stats = new IncrementalStats();
        long time = peersim.core.CommonState.getTime();

        Map<MessageStatus, Long> count = globalRequestStatus.values().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long[] row = {
                time,
                count.getOrDefault(MessageStatus.SUCCESS, 0L),
                count.getOrDefault(MessageStatus.FLYING, 0L),
                count.getOrDefault(MessageStatus.DROPPED, 0L),
                count.getOrDefault(MessageStatus.REJECTED, 0L)
        };
        try {
            Files.writeString(Path.of(PATH), Arrays.stream(row).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}

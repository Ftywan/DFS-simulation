package ipfs;

import ipfs.message.IPFSMessage;
import ipfs.message.MessageStatus;
import ipfs.message.MessageType;
import org.apache.commons.lang3.ArrayUtils;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ipfs.IPFSUtilities.*;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

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
    private static final String PARAM_STEP = "step";
    private static final String PARAM_SINGLE_REQ_PERCENTAGE = "singleReqPercentage";
    private static final String PARAM_ADD_PERCENTAGE = "addPercentage";
    private static final String PARAM_DELETE_PERCENTAGE = "deletePercentage";
    private static final String PARAM_K = "k";
    private static String REQUEST_PATH;
    private static String FILE_PATH;
    private static String LATENCY_PATH;
    /**
     * Protocol identifier, obtained from config property
     */
    private final int IPFSProtocolId;
    private final int netSize;
    private final double dropRate;
    private final int minDelay;
    private final int maxDelay;
    private final int step;
    private final double singleRequestPercentage;
    private final double addPercentage;
    private final double deletePercentage;
    private final int k;

    File output;
    FileWriter writer;
    List<Node> nodeList;

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
        step = Configuration.getInt(prefix + "." + PARAM_STEP);
        singleRequestPercentage = Configuration.getDouble(prefix + "." +PARAM_SINGLE_REQ_PERCENTAGE);
        addPercentage = Configuration.getDouble(prefix+"."+PARAM_ADD_PERCENTAGE);
        deletePercentage = Configuration.getDouble(prefix+"."+PARAM_DELETE_PERCENTAGE);
        k = Configuration.getInt(prefix+"."+PARAM_K);

        String identifier = netSize + "nodes" + dropRate + "drop" + minDelay + "minD" + maxDelay + "maxD"+
                singleRequestPercentage+"singleReq"+addPercentage+"add"+deletePercentage+"delete"+k+"K";

        REQUEST_PATH = "exp/" + identifier + "-request" + ".csv";
        String[] header = {"time", "success", "flying", "dropped", "failed"};
        try {
            Files.writeString(Path.of(REQUEST_PATH), String.join(",", header) + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FILE_PATH = "exp/" + identifier + "-file"+ ".csv";
        nodeList = new ArrayList<>();
        for (int i = 0; i < Network.size(); i ++) {
            nodeList.add(Network.get(i));
        }
        String fileHeader = distributionOrderNodeIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        fileHeader = "time," + fileHeader;
        try {
            Files.writeString(Path.of(FILE_PATH), fileHeader + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LATENCY_PATH = "exp/" + identifier + "-latency" + ".csv";
        String latencyHeader = "type,start,end,duration";
        try {
            Files.writeString(Path.of(LATENCY_PATH), latencyHeader + "\n");
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
        long time = peersim.core.CommonState.getTime();

        // success, flying, dropped, rejected
        long[] row = {time, 0, 0, 0, 0};
        for (Map.Entry<IPFSMessage, MessageStatus> entry : globalRequestStatus.entrySet()) {
            MessageStatus status = entry.getValue();
            if (status == MessageStatus.SUCCESS) {
                row[1] += entry.getKey().getRequestCount();
            } else if (status == MessageStatus.FLYING) {
                row[2] += entry.getKey().getRequestCount();
            } else if (status == MessageStatus.DROPPED) {
                row[3] += entry.getKey().getRequestCount();
            } else if (status == MessageStatus.REJECTED) {
                row[4] += entry.getKey().getRequestCount();
            }
        }
        try {
            Files.writeString(Path.of(REQUEST_PATH), Arrays.stream(row).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long[] fileRow = new long[Network.size()+1];
        fileRow[0] = time;
        Map<Long, Long> fileDistribution = getFileCount();
        int idx = 1;
        for (Node node: nodeList) {
            fileRow[idx] = fileDistribution.getOrDefault(node.getID(), 0L);
            idx++;
        }
        try {
            Files.writeString(Path.of(FILE_PATH), Arrays.stream(fileRow).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // log only when ending
        if (CommonState.getTime() >= CommonState.getEndTime() - step) {
            for (Map.Entry<IPFSMessage, Long> entry : endTimestamp.entrySet()) {
                MessageType type = entry.getKey().getType();
                Long start = startTimestamp.get(entry.getKey());
                Long end = entry.getValue();
                Long latency = end - start;

                try {
                    Files.writeString(Path.of(LATENCY_PATH), type.toString() + "," + start + "," + end + "," + latency + "\n", APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;

    }

    public Map<Long, Long> getFileCount() {
        Map<Long, Long> idToCount = new HashMap<>();

        for (Map.Entry<String, Node> entry : globalContentAddressingTable.entrySet()) {
            idToCount.merge(entry.getValue().getID(), 1L, Long::sum);
        }

        return idToCount;
    }
}

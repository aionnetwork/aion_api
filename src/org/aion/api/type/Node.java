package org.aion.api.type;

/**
 * Represents the latest network status and the Aion blockchain status of the node.
 *
 * @author Jay Tseng
 * @see org.aion.api.INet#getActiveNodes() getActiveNodes
 */
public final class Node {

    private final long blockNumber;
    private final int p2pPort;
    private final int latency;
    private final String nodeId;
    private final String p2pIP;

    private Node(NodeBuilder builder) {
        this.blockNumber = builder.blockNumber;
        this.p2pPort = builder.p2pPort;
        this.latency = builder.latency;
        this.nodeId = builder.nodeId;
        this.p2pIP = builder.p2pIP;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public int getP2pPort() {
        return p2pPort;
    }

    public int getLatency() {
        return latency;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getP2pIP() {
        return p2pIP;
    }

    /** This Builder class is used to build a {@link Node} instance. */
    public static class NodeBuilder {

        private long blockNumber;
        private int p2pPort;
        private int latency;
        private String nodeId;
        private String p2pIP;

        public NodeBuilder() {}

        public Node.NodeBuilder blockNumber(final long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public Node.NodeBuilder p2pPort(final int p2pPort) {
            this.p2pPort = p2pPort;
            return this;
        }

        public Node.NodeBuilder latency(final int latency) {
            this.latency = latency;
            return this;
        }

        public Node.NodeBuilder nodeId(final String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Node.NodeBuilder p2pIP(final String p2pIP) {
            this.p2pIP = p2pIP;
            return this;
        }

        public Node createNode() {
            if (nodeId == null || p2pIP == null) {
                throw new NullPointerException(
                        "NodeId#" + String.valueOf(nodeId) + " P2pIP#" + String.valueOf(p2pIP));
            }

            if (blockNumber < 0 || p2pPort < 0 || latency < 0) {
                throw new IllegalArgumentException(
                        "Block#" + blockNumber + " P2pPort#" + p2pPort + " Latency#" + latency);
            }

            return new Node(this);
        }
    }
}

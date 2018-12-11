package org.aion.api.type;

/**
 * Represents the node syncing status and block info of the connecting node.
 *
 * @see org.aion.api.INet#syncInfo() SyncInfo
 */
public final class SyncInfo {

    private final boolean syncing;
    private final long networkBestBlock;
    private final long chainBestBlock;
    private final long maxImportBlocks;
    private final long startingBlock;

    public SyncInfo(
            boolean syncing,
            long networkBestBlock,
            long chainBestBlock,
            long maxImportBlocks,
            long startingBlock) {
        if (networkBestBlock < 0
                || chainBestBlock < 0
                || startingBlock < 0
                || maxImportBlocks < 1) {
            throw new IllegalArgumentException(
                    "networkBestBlock#"
                            + networkBestBlock
                            + " chainBestBlock#"
                            + chainBestBlock
                            + " maxImportBlocks#"
                            + maxImportBlocks
                            + " startingBlock#"
                            + startingBlock);
        }

        this.syncing = syncing;
        this.networkBestBlock = networkBestBlock;
        this.chainBestBlock = chainBestBlock;
        this.maxImportBlocks = maxImportBlocks;
        this.startingBlock = startingBlock;
    }

    public boolean isSyncing() {
        return syncing;
    }

    public long getNetworkBestBlock() {
        return networkBestBlock;
    }

    public long getChainBestBlock() {
        return chainBestBlock;
    }

    public long getMaxImportBlocks() {
        return maxImportBlocks;
    }

    public long getStartingBlock() {
        return startingBlock;
    }
}

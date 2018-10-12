/*
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 */


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

    public SyncInfo(boolean syncing, long networkBestBlock, long chainBestBlock,
        long maxImportBlocks, long startingBlock) {
        if (networkBestBlock < 0 || chainBestBlock < 0 || startingBlock < 0
            || maxImportBlocks < 1) {
            throw new IllegalArgumentException("networkBestBlock#" + networkBestBlock +
                " chainBestBlock#" + chainBestBlock +
                " maxImportBlocks#" + maxImportBlocks +
                " startingBlock#" + startingBlock);
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

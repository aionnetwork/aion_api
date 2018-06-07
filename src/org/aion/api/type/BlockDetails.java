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

import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

import java.math.BigInteger;
import java.util.List;

/**
 * The BlockDetails return the detailed block information including the transactions and the
 * transaction logs
 *
 * @author Jay Tseng
 * @see org.aion.api.IAdmin#getBlockDetailsByNumber(java.lang.String)  getBlockDetailsByNumber
 * @see org.aion.api.IAdmin#getBlockDetailsByNumber(java.util.List)  getBlockDetailsByNumber
 * @see org.aion.api.type.TxDetails TxDetails
 * @see org.aion.api.type.TxLog TxLog
 */

public final class BlockDetails {

    private final long number;
    private final long timestamp;
    private final long nrgConsumed;
    private final long nrgLimit;
    private final ByteArrayWrapper bloom;
    private final ByteArrayWrapper extraData;
    private final ByteArrayWrapper solution;
    private final Hash256 hash;
    private final Hash256 parentHash;
    private final BigInteger nonce;
    private final BigInteger difficulty;
    private final BigInteger totalDifficulty;
    private final Address minerAddress;
    private final Hash256 stateRoot;
    private final Hash256 txTrieRoot;
    private final Hash256 receiptTxRoot;
    private final int size;
    private final List<TxDetails> txDetails;
    private final long blockTime;

    private BlockDetails(BlockDetails.BlockDetailsBuilder builder) {
        this.number = builder.number;
        this.timestamp = builder.timestamp;
        this.nrgConsumed = builder.nrgConsumed;
        this.nrgLimit = builder.nrgLimit;
        this.bloom = builder.bloom;
        this.extraData = builder.extraData;
        this.solution = builder.solution;
        this.parentHash = builder.parentHash;
        this.hash = builder.hash;
        this.nonce = builder.nonce;
        this.difficulty = builder.difficulty;
        this.minerAddress = builder.minerAddress;
        this.stateRoot = builder.stateRoot;
        this.txTrieRoot = builder.txTrieRoot;
        this.receiptTxRoot = builder.receiptTxRoot;
        this.size = builder.size;
        this.txDetails = builder.txDetails;
        this.totalDifficulty = builder.totalDifficulty;
        this.blockTime = builder.blockTime;
    }

    public long getNumber() {
        return number;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getNrgConsumed() {
        return nrgConsumed;
    }

    public long getNrgLimit() {
        return nrgLimit;
    }

    public ByteArrayWrapper getBloom() {
        return bloom;
    }

    public ByteArrayWrapper getExtraData() {
        return extraData;
    }

    public ByteArrayWrapper getSolution() {
        return solution;
    }

    public Hash256 getParentHash() {
        return parentHash;
    }

    public Hash256 getHash() {
        return hash;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getDifficulty() {
        return difficulty;
    }

    public Address getMinerAddress() {
        return minerAddress;
    }

    public Hash256 getStateRoot() {
        return stateRoot;
    }

    public Hash256 getTxTrieRoot() {
        return txTrieRoot;
    }

    public Hash256 getReceiptTxRoot() {
        return receiptTxRoot;
    }

    public int getSize() {
        return size;
    }

    public List<TxDetails> getTxDetails() {
        return txDetails;
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public static class BlockDetailsBuilder {

        private long number;
        private long timestamp;
        private long nrgConsumed;
        private long nrgLimit;
        private ByteArrayWrapper bloom;
        private ByteArrayWrapper extraData;
        private ByteArrayWrapper solution;
        private Hash256 parentHash;
        private Hash256 hash;
        private BigInteger nonce;
        private BigInteger difficulty;
        private BigInteger totalDifficulty;
        private Address minerAddress;
        private Hash256 stateRoot;
        private Hash256 txTrieRoot;
        private Hash256 receiptTxRoot;
        private int size;
        private List<TxDetails> txDetails;
        private long blockTime;

        public BlockDetailsBuilder() {
        }

        public BlockDetailsBuilder number(final long number) {
            this.number = number;
            return this;
        }

        public BlockDetailsBuilder timestamp(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BlockDetailsBuilder nrgConsumed(final long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public BlockDetailsBuilder nrgLimit(final long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public BlockDetailsBuilder bloom(final ByteArrayWrapper bloom) {
            this.bloom = bloom;
            return this;
        }

        public BlockDetailsBuilder extraData(final ByteArrayWrapper extraData) {
            this.extraData = extraData;
            return this;
        }

        public BlockDetailsBuilder solution(final ByteArrayWrapper solution) {
            this.solution = solution;
            return this;
        }

        public BlockDetailsBuilder txDetails(final List<TxDetails> txDetails) {
            this.txDetails = txDetails;
            return this;
        }

        public BlockDetailsBuilder parentHash(final Hash256 parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public BlockDetailsBuilder hash(final Hash256 hash) {
            this.hash = hash;
            return this;
        }

        public BlockDetailsBuilder nonce(final BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public BlockDetailsBuilder difficulty(final BigInteger difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public BlockDetailsBuilder totalDifficulty(final BigInteger totalDifficulty) {
            this.totalDifficulty = totalDifficulty;
            return this;
        }

        public BlockDetailsBuilder miner(final Address miner) {
            this.minerAddress = miner;
            return this;
        }

        public BlockDetailsBuilder stateRoot(final Hash256 stateRoot) {
            this.stateRoot = stateRoot;
            return this;
        }

        public BlockDetailsBuilder txTrieRoot(final Hash256 txTrieRoot) {
            this.txTrieRoot = txTrieRoot;
            return this;
        }

        public BlockDetailsBuilder receiptTxRoot(final Hash256 receiptTxRoot) {
            this.receiptTxRoot = receiptTxRoot;
            return this;
        }

        public BlockDetailsBuilder size(final int s) {
            this.size = s;
            return this;
        }

        public BlockDetailsBuilder blockTime(final long blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public BlockDetails createBlockDetails() {

            if (bloom == null || extraData == null || solution == null || txDetails == null
                || parentHash == null || hash == null
                || nonce == null || difficulty == null || minerAddress == null || stateRoot == null
                || txTrieRoot == null
                || receiptTxRoot == null || totalDifficulty == null) {
                throw new NullPointerException(
                    "bloom#" + String.valueOf(bloom) +
                        " extraData#" + String.valueOf(extraData) +
                        " solution#" + String.valueOf(solution) +
                        " txDetails#" + String.valueOf(txDetails) +
                        " parentHash#" + String.valueOf(parentHash) +
                        " hash#" + String.valueOf(hash) +
                        " nonce#" + String.valueOf(nonce) +
                        " difficulty#" + String.valueOf(difficulty) +
                        " total difficulty#" + String.valueOf(totalDifficulty) +
                        " minerAddress#" + String.valueOf(minerAddress) +
                        " stateRoot#" + String.valueOf(stateRoot) +
                        " txTrieRoot#" + String.valueOf(txTrieRoot) +
                        " receiptTxRoot#" + String.valueOf(receiptTxRoot));
            }

            if (number < 0 || timestamp < 0 || nrgConsumed < 0 || nrgLimit < 0 || size < 0) {
                throw new IllegalArgumentException("Block#" + number + " Time#" + timestamp
                    + " NrgConsumed#" + nrgConsumed + " NrgLimit#" + nrgLimit
                    + " size#" + size);
            }

            return new BlockDetails(this);
        }
    }
}

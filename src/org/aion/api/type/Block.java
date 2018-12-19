package org.aion.api.type;

import java.math.BigInteger;
import java.util.List;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.vm.api.interfaces.Address;

/**
 * The Block return data structure, typically used in Block getter API.
 *
 * @author Jay Tseng
 * @see org.aion.api.IChain#getBlockByNumber(long) getBlockByNumber
 * @see org.aion.api.IChain#getBlockByHash(Hash256) getBlockByHash
 */
public class Block {

    private final long number;
    private final long timestamp;
    private final long nrgConsumed;
    private final long nrgLimit;
    private final ByteArrayWrapper bloom;
    private final ByteArrayWrapper extraData;
    private final ByteArrayWrapper solution;
    private final List<Hash256> txHash;
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

    private Block(BlockBuilder builder) {
        this.number = builder.number;
        this.timestamp = builder.timestamp;
        this.nrgConsumed = builder.nrgConsumed;
        this.nrgLimit = builder.nrgLimit;
        this.bloom = builder.bloom;
        this.extraData = builder.extraData;
        this.solution = builder.solution;
        this.txHash = builder.txHash;
        this.hash = builder.hash;
        this.parentHash = builder.parentHash;
        this.nonce = builder.nonce;
        this.difficulty = builder.difficulty;
        this.minerAddress = builder.minerAddress;
        this.stateRoot = builder.stateRoot;
        this.txTrieRoot = builder.txTrieRoot;
        this.receiptTxRoot = builder.receiptTxRoot;
        this.size = builder.size;
        this.totalDifficulty = builder.totalDifficulty;
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

    public List<Hash256> getTxHash() {
        return txHash;
    }

    public int getSize() {
        return size;
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    @Override
    public String toString() {
        StringBuilder sb =
                new StringBuilder()
                        .append("logsBloom: ")
                        .append("0x")
                        .append(bloom.toString())
                        .append(",\n")
                        .append("totalDifficulty: ")
                        .append(totalDifficulty.toString())
                        .append(",\n")
                        .append("receiptsRoot: ")
                        .append("0x")
                        .append(receiptTxRoot.toString())
                        .append(",\n")
                        .append("extraData: ")
                        .append("0x")
                        .append(extraData.toString())
                        .append(",\n")
                        .append("nrgUsed: ")
                        .append(nrgConsumed)
                        .append(",\n")
                        .append("transactions: ")
                        .append(",\n")
                        .append("[")
                        .append("\n");

        int cnt = txHash.size();
        for (Hash256 h : txHash) {
            sb.append("  ").append("0x").append(h.toString());
            if (--cnt > 0) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("]").append(",\n");
        sb.append("nonce: ")
                .append("0x")
                .append(nonce.toString())
                .append(",\n")
                .append("miner: ")
                .append("0x")
                .append(minerAddress.toString())
                .append(",\n")
                .append("difficulty: ")
                .append(difficulty.toString())
                .append(",\n")
                .append("number: ")
                .append(number)
                .append(",\n")
                .append("nrgLimit: ")
                .append(nrgLimit)
                .append(",\n")
                .append("solution: ")
                .append("0x")
                .append(solution.toString())
                .append(",\n")
                .append("size: ")
                .append(size)
                .append(",\n")
                .append("transactionsRoot: ")
                .append("0x")
                .append(txTrieRoot.toString())
                .append(",\n")
                .append("stateRoot: ")
                .append("0x")
                .append(stateRoot.toString())
                .append(",\n")
                .append("parentHash: ")
                .append("0x")
                .append(parentHash.toString())
                .append(",\n")
                .append("hash: ")
                .append("0x")
                .append(hash.toString())
                .append(",\n")
                .append("timeStamp: ")
                .append(timestamp)
                .append("\n");

        return sb.toString();
    }

    /** This Builder class is used to build a {@link Block} instance. */
    public static class BlockBuilder {

        private long number;
        private long timestamp;
        private long nrgConsumed;
        private long nrgLimit;
        private ByteArrayWrapper bloom;
        private ByteArrayWrapper extraData;
        private ByteArrayWrapper solution;
        private List<Hash256> txHash;
        private Hash256 hash;
        private Hash256 parentHash;
        private BigInteger nonce;
        private BigInteger difficulty;
        private Address minerAddress;
        private Hash256 stateRoot;
        private Hash256 txTrieRoot;
        private Hash256 receiptTxRoot;
        private int size;
        private BigInteger totalDifficulty;

        public BlockBuilder() {}

        public BlockBuilder number(final long number) {
            this.number = number;
            return this;
        }

        public BlockBuilder timestamp(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BlockBuilder nrgConsumed(final long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public BlockBuilder nrgLimit(final long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public BlockBuilder bloom(final ByteArrayWrapper bloom) {
            this.bloom = bloom;
            return this;
        }

        public BlockBuilder extraData(final ByteArrayWrapper extraData) {
            this.extraData = extraData;
            return this;
        }

        public BlockBuilder solution(final ByteArrayWrapper solution) {
            this.solution = solution;
            return this;
        }

        public BlockBuilder txHash(final List<Hash256> txHash) {
            this.txHash = txHash;
            return this;
        }

        public BlockBuilder hash(final Hash256 hash) {
            this.hash = hash;
            return this;
        }

        public BlockBuilder parentHash(final Hash256 parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public BlockBuilder nonce(final BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public BlockBuilder difficulty(final BigInteger difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public BlockBuilder miner(final Address miner) {
            this.minerAddress = miner;
            return this;
        }

        public BlockBuilder stateRoot(final Hash256 stateRoot) {
            this.stateRoot = stateRoot;
            return this;
        }

        public BlockBuilder txTrieRoot(final Hash256 txTrieRoot) {
            this.txTrieRoot = txTrieRoot;
            return this;
        }

        public BlockBuilder receiptTxRoot(final Hash256 receiptTxRoot) {
            this.receiptTxRoot = receiptTxRoot;
            return this;
        }

        public BlockBuilder size(final int s) {
            this.size = s;
            return this;
        }

        public BlockBuilder totalDifficulty(final BigInteger td) {
            this.totalDifficulty = td;
            return this;
        }

        public Block createBlock() {

            if (bloom == null
                    || extraData == null
                    || solution == null
                    || txHash == null
                    || parentHash == null
                    || nonce == null
                    || difficulty == null
                    || minerAddress == null
                    || stateRoot == null
                    || txTrieRoot == null
                    || receiptTxRoot == null
                    || totalDifficulty == null) {
                throw new NullPointerException(
                        "bloom#"
                                + String.valueOf(bloom)
                                + " extraData#"
                                + String.valueOf(extraData)
                                + " solution#"
                                + String.valueOf(solution)
                                + " txHash#"
                                + String.valueOf(txHash)
                                + " parentHash#"
                                + String.valueOf(parentHash)
                                + " nonce#"
                                + String.valueOf(nonce)
                                + " difficulty#"
                                + String.valueOf(difficulty)
                                + " totalDifficulty#"
                                + String.valueOf(totalDifficulty)
                                + " minerAddress#"
                                + String.valueOf(minerAddress)
                                + " stateRoot#"
                                + String.valueOf(stateRoot)
                                + " txTrieRoot#"
                                + String.valueOf(txTrieRoot)
                                + " receiptTxRoot#"
                                + String.valueOf(receiptTxRoot));
            }

            if (number < 0 || timestamp < 0 || nrgConsumed < 0 || nrgLimit < 0 || size < 0) {
                throw new IllegalArgumentException(
                        "Block#"
                                + number
                                + " Time#"
                                + timestamp
                                + " NrgConsumed#"
                                + nrgConsumed
                                + " NrgLimit#"
                                + nrgLimit
                                + " size#"
                                + size);
            }

            return new Block(this);
        }
    }
}

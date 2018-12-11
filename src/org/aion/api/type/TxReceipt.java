package org.aion.api.type;

import java.util.List;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;

/**
 * TxReceipt class containing all relevant information to transaction receipts utilized by {@link
 * org.aion.api.ITx#getTxReceipt(Hash256) getTxReceipt}.
 */
public final class TxReceipt {

    private final int txIndex;
    private final long blockNumber;
    private final long nrgConsumed;
    private final long cumulativeNrgUsed;
    private final Hash256 blockHash;
    private final Hash256 txHash;
    private final Address from;
    private final Address to;
    private final Address contractAddress;
    private final List<TxLog> txLogs;

    private TxReceipt(TxReceiptBuilder builder) {
        this.txIndex = builder.txIndex;
        this.blockNumber = builder.blockNumber;
        this.nrgConsumed = builder.nrgConsumed;
        this.cumulativeNrgUsed = builder.cumulativeNrgUsed;
        this.blockHash = builder.blockHash;
        this.txHash = builder.txHash;
        this.from = builder.from;
        this.to = builder.to;
        this.contractAddress = builder.contractAddress;
        this.txLogs = builder.txLogs;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getNrgConsumed() {
        return nrgConsumed;
    }

    public long getCumulativeNrgUsed() {
        return cumulativeNrgUsed;
    }

    public Hash256 getBlockHash() {
        return blockHash;
    }

    public Hash256 getTxHash() {
        return txHash;
    }

    public Address getFrom() {
        return from;
    }

    public Address getTo() {
        return to;
    }

    public Address getContractAddress() {
        return contractAddress;
    }

    public List<TxLog> getTxLogs() {
        return txLogs;
    }

    @Override
    public String toString() {

        StringBuilder sb =
                new StringBuilder()
                        .append("txIndex: ")
                        .append(txIndex)
                        .append(",\n")
                        .append("blockNumber: ")
                        .append(blockNumber)
                        .append(",\n")
                        .append("nrg: ")
                        .append(nrgConsumed)
                        .append(",\n")
                        .append("nrgCumulativeUsed: ")
                        .append(cumulativeNrgUsed)
                        .append(",\n")
                        .append("blockHash: ")
                        .append("0x")
                        .append(blockHash.toString())
                        .append(",\n")
                        .append("txHash: ")
                        .append("0x")
                        .append(txHash.toString())
                        .append(",\n")
                        .append("from: ")
                        .append("0x")
                        .append(from.toString())
                        .append(",\n")
                        .append("to: ")
                        .append("0x")
                        .append(to.toString())
                        .append(",\n")
                        .append("contractAddress: ")
                        .append(contractAddress.toString())
                        .append(",\n")
                        .append("log: ")
                        .append("\n");

        int cnt = txLogs.size();
        for (TxLog tl : txLogs) {
            sb.append("[").append("\n");
            sb.append(tl.toString(1));
            sb.append("]");

            if (--cnt > 0) {
                sb.append(",");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /** This Builder class is used to build a {@link TxReceipt} instance. */
    public static class TxReceiptBuilder {

        private int txIndex;
        private long blockNumber;
        private long nrgConsumed;
        private long cumulativeNrgUsed;
        private Hash256 blockHash;
        private Hash256 txHash;
        private Address from;
        private Address to;
        private Address contractAddress;
        private List<TxLog> txLogs;

        public TxReceiptBuilder() {}

        public TxReceipt.TxReceiptBuilder txIndex(final int txIndex) {
            this.txIndex = txIndex;
            return this;
        }

        public TxReceipt.TxReceiptBuilder blockNumber(final long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public TxReceipt.TxReceiptBuilder nrgConsumed(final long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public TxReceipt.TxReceiptBuilder cumulativeNrgUsed(final long cumulativeNrgUsed) {
            this.cumulativeNrgUsed = cumulativeNrgUsed;
            return this;
        }

        public TxReceipt.TxReceiptBuilder blockHash(final Hash256 blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public TxReceipt.TxReceiptBuilder txHash(final Hash256 txHash) {
            this.txHash = txHash;
            return this;
        }

        public TxReceipt.TxReceiptBuilder from(final Address from) {
            this.from = from;
            return this;
        }

        public TxReceipt.TxReceiptBuilder to(final Address to) {
            this.to = to;
            return this;
        }

        public TxReceipt.TxReceiptBuilder contractAddress(final Address contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public TxReceipt.TxReceiptBuilder txLogs(final List<TxLog> txLogs) {
            this.txLogs = txLogs;
            return this;
        }

        public TxReceipt createTxReceipt() {
            if (blockHash == null
                    || txHash == null
                    || from == null
                    || to == null
                    || contractAddress == null
                    || txLogs == null) {

                throw new NullPointerException(
                        "TxHash#"
                                + String.valueOf(txHash)
                                + " BlockHash#"
                                + String.valueOf(blockHash)
                                + " From#"
                                + String.valueOf(from)
                                + " To#"
                                + String.valueOf(to)
                                + " ContractAddress#"
                                + String.valueOf(contractAddress)
                                + " TxLogs#"
                                + String.valueOf(txLogs));
            }

            if (txIndex < 0 || blockNumber < 0 || nrgConsumed < 0 || cumulativeNrgUsed < 0) {
                throw new IllegalArgumentException(
                        "TxIdx#"
                                + txIndex
                                + " Block#"
                                + blockNumber
                                + " NrgConsumed#"
                                + nrgConsumed
                                + " CumulativeNrg#"
                                + cumulativeNrgUsed);
            }

            return new TxReceipt(this);
        }
    }
}

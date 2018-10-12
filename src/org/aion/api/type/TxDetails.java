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

import java.math.BigInteger;
import java.util.List;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

/**
 * TxLog class containing all relevant information to transaction log utilized by {@link
 * org.aion.api.ITx#getTxReceipt(org.aion.base.type.Hash256) getTxReceipt}.
 *
 * @author Jay Tseng
 * @see org.aion.api.type.TxReceipt TxReceipt
 */

public final class TxDetails {

    private final Address from;
    private final Address to;
    private final Hash256 txHash;
    private final BigInteger value;
    private final BigInteger nonce;
    private final long nrgConsumed;
    private final long nrgPrice;
    private final ByteArrayWrapper data;
    private final List<TxLog> logs;
    private final int txIndex;
    private final Address contract;
    private final long timestamp;
    private final String error;

    private TxDetails(TxDetailsBuilder builder) {
        this.from = builder.from;
        this.to = builder.to;
        this.txHash = builder.txHash;
        this.value = builder.value;
        this.nonce = builder.nonce;
        this.nrgConsumed = builder.nrgConsumed;
        this.nrgPrice = builder.nrgPrice;
        this.data = builder.data;
        this.logs = builder.logs;
        this.txIndex = builder.txIndex;
        this.contract = builder.contract;
        this.timestamp = builder.timestamp;
        this.error = builder.error;
    }

    public Address getFrom() {
        return from;
    }

    public Address getTo() {
        return to;
    }

    public Address getContract() {
        return contract;
    }

    public Hash256 getTxHash() {
        return txHash;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public long getNrgConsumed() {
        return nrgConsumed;
    }

    public long getNrgPrice() {
        return nrgPrice;
    }

    public ByteArrayWrapper getData() {
        return data;
    }

    public List<TxLog> getLogs() {
        return logs;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int _level) {

        StringBuilder lv = new StringBuilder();
        int level = _level;
        while (level-- > 0) {
            lv.append("  ");
        }

        StringBuilder sb = new StringBuilder()
            .append(lv).append("nrgPrice: ").append(nrgPrice).append(",\n")
            .append(lv).append("nrg: ").append(nrgConsumed).append(",\n")
            .append(lv).append("transactionIndex: ").append(txIndex).append(",\n")
            .append(lv).append("nonce: ").append(nonce.toString()).append(",\n")
            .append(lv).append("input: ").append("0x").append(data.toString()).append(",\n")
            .append(lv).append("from: ").append("0x").append(from.toString()).append(",\n")
            .append(lv).append("to: ").append("0x").append(to.toString()).append(",\n")
            .append(lv).append("value: ").append(value.toString()).append(",\n")
            .append(lv).append("hash: ").append("0x").append(txHash.toString()).append(",\n")
            .append(lv).append("timestamp: ").append(timestamp).append(",\n")
            .append(lv).append("error: ").append(error).append(",\n")
            .append(lv).append("log: ").append("\n");

        int cnt = logs.size();
        for (TxLog tl : logs) {
            sb.append(lv).append("[").append("\n");
            sb.append(lv).append(tl.toString(++_level));
            sb.append(lv).append("]");

            if (--cnt > 0) {
                sb.append(",");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * This Builder class is used to build a {@link TxDetails} instance.
     */
    public static class TxDetailsBuilder {

        private Address from;
        private Address to;
        private Address contract;
        private Hash256 txHash;
        private BigInteger value;
        private BigInteger nonce;
        private long nrgConsumed;
        private long nrgPrice;
        private ByteArrayWrapper data;
        private List<TxLog> logs;
        private int txIndex;
        private long timestamp;
        private String error;

        public TxDetailsBuilder() {
        }

        public TxDetailsBuilder from(final Address from) {
            this.from = from;
            return this;
        }

        public TxDetailsBuilder to(final Address to) {
            this.to = to;
            return this;
        }

        public TxDetailsBuilder txHash(final Hash256 txHash) {
            this.txHash = txHash;
            return this;
        }

        public TxDetailsBuilder value(final BigInteger value) {
            this.value = value;
            return this;
        }

        public TxDetailsBuilder nonce(final BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public TxDetailsBuilder nrgConsumed(final long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public TxDetailsBuilder nrgPrice(final long nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public TxDetailsBuilder data(final ByteArrayWrapper data) {
            this.data = data;
            return this;
        }

        public TxDetailsBuilder logs(final List<TxLog> logs) {
            this.logs = logs;
            return this;
        }

        public TxDetailsBuilder txIndex(final int txIndex) {
            this.txIndex = txIndex;
            return this;
        }

        public TxDetailsBuilder contract(final Address contract) {
            this.contract = contract;
            return this;
        }

        public TxDetailsBuilder timestamp(final long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TxDetailsBuilder error(final String error) {
            this.error = error;
            return this;
        }


        public TxDetails createTxDetails() {
            if (from == null || to == null || contract == null || txHash == null || value == null
                || nonce == null || data == null) {
                throw new NullPointerException(
                    "From#" + String.valueOf(from) +
                        " To#" + String.valueOf(to) +
                        " Contract#" + String.valueOf(contract) +
                        " Hash#" + String.valueOf(txHash) +
                        " Value#" + String.valueOf(value) +
                        " Nonce#" + String.valueOf(nonce) +
                        " Data#" + String.valueOf(data) +
                        " Logs#" + String.valueOf(logs));
            }

            if (nrgConsumed < 0 || nrgPrice < 0 || txIndex < 0) {
                throw new IllegalArgumentException(
                    "NrgConsumed#" + nrgConsumed +
                        " TxIndex#" + String.valueOf(txIndex) +
                        " NrgPrice#" + nrgPrice);
            }

            return new TxDetails(this);
        }
    }
}

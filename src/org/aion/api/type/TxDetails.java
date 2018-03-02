/*******************************************************************************
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
 *
 ******************************************************************************/

package org.aion.api.type;

import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

import java.math.BigInteger;
import java.util.List;

/**
 * TxLog class containing all relevant information to transaction log utilized by
 * {@link org.aion.api.ITx#getTxReceipt(org.aion.base.type.Hash256) getTxReceipt}.
 *
 * @see org.aion.api.type.TxReceipt TxReceipt
 *
 * @author Jay Tseng
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

        public TxDetailsBuilder(){
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

        public TxDetails createTxDetails() {
            if (from == null || to == null || contract == null || txHash == null || value == null || nonce == null || data == null) {
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

            if (nrgConsumed < 0 || nrgPrice < 1 || txIndex < 0) {
                throw new IllegalArgumentException(
                        "NrgConsumed#" + nrgConsumed +
                        " TxIndex#" + String.valueOf(txIndex) +
                        " NrgPrice#" + nrgPrice);
            }

            return new TxDetails(this);
        }
    }
}

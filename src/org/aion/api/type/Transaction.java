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
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

/**
 * The Transaction return data structure, typically used in Transaction getter API.
 *
 * @author Jay Tseng
 * @see org.aion.api.IChain#getTransactionByBlockHashAndIndex(org.aion.base.type.Hash256, int)
 * getTransactionByBlockHashAndIndex
 * @see org.aion.api.IChain#getTransactionByBlockNumberAndIndex(long, int)
 * GetTransactionByBlockNumberAndIndex
 * @see org.aion.api.IChain#getTransactionByHash(org.aion.base.type.Hash256) getTransactionByHash
 */

public final class Transaction {

    private final int transactionIndex;
    private final long blockNumber;
    private final long timeStamp;
    private final long nrgConsumed;
    private final long nrgPrice;
    private final Address from;
    private final Address to;
    private final Hash256 blockHash;
    private final Hash256 txHash;
    private final BigInteger nonce;
    private final BigInteger value;
    private final ByteArrayWrapper data;

    private Transaction(TransactionBuilder builder) {
        this.transactionIndex = builder.transactionIndex;
        this.blockNumber = builder.blockNumber;
        this.timeStamp = builder.timeStamp;
        this.nrgConsumed = builder.nrgConsumed;
        this.nrgPrice = builder.nrgPrice;
        this.from = builder.from;
        this.to = builder.to;
        this.blockHash = builder.blockHash;
        this.txHash = builder.txHash;
        this.nonce = builder.nonce;
        this.value = builder.value;
        this.data = builder.data;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getNrgConsumed() {
        return nrgConsumed;
    }

    public long getNrgPrice() {
        return nrgPrice;
    }

    public Address getFrom() {
        return from;
    }

    public Address getTo() {
        return to;
    }

    public Hash256 getBlockHash() {
        return blockHash;
    }

    public Hash256 getTxHash() {
        return txHash;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getValue() {
        return value;
    }

    public ByteArrayWrapper getData() {
        return data;
    }

    @Override
    public String toString() {
        return "nrgPrice: " + nrgPrice + ",\n"
            + "nrg: " + nrgConsumed + ",\n"
            + "nonce: " + nonce.toString() + ",\n"
            + "transactionIndex: " + transactionIndex + ",\n"
            + "input: " + "0x" + data.toString() + ",\n"
            + "blockNumber: " + blockNumber + ",\n"
            + "from: " + "0x" + from.toString() + ",\n"
            + "to: " + "0x" + to.toString() + ",\n"
            + "value: " + value.toString() + ",\n"
            + "hash: " + "0x" + txHash.toString() + ",\n"
            + "timestamp: " + timeStamp + "\n";
    }

    /**
     * This Builder class is used to build a {@link Transaction} instance.
     */
    public static class TransactionBuilder {

        private int transactionIndex;
        private long blockNumber;
        private long timeStamp;
        private long nrgConsumed;
        private long nrgPrice;
        private Address from;
        private Address to;
        private Hash256 blockHash;
        private Hash256 txHash;
        private BigInteger nonce;
        private BigInteger value;
        private ByteArrayWrapper data;

        public TransactionBuilder() {
        }

        public Transaction.TransactionBuilder transactionIndex(final int transactionIndex) {
            this.transactionIndex = transactionIndex;
            return this;
        }

        public Transaction.TransactionBuilder blockNumber(final long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public Transaction.TransactionBuilder timeStamp(final long timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public Transaction.TransactionBuilder nrgConsumed(final long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public Transaction.TransactionBuilder nrgPrice(final long nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public Transaction.TransactionBuilder from(final Address from) {
            this.from = from;
            return this;
        }

        public Transaction.TransactionBuilder to(final Address to) {
            this.to = to;
            return this;
        }

        public Transaction.TransactionBuilder blockHash(final Hash256 blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public Transaction.TransactionBuilder txHash(final Hash256 txHash) {
            this.txHash = txHash;
            return this;
        }

        public Transaction.TransactionBuilder nonce(final BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public Transaction.TransactionBuilder value(final BigInteger value) {
            this.value = value;
            return this;
        }

        public Transaction.TransactionBuilder data(final ByteArrayWrapper data) {
            this.data = data;
            return this;
        }


        public Transaction createTransaction() {

            if (from == null || to == null || blockHash == null || txHash == null ||
                nonce == null || value == null || data == null) {
                throw new NullPointerException(
                    "From#" + String.valueOf(from) +
                        " To#" + String.valueOf(to) +
                        " BlockHash#" + String.valueOf(blockHash) +
                        " TxHash#" + String.valueOf(txHash) +
                        " Value#" + String.valueOf(value) +
                        " Data#" + String.valueOf(data) +
                        " Nonce#" + String.valueOf(nonce));
            }

            if (transactionIndex < 0 || blockNumber < 0 || timeStamp < 0 || nrgConsumed < 0
                || nrgPrice < 0) {
                throw new IllegalArgumentException(
                    "TxIndex#" + transactionIndex +
                        " BlockNumber#" + blockNumber +
                        " TimeStamp#" + timeStamp +
                        " NrgConsumed#" + nrgConsumed +
                        " NrgPrice#" + nrgPrice);
            }

            return new Transaction(this);
        }
    }
}

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
import org.aion.base.util.ByteArrayWrapper;

import java.math.BigInteger;

/**
 * The transaction input arguments used in
 * {@link org.aion.api.ITx#sendTransaction(org.aion.api.type.TxArgs) sendTransaction} and
 * {@link org.aion.api.ITx#call(org.aion.api.type.TxArgs) call}.
 */

public final class TxArgs {
    private final Address from;
    private final Address to;
    private final BigInteger value;
    private final BigInteger nonce;
    private final long nrgLimit;
    private final long nrgPrice;
    private final ByteArrayWrapper data;

    private TxArgs(TxArgsBuilder builder) {
        this.from = builder.from;
        this.to = builder.to;
        this.value = builder.value;
        this.nonce = builder.nonce;
        this.nrgLimit = builder.nrgLimit;
        this.nrgPrice = builder.nrgPrice;
        this.data = builder.data;
    }

    public Address getFrom() {
        return from;
    }

    public Address getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public long getNrgLimit() {
        return nrgLimit;
    }

    public long getNrgPrice() {
        return nrgPrice;
    }

    public ByteArrayWrapper getData() {
        return data;
    }

    /**
     * This Builder class is used to build a {@link TxArgs} instance.
     */
    public static class TxArgsBuilder {
        private Address from;
        private Address to;
        private BigInteger value;
        private BigInteger nonce;
        private long nrgLimit;
        private long nrgPrice;
        private ByteArrayWrapper data;

        public TxArgsBuilder(){
        }

        public TxArgs.TxArgsBuilder from(final Address from) {
            this.from = from;
            return this;
        }

        public TxArgs.TxArgsBuilder to(final Address to) {
            this.to = to;
            return this;
        }

        public TxArgs.TxArgsBuilder value(final BigInteger value) {
            this.value = value;
            return this;
        }

        public TxArgs.TxArgsBuilder nonce(final BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public TxArgs.TxArgsBuilder nrgLimit(final long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public TxArgs.TxArgsBuilder nrgPrice(final long nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public TxArgs.TxArgsBuilder data(final ByteArrayWrapper data) {
            this.data = data;
            return this;
        }


        public TxArgs createTxArgs() {
            if (from == null || to == null || value == null || nonce == null || data == null) {
                throw new NullPointerException(
                        "From#" + String.valueOf(from) +
                        " To#" + String.valueOf(to) +
                        " Value#" + String.valueOf(value) +
                        " Nonce#" + String.valueOf(nonce) +
                        " Data#" + String.valueOf(data));
            }

            if (nrgLimit < 0 || nrgPrice < 1) {
                throw new IllegalArgumentException(
                        "NrgLimit#" + nrgLimit +
                        " NrgPrice#" + nrgPrice);
            }

            return new TxArgs(this);
        }
    }
}

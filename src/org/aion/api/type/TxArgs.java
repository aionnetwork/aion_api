package org.aion.api.type;

import static org.aion.api.ITx.NRG_LIMIT_TX_MIN;
import static org.aion.api.ITx.NRG_PRICE_MIN;

import java.math.BigInteger;
import org.aion.api.ITx;
import org.aion.base.type.AionAddress;
import org.aion.base.util.ByteArrayWrapper;

/**
 * The transaction input arguments used in {@link
 * org.aion.api.ITx#sendTransaction(org.aion.api.type.TxArgs) sendTransaction} and {@link
 * org.aion.api.ITx#call(org.aion.api.type.TxArgs) call}.
 */
public final class TxArgs {

    private final AionAddress from;
    private final AionAddress to;
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

    public AionAddress getFrom() {
        return from;
    }

    public AionAddress getTo() {
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

    /** This Builder class is used to build a {@link TxArgs} instance. */
    public static class TxArgsBuilder {

        private AionAddress from;
        private AionAddress to;
        private BigInteger value;
        private BigInteger nonce;
        private long nrgLimit;
        private long nrgPrice;
        private ByteArrayWrapper data;

        public TxArgsBuilder() {}

        public TxArgs.TxArgsBuilder from(final AionAddress from) {
            this.from = from;
            return this;
        }

        public TxArgs.TxArgsBuilder to(final AionAddress to) {
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

            if (value == null) {
                value = BigInteger.ZERO;
            }

            if (nonce == null) {
                nonce = BigInteger.ZERO;
            }

            if (data == null) {
                data = ByteArrayWrapper.wrap(new byte[0]);
            }

            if (from == null) {
                from = AionAddress.EMPTY_ADDRESS();
            }

            if (to == null) {
                to = AionAddress.EMPTY_ADDRESS();
            }

            if (nrgLimit == 0) {
                nrgLimit =
                        to.equals(AionAddress.EMPTY_ADDRESS())
                                ? ITx.NRG_LIMIT_CONTRACT_CREATE_MAX
                                : ITx.NRG_LIMIT_TX_MAX;
            }

            if (nrgPrice == 0) {
                nrgPrice = NRG_PRICE_MIN;
            }

            if (nrgLimit < NRG_LIMIT_TX_MIN || nrgPrice < NRG_PRICE_MIN) {
                throw new IllegalArgumentException(
                        "NrgLimit#" + nrgLimit + " NrgPrice#" + nrgPrice);
            }

            return new TxArgs(this);
        }
    }
}

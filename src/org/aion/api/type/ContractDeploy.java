package org.aion.api.type;

import java.math.BigInteger;
import org.aion.api.ITx;
import org.aion.base.type.AionAddress;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.Bytesable;

/**
 * The helper class for gather all of the deploy arguments to deploy the contract. Use the builder
 * class to create this helper class.
 *
 * @author Jay Tseng
 * @see org.aion.api.ITx#contractDeploy contractDeploy
 * @see org.aion.api.type.CompileResponse CompileResponse
 */
public final class ContractDeploy {

    private final CompileResponse cr;
    private final AionAddress from;
    private final boolean constructor;
    private final ByteArrayWrapper data;
    private final long nrgLimit;
    private final long nrgPrice;
    private final BigInteger value;

    private ContractDeploy(ContractDeployBuilder builder) {
        this.cr = builder.cr;
        this.from = builder.from;
        this.constructor = builder.constructor;
        this.data = builder.data;
        this.nrgLimit = builder.nrgLimit;
        this.nrgPrice = builder.nrgPrice;
        this.value = builder.value;
    }

    public AionAddress getFrom() {
        return from;
    }

    public long getNrgLimit() {
        return nrgLimit;
    }

    public long getNrgPrice() {
        return nrgPrice;
    }

    public BigInteger getValue() {
        return value;
    }

    public CompileResponse getCompileResponse() {
        return cr;
    }

    public ByteArrayWrapper getData() {
        return data;
    }

    public boolean isConstructor() {
        return constructor;
    }

    public static class ContractDeployBuilder {

        private CompileResponse cr;
        private AionAddress from;
        private boolean constructor;
        private ByteArrayWrapper data;
        private long nrgLimit;
        private long nrgPrice;
        private BigInteger value;

        public ContractDeployBuilder() {}

        public ContractDeploy createContractDeploy() {
            if (cr == null) {
                throw new NullPointerException("CompileResponse is null");
            }

            if (value == null) {
                value = BigInteger.ZERO;
            }

            if (data == null) {
                data = ByteArrayWrapper.wrap(Bytesable.NULL_BYTE);
            }

            if (nrgLimit == 0) {
                nrgLimit = ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
            }

            if (nrgPrice == 0) {
                nrgPrice = ITx.NRG_PRICE_MIN;
            }

            if (nrgLimit < ITx.NRG_LIMIT_TX_MIN || nrgPrice < ITx.NRG_PRICE_MIN) {
                throw new IllegalArgumentException(
                        "nrgConsumed#" + nrgLimit + " nrgPrice#" + nrgPrice);
            }

            return new ContractDeploy(this);
        }

        public ContractDeployBuilder compileResponse(CompileResponse cr) {
            this.cr = cr;
            return this;
        }

        public ContractDeployBuilder from(AionAddress from) {
            this.from = from;
            return this;
        }

        public ContractDeployBuilder constructor(boolean constructor) {
            this.constructor = constructor;
            return this;
        }

        public ContractDeployBuilder data(ByteArrayWrapper data) {
            this.data = data;
            return this;
        }

        public ContractDeployBuilder nrgLimit(long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public ContractDeployBuilder nrgPrice(long nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public ContractDeployBuilder value(BigInteger value) {
            this.value = value;
            return this;
        }
    }
}

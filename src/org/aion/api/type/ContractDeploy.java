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
 * The helper class for gather all of the deploy arguments to deploy the contract. Use the builder class to create this
 * helper class.
 *
 * @see org.aion.api.ITx#contractDeploy contractDeploy
 * @see org.aion.api.type.CompileResponse CompileResponse
 *
 * @author Jay Tseng
 */

public final class ContractDeploy {
    private final CompileResponse cr;
    private final Address from;
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

    public Address getFrom() {
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
        private Address from;
        private boolean constructor;
        private ByteArrayWrapper data;
        private long nrgLimit;
        private long nrgPrice;
        private BigInteger value;

        public ContractDeployBuilder() {}

        public ContractDeploy createContractDeploy() {
            if (cr == null || from == null || data == null || value == null) {
                throw new NullPointerException(
                        "CompileResponse#" + String.valueOf(cr) +
                                " from#" + String.valueOf(from) +
                                " data#" + String.valueOf(data) +
                                " value#" + String.valueOf(value));
            }

            if (nrgLimit < 0 || nrgPrice < 1) {
                throw new IllegalArgumentException("nrgConsumed#" + nrgLimit + " nrgPrice#" + nrgPrice);
            }

            return new ContractDeploy(this);
        }

        public ContractDeployBuilder compileResponse(CompileResponse cr) {
            this.cr = cr;
            return this;
        }

        public ContractDeployBuilder from(Address from) {
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

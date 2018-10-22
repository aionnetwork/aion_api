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

/**
 * AccountDetails class containing all relevant information identifying an account
 *
 * @author Ali Sharif
 */
public final class AccountDetails {

    private final Address address;
    private final BigInteger balance;

    private AccountDetails(AccountDetailsBuilder builder) {
        this.address = builder.address;
        this.balance = builder.balance;
    }

    public Address getAddress() {
        return address;
    }

    public BigInteger getBalance() {
        return balance;
    }

    /** This Builder class is used to build a {@link AccountDetails } instance. */
    public static class AccountDetailsBuilder {

        private Address address;
        private BigInteger balance;

        public AccountDetailsBuilder() {}

        public AccountDetailsBuilder address(final Address address) {
            this.address = address;
            return this;
        }

        public AccountDetailsBuilder balance(final BigInteger balance) {
            this.balance = balance;
            return this;
        }

        public AccountDetails createAccountDetails() {
            if (address == null || balance == null) {
                throw new NullPointerException(
                        "Address#"
                                + String.valueOf(address)
                                + " Balance#"
                                + String.valueOf(balance));
            }

            return new AccountDetails(this);
        }
    }
}

package org.aion.api.type;

import java.math.BigInteger;
import org.aion.base.type.AionAddress;

/**
 * AccountDetails class containing all relevant information identifying an account
 *
 * @author Ali Sharif
 */
public final class AccountDetails {

    private final AionAddress address;
    private final BigInteger balance;

    private AccountDetails(AccountDetailsBuilder builder) {
        this.address = builder.address;
        this.balance = builder.balance;
    }

    public AionAddress getAddress() {
        return address;
    }

    public BigInteger getBalance() {
        return balance;
    }

    /** This Builder class is used to build a {@link AccountDetails } instance. */
    public static class AccountDetailsBuilder {

        private AionAddress address;
        private BigInteger balance;

        public AccountDetailsBuilder() {}

        public AccountDetailsBuilder address(final AionAddress address) {
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

package org.aion.api.type;

import java.util.List;
import org.aion.base.type.AionAddress;

/**
 * The helper class for gather all of the contract event filter conditions. Use the builder class to
 * create this helper class.
 *
 * @author Jay Tseng
 * @see org.aion.api.IContract#register(org.aion.api.type.ContractEventFilter) register
 */
public final class ContractEventFilter {

    private final String fromBlock;
    private final String toBlock;
    private final List<AionAddress> addresses;
    private final List<String> topics;
    private final long expireTime; // 0 means no expire. time unit is sec.

    private ContractEventFilter(ContractEventFilterBuilder builder) {
        this.fromBlock = builder.fromBlock;
        this.toBlock = builder.toBlock;
        this.addresses = builder.addresses;
        this.topics = builder.topics;
        this.expireTime = builder.expireTime;
    }

    public String getFromBlock() {
        return fromBlock;
    }

    public String getToBlock() {
        return toBlock;
    }

    public List<AionAddress> getAddresses() {
        return addresses;
    }

    public List<String> getTopics() {
        return topics;
    }

    public long getExpireTime() {
        return expireTime;
    }

    /** This Builder class is used to build a {@link ContractEventFilter} instance. */
    public static class ContractEventFilterBuilder {

        private String fromBlock;
        private String toBlock;
        private List<AionAddress> addresses;
        private List<String> topics;
        private long expireTime;

        public ContractEventFilterBuilder() {}

        public ContractEventFilterBuilder(ContractEventFilter ef) {
            this.fromBlock = ef.fromBlock;
            this.toBlock = ef.toBlock;
            this.addresses = ef.addresses;
            this.expireTime = ef.expireTime;
            this.topics = ef.topics;
        }

        public ContractEventFilter.ContractEventFilterBuilder fromBlock(final String fromBlock) {
            this.fromBlock = fromBlock;
            return this;
        }

        public ContractEventFilter.ContractEventFilterBuilder toBlock(final String toBlock) {
            this.toBlock = toBlock;
            return this;
        }

        public ContractEventFilter.ContractEventFilterBuilder addresses(
                final List<AionAddress> addresses) {
            this.addresses = addresses;
            return this;
        }

        public ContractEventFilter.ContractEventFilterBuilder topics(final List<String> topics) {
            this.topics = topics;
            return this;
        }

        public ContractEventFilter.ContractEventFilterBuilder expireTime(final long expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public ContractEventFilter createContractEventFilter() {
            if (fromBlock == null || toBlock == null || addresses == null || topics == null) {
                throw new NullPointerException(
                        "fromBlock#"
                                + String.valueOf(fromBlock)
                                + " toBlock#"
                                + String.valueOf(toBlock)
                                + " addresses#"
                                + String.valueOf(addresses)
                                + " topics#"
                                + String.valueOf(topics));
            }

            if (expireTime < 0) {
                throw new IllegalArgumentException("expireTime#" + expireTime);
            }

            return new ContractEventFilter(this);
        }
    }
}

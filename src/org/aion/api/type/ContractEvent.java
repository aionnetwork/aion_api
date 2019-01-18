package org.aion.api.type;

import java.util.List;
import org.aion.base.type.AionAddress;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;

/**
 * The helper class for gather all of the contract event information. Use the builder class to
 * create this helper class.
 *
 * @see org.aion.api.IContract#getEvents() getEvents
 * @see org.aion.api.type.CompileResponse CompileResponse
 * @author Jay Tseng
 */
public final class ContractEvent extends Event {
    private final AionAddress address;
    private final Hash256 blockHash;
    private final Hash256 txHash;
    private final ByteArrayWrapper data;
    private final long blockNumber;
    private final int logIndex;
    private final String eventName;
    private final boolean removed;
    private final int txIndex;

    // The object type defined by the abi of the contract function. Therefore, the developer need to
    // cast it to the correct
    // Solidity type like IString, IBool, IAddress, etc.
    private final List<Object> results;

    private ContractEvent(ContractEventBuilder builder) {
        super(type.CONTRACT);
        this.address = builder.address;
        this.blockHash = builder.blockHash;
        this.txHash = builder.txHash;
        this.data = builder.data;
        this.blockNumber = builder.blockNumber;
        this.logIndex = builder.logIndex;
        this.eventName = builder.eventName;
        this.removed = builder.removed;
        this.txIndex = builder.txIndex;
        this.results = builder.results;
    }

    public AionAddress getAddress() {
        return address;
    }

    public Hash256 getBlockHash() {
        return blockHash;
    }

    public Hash256 getTxHash() {
        return txHash;
    }

    public ByteArrayWrapper getData() {
        return data;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public int getLogIndex() {
        return logIndex;
    }

    public String getEventName() {
        return eventName;
    }

    public boolean isRemoved() {
        return removed;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public List<Object> getResults() {
        return results;
    }

    /** This Builder class is used to build a {@link ContractEvent} instance. */
    public static class ContractEventBuilder {
        private AionAddress address;
        private Hash256 blockHash;
        private Hash256 txHash;
        private ByteArrayWrapper data;
        private long blockNumber;
        private int logIndex;
        private String eventName;
        private boolean removed;
        private int txIndex;
        private List<Object> results;

        public ContractEventBuilder(ContractEvent eventCopy) {
            if (eventCopy == null) {
                throw new NullPointerException();
            }

            this.address = eventCopy.getAddress();
            this.blockHash = eventCopy.getBlockHash();
            this.txHash = eventCopy.getTxHash();
            this.data = eventCopy.data;
            this.blockNumber = eventCopy.blockNumber;
            this.logIndex = eventCopy.logIndex;
            this.eventName = eventCopy.eventName;
            this.removed = eventCopy.removed;
            this.txIndex = eventCopy.txIndex;
            this.results = eventCopy.results;
        }

        public ContractEventBuilder() {}

        public ContractEvent.ContractEventBuilder address(final AionAddress address) {
            this.address = address;
            return this;
        }

        public ContractEvent.ContractEventBuilder blockHash(final Hash256 blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public ContractEvent.ContractEventBuilder txHash(final Hash256 txHash) {
            this.txHash = txHash;
            return this;
        }

        public ContractEvent.ContractEventBuilder data(final ByteArrayWrapper data) {
            this.data = data;
            return this;
        }

        public ContractEvent.ContractEventBuilder blockNumber(final long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public ContractEvent.ContractEventBuilder logIndex(final int logIndex) {
            this.logIndex = logIndex;
            return this;
        }

        public ContractEvent.ContractEventBuilder eventName(final String eventName) {
            this.eventName = eventName;
            return this;
        }

        public ContractEvent.ContractEventBuilder removed(final boolean removed) {
            this.removed = removed;
            return this;
        }

        public ContractEvent.ContractEventBuilder txIndex(final int txIndex) {
            this.txIndex = txIndex;
            return this;
        }

        public ContractEvent.ContractEventBuilder results(final List<Object> results) {
            this.results = results;
            return this;
        }

        public ContractEvent createContractEvent() {

            if (address == null
                    || blockHash == null
                    || txHash == null
                    || data == null
                    || eventName == null) {
                throw new NullPointerException(
                        "address#"
                                + String.valueOf(address)
                                + " blockHash#"
                                + String.valueOf(blockHash)
                                + " txHash#"
                                + String.valueOf(txHash)
                                + " data#"
                                + String.valueOf(data)
                                + " eventName#"
                                + String.valueOf(eventName));
            }

            if (blockNumber < 0 || logIndex < 0 || txIndex < 0) {
                throw new IllegalArgumentException(
                        "blockNumber#"
                                + blockNumber
                                + " logIndex#"
                                + logIndex
                                + " txIndex#"
                                + txIndex);
            }

            return new ContractEvent(this);
        }
    }
}

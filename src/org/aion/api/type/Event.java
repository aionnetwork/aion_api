package org.aion.api.type;

/**
 * The abstract class for the class {@link org.aion.api.type.ContractEvent ContractEvent} defines
 * the event type.
 *
 * @author Jay Tseng
 */
public abstract class Event {

    private final type evType;

    Event(type t) {
        this.evType = t;
    }

    public type getType() {
        return this.evType;
    }

    enum type {
        CONTRACT,
        BLOCK,
        TX
    }
}

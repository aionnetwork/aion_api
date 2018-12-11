package org.aion.api;

import org.aion.api.type.ApiMsg;

/**
 * This interface provides methods for fetching the Aion blockchain network status and the specific
 * information.
 *
 * @author Jay Tseng
 */
public interface INet {

    /**
     * Check connecting node syncing detailed information.
     *
     * @return the class {@link org.aion.api.type.SyncInfo } containing all relevant information
     *     wrapped into ApiMsg. You can retrieve through this method {@link ApiMsg#getObject()
     *     getObject}.
     */
    ApiMsg syncInfo();

    /**
     * Check connected node's sync status.
     *
     * @return the boolean value wrapped into ApiMsg. You can retrieve through this method {@link
     *     ApiMsg#getObject() getObject}.
     */
    ApiMsg isSyncing();

    /**
     * Returns the current version of the Aion modules.
     *
     * @return the class {@link org.aion.api.type.Protocol Protocol} wrapped into ApiMsg. You can
     *     retrieve through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getProtocolVersion();

    /**
     * Returns the current active connected nodes.
     *
     * @return the interface {@link java.util.List } of the class {@link org.aion.api.type.Node}
     *     wrapped into ApiMsg. You can retrieve through this method {@link ApiMsg#getObject()
     *     getObject}.
     */
    ApiMsg getActiveNodes();

    /**
     * Returns the whole consensus network nodes list.
     *
     * @return the interface {@link java.util.List } of the class {@link java.lang.String } wrapped
     *     into ApiMsg. You can retrieve through this method {@link ApiMsg#getObject() getObject}.
     */
    ApiMsg getStaticNodes();

    /**
     * Returns a boolean value if the client is actively listening for network connections.
     *
     * @return the boolean value wrapped into ApiMsg. You can retrieve through this method {@link
     *     ApiMsg#getObject() getObject}.
     */
    ApiMsg isListening();

    /**
     * Returns a int value the connecting peers number of the connecting kernel.
     *
     * @return the int value wrapped into ApiMsg. You can retrieve through this method {@link
     *     ApiMsg#getObject() getObject}.
     */
    ApiMsg getPeerCount();
}

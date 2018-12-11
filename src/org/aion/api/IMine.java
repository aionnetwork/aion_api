package org.aion.api;

import org.aion.api.type.ApiMsg;

/**
 * This interface provides methods for fetching the miner statistics such as mining and Hashrate.
 *
 * @author Jay Tseng
 */
public interface IMine {

    /**
     * Check connecting node isMining status.
     *
     * @return boolean value of isMining status.
     */
    ApiMsg isMining();
}

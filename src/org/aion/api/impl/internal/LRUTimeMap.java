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

package org.aion.api.impl.internal;

/**
 * Created by Jay Tseng on 23/05/17.
 */

import org.aion.api.IUtils;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simple LRU map used for reusing lookup values.
 */
public class LRUTimeMap<K, V> extends LRUMap<K, V> {

    private static final long serialVersionUID = 1000000001L;
    protected final int defaultTimeout;
    private final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.EXE.name());
    private final Map<Long, K> timemap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final ScheduledExecutorService timeoutChecker = Executors.newSingleThreadScheduledExecutor();

    public LRUTimeMap(int maxEntries) {
        super(maxEntries);
        this.defaultTimeout = 180_000;
        runChecker();
    }

    private void runChecker() {
        this.timeoutChecker.scheduleWithFixedDelay(() -> {
            check();
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void check() {

        long current = System.currentTimeMillis();
        List<Long> timeList = new ArrayList<>();
        Iterator it = timemap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if ((long) entry.getKey() < current) {
                timeList.add((long) entry.getKey());
            } else {
                break;
            }
        }

        for (Long l : timeList) {
            K k = timemap.get(l);
            this.remove(k);
            timemap.remove(l);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("remove timeout msg - [{}]", IUtils.bytes2Hex(((ByteBuffer) k).array()));
            }
        }
    }

    public LRUTimeMap(int maxEntries, int timeout) {
        super(maxEntries);
        this.defaultTimeout = timeout;
        runChecker();
    }

    @Override
    public V put(K key, V val) {
        return put(key, val, defaultTimeout);
    }

    public V put(K key, V val, int timeout) {
        Object obj = super.put(key, val);
        timemap.put(System.currentTimeMillis() + timeout, key);
        return (V) obj;
    }

    @Override
    public void clear() {
        super.clear();
        this.timemap.clear();
    }
}
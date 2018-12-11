package org.aion.api.impl.internal;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.aion.api.IUtils;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;

/** Created by Jay Tseng on 23/05/17. */

/** Simple LRU map used for reusing lookup values. */
public class LRUTimeMap<K, V> extends LRUMap<K, V> {

    private static final long serialVersionUID = 1000000001L;
    private final int timeout;
    private final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.EXE.name());
    private final Map<Long, K> timeMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final ScheduledExecutorService timeoutChecker =
            Executors.newSingleThreadScheduledExecutor();

    public LRUTimeMap(int maxEntries) {
        super(maxEntries);
        this.timeout = 180_000;
        runChecker();
    }

    public LRUTimeMap(int maxEntries, int timeout) {
        super(maxEntries);
        this.timeout = timeout;
        runChecker();
    }

    private void runChecker() {
        this.timeoutChecker.scheduleWithFixedDelay(this::check, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void check() {

        long current = System.currentTimeMillis();
        List<Long> timeList = new ArrayList<>();
        for (Object o : timeMap.entrySet()) {
            Entry entry = (Entry) o;
            if ((long) entry.getKey() < current) {
                timeList.add((long) entry.getKey());
            } else {
                break;
            }
        }

        for (Long l : timeList) {
            K k = timeMap.get(l);
            this.remove(k);
            timeMap.remove(l);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "remove timeout msg - [{}]", IUtils.bytes2Hex(((ByteBuffer) k).array()));
            }
        }
    }

    @Override
    public V put(K key, V val) {
        Object obj = super.put(key, val);
        timeMap.put(System.currentTimeMillis() + timeout, key);
        return (V) obj;
    }

    @Override
    public void clear() {
        super.clear();
        this.timeMap.clear();
        if (timeoutChecker != null) {
            timeoutChecker.shutdown();
        }
    }
}

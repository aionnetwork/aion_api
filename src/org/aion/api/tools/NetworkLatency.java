package org.aion.api.tools;

import java.util.ArrayList;

/** Created by jay on 10/12/16. */
public class NetworkLatency {
    private ArrayList<Long> start;
    private ArrayList<Long> end;

    public NetworkLatency() {
        start = new ArrayList<>();
        end = new ArrayList<>();
    }

    public NetworkLatency(int arrSize) {
        start = new ArrayList<>();
        start.ensureCapacity(arrSize);
        end = new ArrayList<>();
        end.ensureCapacity(arrSize);
    }

    public void addStart(Long l) {
        start.add(l);
    }

    public void addEnd(long l) {
        end.add(l);
    }

    public void clear() {
        start.clear();
        end.clear();
    }

    public Long diff(int i) {
        if (i < start.size() && i < end.size()) {
            return end.get(i) - start.get(i);
        } else {
            return -1L;
        }
    }
}

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


package org.aion.api.tools;

import java.util.ArrayList;

/**
 * Created by jay on 10/12/16.
 */
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

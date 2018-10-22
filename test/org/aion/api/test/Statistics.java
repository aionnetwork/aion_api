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

package org.aion.api.test;

import java.util.Arrays;

/** Created by Jay Tseng on 13/02/17. */
public class Statistics {

    double[] data;
    int size;

    public Statistics(double[] data) {
        this.data = data;
        size = data.length;
    }

    private double getMean() {
        double sum = 0.0;
        for (double a : data) {
            sum += a;
        }
        return sum / size;
    }

    private double getVariance() {
        double mean = getMean();
        double temp = 0;
        for (double a : data) {
            temp += (a - mean) * (a - mean);
        }
        return temp / size;
    }

    double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public double median() {
        Arrays.sort(data);

        if (data.length % 2 == 0) {
            return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
        }
        return data[data.length / 2];
    }
}

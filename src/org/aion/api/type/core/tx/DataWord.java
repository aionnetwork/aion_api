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

package org.aion.api.type.core.tx;

import org.aion.base.util.ByteArrayWrapper;
import org.aion.base.util.ByteUtil;
import org.aion.base.util.FastByteComparisons;
import org.aion.base.util.Hex;
import org.aion.base.vm.IDataWord;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Data word is the basic unit data used by Fvm. The size of a data word is
 * specified to 128 bits.
 */
public class DataWord implements Comparable<DataWord>, IDataWord {

    private static final BigInteger MAX_VALUE = BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE);

    private static final WordType wType = WordType.DATA_WORD;
    private static final DataWord ZERO = new DataWord(0);
    public static final DataWord ONE = new DataWord(1);
    private static final int BYTES = 16;

    private byte[] data;

    public DataWord() {
        data = new byte[BYTES];
    }

    private DataWord(int num) {
        ByteBuffer bb = ByteBuffer.allocate(BYTES);
        bb.position(12);
        bb.putInt(num);
        data = bb.array();
    }

    DataWord(long num) {
        ByteBuffer bb = ByteBuffer.allocate(BYTES);
        bb.position(8);
        bb.putLong(num);
        data = bb.array();
    }

    private DataWord(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Input data");
        } else if (data.length == BYTES) {
            this.data = data;
        } else if (data.length < BYTES) {
            ByteBuffer bb = ByteBuffer.allocate(BYTES);
            bb.position(BYTES - data.length);
            bb.put(data);
            this.data = bb.array();
        } else {
            throw new RuntimeException("Data word can't exceed 16 bytes: " + Hex.toHexString(data));
        }
    }

    public DataWord(BigInteger num) {
        this(num.toByteArray());
    }

    public DataWord(String data) {
        this(Hex.decode(data));
    }

    public DataWord(ByteArrayWrapper wrapper) {
        this(wrapper.getData());
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getNoLeadZeroesData() {
        return ByteUtil.stripLeadingZeroes(data);
    }

    public BigInteger value() {
        return new BigInteger(1, data);
    }

    private BigInteger sValue() {
        return new BigInteger(data);
    }

    public String bigIntValue() {
        return new BigInteger(data).toString();
    }

    /**
     * Converts this DataWord to an int, checking for lost information. If this
     * DataWord is out of the possible range for an int result then an
     * ArithmeticException is thrown.
     *
     * @return this DataWord converted to an int.
     * @throws ArithmeticException
     *             if this does not fit in an int.
     */
    private int intValue() {
        if (value().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new ArithmeticException("This dataword is beyond the range of int: " + value());
        }

        int v = 0;
        for (int i = 12; i < BYTES; i++) {
            v = (v << 8) + (data[i] & 0xff);
        }

        return v;
    }

    /**
     * In case of int overflow returns Integer.MAX_VALUE otherwise works as
     * #intValue()
     */
    public int intValueSafe() {
        if (value().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return Integer.MAX_VALUE;
        }

        int v = 0;
        for (int i = 12; i < BYTES; i++) {
            v = (v << 8) + (data[i] & 0xff);
        }

        return v;
    }

    /**
     * Converts this DataWord to a long, checking for lost information. If this
     * DataWord is out of the possible range for a long result then an
     * ArithmeticException is thrown.
     *
     * @return this DataWord converted to a long.
     * @throws ArithmeticException
     *             if this does not fit in a long.
     */
    long longValue() {
        if (value().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ArithmeticException("This dataword is beyond the range of int: " + value());
        }

        long v = 0;
        for (int i = 8; i < BYTES; i++) {
            v = (v << 8) + (data[i] & 0xff);
        }

        return v;
    }

    /**
     * In case of long overflow returns Long.MAX_VALUE otherwise works as
     * #longValue()
     */
    public long longValueSafe() {
        if (value().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            return Long.MAX_VALUE;
        }

        long v = 0;
        for (int i = 8; i < BYTES; i++) {
            v = (v << 8) + (data[i] & 0xff);
        }

        return v;
    }

    public boolean isZero() {
        for (int i = 0; i < BYTES; i++) {
            if (data[BYTES - 1 - i] != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isNegative() {
        int result = data[0] & 0x80;
        return result == 0x80;
    }

    public DataWord and(DataWord w2) {
        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] &= w2.data[i];
        }
        return this;
    }

    public DataWord or(DataWord w2) {
        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] |= w2.data[i];
        }
        return this;
    }

    public DataWord xor(DataWord w2) {
        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] ^= w2.data[i];
        }
        return this;
    }

    public void negate() {
        if (this.isZero()) {
            return;
        }

        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] = (byte) ~this.data[i];
        }

        for (int i = this.data.length - 1; i >= 0; --i) {
            this.data[i] = (byte) (1 + this.data[i] & 0xFF);
            if (this.data[i] != 0) {
                break;
            }
        }
    }

    public void bnot() {
        if (this.isZero()) {
            this.data = ByteUtil.copyToArray(MAX_VALUE);
            return;
        }
        BigInteger result = MAX_VALUE.subtract(this.value());
        setData(result);
    }

    public void add(DataWord word) {
        BigInteger result = value().add(word.value());
        setData(result);
    }

    public void mul(DataWord word) {
        BigInteger result = value().multiply(word.value());
        setData(result);
    }

    public void div(DataWord word) {
        if (word.isZero()) {
            this.and(ZERO);
            return;
        }

        BigInteger result = value().divide(word.value());
        setData(result);
    }

    public void sDiv(DataWord word) {

        if (word.isZero()) {
            this.and(ZERO);
            return;
        }

        BigInteger result = sValue().divide(word.sValue());
        setData(result);
    }

    public void sub(DataWord word) {
        BigInteger result = value().subtract(word.value());
        setData(result);
    }

    public void exp(DataWord word) {
        BigInteger result = value().pow(word.intValue());
        setData(result);
    }

    public void mod(DataWord word) {
        if (word.isZero()) {
            this.and(ZERO);
            return;
        }

        BigInteger result = value().mod(word.value());
        setData(result);
    }

    public void sMod(DataWord word) {
        if (word.isZero()) {
            this.and(ZERO);
            return;
        }

        BigInteger result = sValue().abs().mod(word.sValue().abs());
        result = (sValue().signum() == -1) ? result.negate() : result;

        setData(result);
    }

    public void addmod(DataWord word1, DataWord word2) {
        if (word2.isZero()) {
            this.data = new byte[BYTES<<1];
            return;
        }

        BigInteger result = value().add(word1.value()).mod(word2.value());
        setData(result);
    }

    public void mulmod(DataWord word1, DataWord word2) {
        if (this.isZero() || word1.isZero() || word2.isZero()) {
            this.data = new byte[BYTES<<1];
            return;
        }

        BigInteger result = value().multiply(word1.value()).mod(word2.value());
        setData(result);
    }

    private void setData(BigInteger bi) {
        this.data = ByteUtil.copyToArray(bi.and(MAX_VALUE));
    }

    public String toString() {
        return Hex.toHexString(data);
    }

    public String shortHex() {
        String hexValue = Hex.toHexString(getNoLeadZeroesData()).toUpperCase();
        return "0x" + hexValue.replaceFirst("^0+(?!$)", "");
    }

    public DataWord copy() {
        byte[] bs = new byte[BYTES];
        System.arraycopy(data, 0, bs, 0, BYTES);
        return new DataWord(bs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataWord dataWord = (DataWord) o;

        return Arrays.equals(data, dataWord.data);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public int compareTo(DataWord o) {
        if (o == null || o.getData() == null) {
            return -1;
        }
        int result = FastByteComparisons.compareTo(data, 0, data.length, o.getData(), 0, o.getData().length);
        // Convert result into -1, 0 or 1 as is the convention
        return (int) Math.signum(result);
    }

    public void signExtend(byte k) {
        if (0 > k || k > 31) {
            throw new IndexOutOfBoundsException();
        }
        byte mask = this.sValue().testBit((k * 8) + 7) ? (byte) 0xff : 0;
        for (int i = 31; i > k; i--) {
            this.data[31 - i] = mask;
        }
    }

    public int bytesOccupied() {
        int firstNonZero = ByteUtil.firstNonZeroByte(data);
        if (firstNonZero == -1) {
            return 0;
        }
        return 16 - firstNonZero;
    }

    public boolean isHex(String hex) {
        return Hex.toHexString(data).equals(hex);
    }

    public String asString() {
        return new String(getNoLeadZeroesData());
    }

    public WordType getType() { return wType; }
}

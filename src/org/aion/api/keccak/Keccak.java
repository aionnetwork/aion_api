/**
 * ***************************************************************************** Copyright (c)
 * 2017-2018 Aion foundation.
 *
 * <p>This file is part of the aion network project.
 *
 * <p>The aion network project is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * <p>The aion network project is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with the aion network
 * project source files. If not, see <https://www.gnu.org/licenses/>.
 *
 * <p>Contributors: Aion foundation.
 *
 * <p>****************************************************************************
 */
package org.aion.api.keccak;

import java.math.BigInteger;
import java.util.Formatter;

// Credits to: https://github.com/romus/sha/blob/master/sha3/src/com/theromus/example/Main.java
public class Keccak {

    private static BigInteger BIT_64 = new BigInteger("18446744073709551615");

    private BigInteger[] RC =
            new BigInteger[] {
                new BigInteger("0000000000000001", 16),
                new BigInteger("0000000000008082", 16),
                new BigInteger("800000000000808A", 16),
                new BigInteger("8000000080008000", 16),
                new BigInteger("000000000000808B", 16),
                new BigInteger("0000000080000001", 16),
                new BigInteger("8000000080008081", 16),
                new BigInteger("8000000000008009", 16),
                new BigInteger("000000000000008A", 16),
                new BigInteger("0000000000000088", 16),
                new BigInteger("0000000080008009", 16),
                new BigInteger("000000008000000A", 16),
                new BigInteger("000000008000808B", 16),
                new BigInteger("800000000000008B", 16),
                new BigInteger("8000000000008089", 16),
                new BigInteger("8000000000008003", 16),
                new BigInteger("8000000000008002", 16),
                new BigInteger("8000000000000080", 16),
                new BigInteger("000000000000800A", 16),
                new BigInteger("800000008000000A", 16),
                new BigInteger("8000000080008081", 16),
                new BigInteger("8000000000008080", 16),
                new BigInteger("0000000080000001", 16),
                new BigInteger("8000000080008008", 16)
            };

    //	The rotation offsets r[x,y].
    private int[][] r =
            new int[][] {
                {0, 36, 3, 41, 18},
                {1, 44, 10, 45, 2},
                {62, 6, 43, 15, 61},
                {28, 55, 25, 21, 56},
                {27, 20, 39, 8, 14}
            };

    private int w;
    private int n;

    public Keccak(int b) {
        w = b / 25;
        int l = log(w);
        n = 12 + 2 * l;
    }

    private int log(int x) {
        return (int) (Math.log(x) / Math.log(2));
    }

    public String getHash(String message, int r, int d) {
        //		Initialization and padding
        BigInteger[][] S = new BigInteger[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                S[i][j] = new BigInteger("0", 16);
            }
        }

        BigInteger[][] P = padding(message, r);

        //	    Absorbing phase
        for (BigInteger[] Pi : P) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if ((i + j * 5) < (r / w)) {
                        S[i][j] = S[i][j].xor(Pi[i + j * 5]);
                    }
                }
            }

            doKeccackf(S);
        }

        //	    Squeezing phase
        String Z = "";

        do {

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if ((5 * i + j) < (r / w)) {
                        Z = Z + addZero(getReverseHexString(S[j][i]), 16).substring(0, 16);
                    }
                }
            }

            doKeccackf(S);
        } while (Z.length() < d * 2);

        return Z.substring(0, d * 2);
    }

    private BigInteger[][] doKeccackf(BigInteger[][] A) {
        for (int i = 0; i < n; i++) {
            A = roundB(A, RC[i]);
        }

        return A;
    }

    private BigInteger[][] roundB(BigInteger[][] A, BigInteger RC) {
        BigInteger[] C = new BigInteger[5];
        BigInteger[] D = new BigInteger[5];
        BigInteger[][] B = new BigInteger[5][5];

        // θ step
        for (int i = 0; i < 5; i++) {
            C[i] = A[i][0].xor(A[i][1]).xor(A[i][2]).xor(A[i][3]).xor(A[i][4]);
        }

        for (int i = 0; i < 5; i++) {
            D[i] = C[(i + 4) % 5].xor(rot(C[(i + 1) % 5], 1));
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[i][j] = A[i][j].xor(D[i]);
            }
        }

        // ρ and π steps
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                B[j][(2 * i + 3 * j) % 5] = rot(A[i][j], r[i][j]);
            }
        }
        // χ step
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[i][j] = B[i][j].xor(B[(i + 1) % 5][j].not().and(B[(i + 2) % 5][j]));
            }
        }

        // ι step
        A[0][0] = A[0][0].xor(RC);

        return A;
    }

    private BigInteger rot(BigInteger x, int n) {
        n = n % w;

        BigInteger leftShift = getShiftLeft64(x, n);
        BigInteger rightShift = x.shiftRight(w - n);

        return leftShift.or(rightShift);
    }

    private BigInteger getShiftLeft64(BigInteger value, int shift) {
        BigInteger retValue = value.shiftLeft(shift);
        BigInteger tmpValue = value.shiftLeft(shift);

        if (retValue.compareTo(BIT_64) > 0) {
            for (int i = 64; i < 64 + shift; i++) {
                tmpValue = tmpValue.clearBit(i);
            }

            tmpValue = tmpValue.setBit(64 + shift);
            retValue = tmpValue.and(retValue);
        }

        return retValue;
    }

    private BigInteger[][] padding(String message, int r) {
        int size;
        message = message + "01";

        while (((message.length() / 2) * 8 % r) != ((r - 8))) {
            message = message + "00";
        }

        message = message + "80";
        size = (((message.length() / 2) * 8) / r);

        BigInteger[][] arrayM = new BigInteger[size][];
        arrayM[0] = new BigInteger[1600 / w];
        initArray(arrayM[0]);

        int count = 0;
        int j = 0;
        int i = 0;

        for (int _n = 0; _n < message.length(); _n++) {

            if (j > (r / w - 1)) {
                j = 0;
                i++;
                arrayM[i] = new BigInteger[1600 / w];
                initArray(arrayM[i]);
            }

            count++;

            if ((count * 4 % w) == 0) {
                String subString = message.substring((count - w / 4), (w / 4) + (count - w / 4));
                arrayM[i][j] = new BigInteger(subString, 16);
                String revertString = getReverseHexString(arrayM[i][j]);
                revertString = addZero(revertString, subString.length());
                arrayM[i][j] = new BigInteger(revertString, 16);
                j++;
            }
        }

        return arrayM;
    }

    private String getReverseHexString(BigInteger l) {
        byte[] array = l.toByteArray();
        reverseByteArray(array);
        return getHexStringByByteArray(array);
    }

    private String addZero(String str, int length) {
        StringBuilder retStr = new StringBuilder(str);
        for (int i = 0; i < length - str.length(); i++) {
            retStr.append("0");
        }
        return retStr.toString();
    }

    private void reverseByteArray(byte[] array) {
        if (array == null) {
            return;
        }

        int i = 0;
        int j = array.length - 1;
        byte tmp;

        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        } // while
    }

    private String getHexStringByByteArray(byte[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder(array.length * 2);
        @SuppressWarnings("resource")
        Formatter formatter = new Formatter(stringBuilder);

        for (byte tempByte : array) {
            formatter.format("%02x", tempByte);
        }

        return stringBuilder.toString();
    }

    private void initArray(BigInteger[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = new BigInteger("0", 16);
        }
    }
}

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
package org.aion.api.sol.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.aion.api.ITx;
import org.aion.api.impl.ErrId;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.TxArgs;
import org.aion.base.util.ByteArrayWrapper;
import org.slf4j.Logger;

/**
 * Abstract class that all Solidity type derive from. Contains a core set of operations related to
 * RLP encoding and decoding. Currently supports all available solidity type. Most functions used
 * are not intended to be user facing, and should be left unused by the user.
 */
public abstract class SolidityAbstractType {

    protected static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.SOL.name());
    String type;
    SolidityValue.SolidityArgsType typeProperty;
    private List<Integer> dynamicParameters;
    List valArray;
    private List outValArray;
    static final int encodeUnitLength = 16;
    static final int encodeUnitLengthDouble = 32;

    public SolidityAbstractType() {
        super();
        this.dynamicParameters = new ArrayList<>();
        this.outValArray = new ArrayList<>();
        this.valArray = new ArrayList<>();
        this.typeProperty = SolidityValue.SolidityArgsType.STATIC;
    }

    // Abstract Implementations

    /**
     * Checks that inputted string is the correct type. To be used with ABI.
     *
     * @param in Solidity Type
     * @return solidity Type belong to this type or not
     */
    public abstract boolean isType(String in);

    /**
     * Returns a correctly formatted hex string, given an input byte array (usually 32 bytes).
     * Encoding varies depending on the solidity type being encoded.
     *
     * @param entry
     * @return formatted string for encode
     */
    public abstract String formatToString(byte[] entry);

    /**
     * For Contract decodes the response data from server, not user facing.
     *
     * @param entry
     * @param offset
     * @return decoded Solidity data
     */
    protected abstract Object decodeToSolidityType(byte[] entry, int offset);

    /**
     * @param offset decodes response of {@link ITx#call(TxArgs) call} into proper format
     * @param data byte string response from call
     * @return Object of decoded parameter, can be type String, Long, Int, byte[] or an ArrayList.
     */
    public Object decode(int offset, ByteArrayWrapper data) {
        if (data == null) {
            throw new NullPointerException();
        }
        return decodeHelper(offset, data, 0);
    }

    private Object decodeHelper(int offset, ByteArrayWrapper data, int layer) {

        if (this.getLayerIsDynamicArray(layer)) {

            if (offset + encodeUnitLength > data.getData().length) {
                return this.decodeToSolidityType(data.getData(), offset);
            }

            int arrayOffset = ApiUtils.toInt(data.getData(), offset, encodeUnitLength);
            int length = ApiUtils.toInt(data.getData(), arrayOffset, encodeUnitLength);

            int arrayStart = arrayOffset + encodeUnitLength;
            int innerLayer = layer + 1;

            int nestedStaticPartLength = getStaticPartLayerLength(innerLayer);
            int roundedNestedStaticPartLength =
                    ((int)
                                    (((double) nestedStaticPartLength + encodeUnitLength - 1)
                                            / encodeUnitLength))
                            * encodeUnitLength;

            List ret = new ArrayList();

            for (int i = 0;
                    i < length * roundedNestedStaticPartLength;
                    i += roundedNestedStaticPartLength) {
                ret.add(decodeHelper(arrayStart + i, data, innerLayer));
            }

            return ret;

        } else if (this.getLayerIsStaticArray(layer)) {
            int length = this.dynamicParameters.get(layer);
            int innerLayer = layer + 1;

            int nestedStaticPartLength = this.getStaticPartLayerLength(innerLayer);
            int encodeLen = isDoubleUnit() ? encodeUnitLengthDouble : encodeUnitLength;
            int roundedNestedStaticPartLength =
                    ((int) (((double) nestedStaticPartLength + encodeLen - 1) / encodeLen))
                            * encodeLen;

            List ret = new ArrayList();
            for (int i = 0;
                    i < length * roundedNestedStaticPartLength;
                    i += roundedNestedStaticPartLength) {
                ret.add(decodeHelper(offset + i, data, innerLayer));
            }

            return ret;
        } else {
            return this.decodeToSolidityType(data.getData(), offset);
        }
    }

    /**
     * returns the solidity type
     *
     * @return solidity type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the solidity type, also verifies the input type correctness and sets the type of the
     * variable from {@link SolidityValue.SolidityArgsType}, not intended for user usage
     *
     * @param type input type
     */
    public void setType(String type) {
        if (type == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[setType] {}", ErrId.getErrString(-315L));
            }
            return;
        }

        this.type = type;
        this.verifyInputParametersList();
    }

    // End Setters & Getters
    private void verifyInputParametersList() {
        if (this.dynamicParameters.size() != 0) {
            if (this.dynamicParameters.contains(-1)) {
                this.typeProperty = SolidityValue.SolidityArgsType.DYNAMICARRAY;
            } else {
                this.typeProperty = SolidityValue.SolidityArgsType.STATICARRAY;
            }
        } else {
            this.typeProperty = SolidityValue.SolidityArgsType.STATIC;
        }
    }

    /**
     * Debug function used for setting and getting dynamic parameters, not intended for user usage
     *
     * @param dynamicParams
     */
    public void setDynamicParameters(List<Integer> dynamicParams) {
        if (dynamicParams == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[setDynamicParameters] {}", ErrId.getErrString(-315L));
            }
            return;
        }
        this.dynamicParameters = dynamicParams;
    }

    /**
     * Utilized to generate the RLP encoded input format to be sent to backend. Not intended for
     * user usage.
     *
     * @return RLP encoded string
     */
    public String getInputFormat() {
        if (this.getIsDynamic()) {
            return this.generateDynamicFormat();
        } else {
            return this.generateStaticFormat();
        }
    }

    /**
     * Get if the solidity type is of type dynamic, used to check which encoding/decoding method to
     * use.
     *
     * @return true if type is dynamic.
     */
    public boolean getIsDynamic() {
        return (this.typeProperty == SolidityValue.SolidityArgsType.DYNAMICARRAY)
                || (this.typeProperty == SolidityValue.SolidityArgsType.DYNAMIC);
    }

    // note this function returns the reversed array
    private boolean getLayerIsDynamicArray(int layer) {
        return layer < this.dynamicParameters.size()
                && (this.dynamicParameters.get((this.dynamicParameters.size() - 1) - layer) == -1);
    }

    private boolean getLayerIsStaticArray(int layer) {
        return layer < this.dynamicParameters.size()
                && (this.dynamicParameters.get((this.dynamicParameters.size() - 1) - layer) != -1);
    }

    private String generateDynamicFormat() {
        return encodeString(valArray, 0);
    }

    private String generateStaticFormat() {
        if (!this.getIsStatic()) {
            return this.encodeString(valArray, 0);
        } else {
            return formatToString((byte[]) valArray.get(0));
        }
    }

    private String encodeString(List l, int layer) {
        if (l == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[encodeString] {}", ErrId.getErrString(-315L));
            }
            return null;
        }

        int innerLayer = layer;
        String outStr = "";

        if (this.getLayerIsDynamicArray(layer)) {
            outStr += ApiUtils.toHexPadded16(BigInteger.valueOf(l.size()).toByteArray());
            innerLayer++;

            for (Object entry : l) {
                outStr = getOutString(innerLayer, outStr, entry);
                if (outStr == null) return null;
            }
        } else {
            innerLayer++;

            int index = (this.dynamicParameters.size() - 1) - layer;
            for (int i = 0; i < this.dynamicParameters.get(index); i++) {
                if (i < l.size()) {
                    Object entry = l.get(i);
                    outStr = getOutString(innerLayer, outStr, entry);
                    if (outStr == null) return null;
                } else {
                    outStr += ApiUtils.toHexPadded16(new byte[] {0});
                }
            }
        }
        return outStr;
    }

    private String getOutString(int innerLayer, String outStr, Object entry) {
        if (entry instanceof byte[]) {
            outStr += formatToString((byte[]) entry);
        } else if (entry instanceof ArrayList) {
            outStr += encodeString((ArrayList) entry, innerLayer);
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[encodeString] {}", ErrId.getErrString(-114L));
            }
            return null;
        }
        return outStr;
    }

    /**
     * Returns the length of the dynamic portion of the type.
     *
     * @return integer indicating the length of the dynamic portion of the hex string
     */
    public int getDynamicPartLength() {
        return getInputFormat().length();
    }

    public int getDynamicOffset() {
        return getDynamicPartLength() / 2;
    }

    /**
     * Returns the length of the static portion of the type.
     *
     * @return integer indicating the length of the static portion of the hex string
     */
    public int getStaticPartLength() {

        boolean doubleUnit = isDoubleUnit();

        if (getIsStatic()) {
            return doubleUnit ? encodeUnitLength << 1 : encodeUnitLength;
        } else {
            // else is static array
            int mul = 1;
            for (Integer i : this.dynamicParameters) {
                if (i != -1) {
                    mul *= i;
                }
            }
            return doubleUnit ? ((encodeUnitLength * mul) << 1) : (encodeUnitLength * mul);
        }
    }

    protected abstract boolean isDoubleUnit();

    /**
     * Get if solidity type is of type static, used to check which encoding/decoding method to use.
     *
     * @return true if type is static.
     */
    private boolean getIsStatic() {
        return (this.typeProperty == SolidityValue.SolidityArgsType.STATIC);
    }

    /**
     * Returns the static layer length, dependant on the layer of the current encoding. Not user
     * facing.
     *
     * @param layer current {@link #dynamicParameters dynamicParameters} index
     * @return
     */
    private int getStaticPartLayerLength(int layer) {
        if (layer >= this.dynamicParameters.size()) {
            return isDoubleUnit() ? encodeUnitLengthDouble : encodeUnitLength;
        } else {
            int mul = 1;
            for (int i = 0; i < this.dynamicParameters.size() - layer; i++) {
                mul *= this.dynamicParameters.get(i);
            }
            return (isDoubleUnit() ? encodeUnitLengthDouble : encodeUnitLength) * mul;
        }
    }
}

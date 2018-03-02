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

package org.aion.api.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.aion.api.IContract;
import org.aion.api.IContractController;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.type.*;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.aion.api.impl.Contract.SC_FN_CONSTRUCTOR;
import static org.aion.api.impl.ErrId.getErrString;

public final class ContractController implements IContractController {
    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.CNT.name());
    private static final String REGEX_SC_REPLACER = "\\p{Cc}";
    private static final Map<Address, IContract> CONTAINER = new HashMap<>();
    private static AionAPIImpl API;

    ContractController(final AionAPIImpl api) {
        API = api;
    }

    public ApiMsg createFromSource(final String source, final Address from, final long nrgLimit, final long nrgPrice) {
        return createFromSource(source, from, nrgLimit, nrgPrice, BigInteger.ZERO, new HashMap<>());
    }

    public ApiMsg createFromSource(final String source, final Address from, final long nrgLimit, final long nrgPrice, final BigInteger value) {
        return createFromSource(source, from, nrgLimit, nrgPrice, value, new HashMap<>());
    }

    public ApiMsg createFromSource(final String source, final Address from, final long nrgLimit, final long nrgPrice,
            List<ISolidityArg> params) {

        Map<String, List<ISolidityArg>> paramsMap = new HashMap();
        paramsMap.put("", params);

        return createFromSource(source, from, nrgLimit, nrgPrice, BigInteger.ZERO, params);
    }

    public ApiMsg createFromSource(final String source, final Address from, final long nrgLimit, final long nrgPrice,
            final Map<String, List<ISolidityArg>> params) {
        return createFromSource(source, from, nrgLimit, nrgPrice, BigInteger.ZERO, params);
    }

    public ApiMsg createFromSource(final String source, final Address from, final long nrgLimit, final long nrgPrice, final BigInteger value,
            List<ISolidityArg> params) {

        Map<String, List<ISolidityArg>> paramsMap = new HashMap();
        paramsMap.put("", params);

        return createFromSource(source, from, nrgLimit, nrgPrice, value, paramsMap);
    }

    public ApiMsg createFromSource(final String source, final Address from, final long nrgLimit, final long nrgPrice, final BigInteger value,
            Map<String, List<ISolidityArg>> params) {
        if (from == null || source == null || params == null || value == null) {
            throw new NullPointerException(
                    "Source#" + String.valueOf(source) + " from#" + String.valueOf(from) + " value#" + String
                            .valueOf(value) + " inputParams#" + String.valueOf(params));
        }

        if (nrgLimit < 0 || nrgPrice < 1) {
            throw new IllegalArgumentException("nrgConsumed#" + nrgLimit + " nrgPrice" + nrgPrice);
        }

        source.replaceAll(REGEX_SC_REPLACER, "");

        ApiMsg apiMsg = API.getTx().compile(source);
        if (apiMsg.isError()) {
            return apiMsg;
        }

        Map<String, CompileResponse> response = apiMsg.getObject();
        for (Map.Entry<String, CompileResponse> entry : response.entrySet()) {
            if (entry.getValue() == null) {
                throw new NullPointerException("null CompileResponse#" + entry.getKey());
            }

            String trimCtName = entry.getKey().replace("<stdin>:", "");
            ByteArrayWrapper data = ByteArrayWrapper.wrap(ByteArrayWrapper.NULL_BYTE);
            boolean hasConstructor = false;
            for (ContractAbiEntry cae : entry.getValue().getAbiDefinition()) {
                if (cae.isConstructor()) {

                    Contract ct = new Contract(entry.getValue(), trimCtName)
                            .newFunction(SC_FN_CONSTRUCTOR)
                            .setFrom(from)
                            .setTxNrgLimit(nrgLimit)
                            .setTxNrgPrice(nrgPrice);

                    List<ISolidityArg> ctParams;
                    // assume only one contract want to deploy
                    if (params.get("") != null && response.size() == 1) {
                        ctParams = params.get("");
                    } else {
                        ctParams = params.get(trimCtName);
                    }

                    if (ctParams != null) {
                        for (ISolidityArg i : ctParams) {
                            ct = ct.setParam(i);
                        }

                        ct.build().encodeParams(ct.getAbiFunction());
                        data = ct.getEncodedData();
                        hasConstructor = true;

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[createCtListFromSource] This contract has contractor!");
                        }
                    }

                    break;
                }
            }

            ContractDeploy.ContractDeployBuilder cd = new ContractDeploy.ContractDeployBuilder()
                    .compileResponse(entry.getValue()).constructor(hasConstructor).data(data).from(from)
                    .nrgLimit(nrgLimit).nrgPrice(nrgPrice).value(value);

            apiMsg = API.getTx().contractDeploy(cd.createContractDeploy());

            if (apiMsg.isError()) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[createFromSource] {}", getErrString(-327L));
                }
                return apiMsg;
            }

            DeployResponse dr = apiMsg.getObject();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("[createFromSource] Contract deployed - address: [{}], txhash: [{}]",
                        dr.getAddress().toString(), dr.getTxid().toString());
            }

            Contract.ContractBuilder builder = new Contract.ContractBuilder().api(API).deployResponse(dr)
                    .compileResponse(entry.getValue()).from(from).contractName(trimCtName);

            CONTAINER.put(dr.getAddress(), builder.createContract());
        }

        return apiMsg.set(1);
    }

    public IContract getContractAt(final Address from, final Address contract, final String abi) {

        // generate type for Gson parsing
        Type abiType = new TypeToken<ArrayList<ContractAbiEntry>>() {
        }.getType();
        List<ContractAbiEntry> abiDef = new Gson().fromJson(abi, abiType);

        CompileResponse.CompileResponseBuilder builder = new CompileResponse.CompileResponseBuilder()
                .abiDefinition(abiDef).abiDefString("").code("").compilerOptions("").compilerVersion("").language("")
                .languageVersion("").userDoc(JsonFmt.JsonFmtBuilder.emptyJsonFmt())
                .developerDoc(JsonFmt.JsonFmtBuilder.emptyJsonFmt()).source("");

        DeployResponse dr = new DeployResponse(contract, Hash256.ZERO_HASH());

        Contract.ContractBuilder contractBuilder = new Contract.ContractBuilder().api(API).deployResponse(dr)
                .compileResponse(builder.createCompileResponse()).from(from).contractName("");

        CONTAINER.put(contract, contractBuilder.createContract());

        return CONTAINER.get(contract);
    }

    public final IContract getContract(Address addr) {
        return CONTAINER.get(addr);
    }

    public final IContract getContract() {

        IContract res = null;
        for (Map.Entry<Address, IContract> ct : CONTAINER.entrySet()) {
            return ct.getValue();
        }

        return res;
    }

    public List<IContract> getContract(String contractName) {
        Objects.requireNonNull(contractName);

        return CONTAINER.entrySet().stream()
                .filter(p ->
                    p.getValue().getContractName().contentEquals(contractName))
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public List<Map.Entry<Address, String>> getContractList() {
        List<Map.Entry<Address, String>> l = new ArrayList<>();

        for (Map.Entry<Address, IContract> ct : this.CONTAINER.entrySet()) {
            l.add(new AbstractMap.SimpleEntry(ct.getKey(), ct.getValue().getContractName()));
        }

        return l;
    }

    public final IContract put(IContract c) {
        return CONTAINER.put(c.getContractAddress(), c);
    }

    public ApiMsg remove(IContract c) {
        return new ApiMsg().set(CONTAINER.remove(c.getContractAddress(), c), ApiMsg.cast.BOOLEAN);
    }

    public void clear() {
        CONTAINER.clear();
    }
}

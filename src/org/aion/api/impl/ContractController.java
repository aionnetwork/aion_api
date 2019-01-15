package org.aion.api.impl;

import static org.aion.api.impl.Contract.SC_FN_CONSTRUCTOR;
import static org.aion.api.impl.ErrId.getErrString;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.Api;
import java.io.File;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.aion.api.IContract;
import org.aion.api.IContractController;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.CompileResponse;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractDeploy;
import org.aion.api.type.DeployResponse;
import org.aion.api.type.JsonFmt;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.vm.api.interfaces.Address;
import org.slf4j.Logger;

public final class ContractController implements IContractController {

    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.CNT.name());
    private static final String REGEX_SC_REPLACER = "\\p{Cc}";
    private static final Map<Address, IContract> CONTAINER = new HashMap<>();
    private static AionAPIImpl API;

    ContractController(final AionAPIImpl api) {
        API = api;
    }

    public ApiMsg createFromSource(
            final String source, final Address from, final long nrgLimit, final long nrgPrice) {
        return createFromSource(source, from, nrgLimit, nrgPrice, BigInteger.ZERO, new HashMap<>());
    }

    public ApiMsg createFromSource(
            final String source,
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            final BigInteger value) {
        return createFromSource(source, from, nrgLimit, nrgPrice, value, new HashMap<>());
    }

    public ApiMsg createFromSource(
            final String source,
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            List<ISolidityArg> params) {

        Map<String, List<ISolidityArg>> paramsMap = new HashMap<>();
        paramsMap.put("", params);

        return createFromSource(source, from, nrgLimit, nrgPrice, BigInteger.ZERO, paramsMap);
    }

    public ApiMsg createFromSource(
            final String source,
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            final Map<String, List<ISolidityArg>> params) {
        return createFromSource(source, from, nrgLimit, nrgPrice, BigInteger.ZERO, params);
    }

    public ApiMsg createFromSource(
            final String source,
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            final BigInteger value,
            List<ISolidityArg> params) {

        Map<String, List<ISolidityArg>> paramsMap = new HashMap<>();
        paramsMap.put("", params);

        return createFromSource(source, from, nrgLimit, nrgPrice, value, paramsMap);
    }

    public ApiMsg createFromSource(
            final String source,
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            final BigInteger value,
            Map<String, List<ISolidityArg>> params) {

        checkParams(from, nrgLimit, nrgPrice, value, params);

        if (source == null) {
            throw new IllegalArgumentException("Source is null");
        }

        final String s = source.replaceAll(REGEX_SC_REPLACER, "");

        ApiMsg apiMsg = Objects.requireNonNull(API.getTx()).compile(s);

        return processCompiledContracts(apiMsg, from, nrgLimit, nrgPrice, value, params);
    }

    public ApiMsg createFromDirectory(
            final File sourceDir,
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            final BigInteger value,
            Map<String, List<ISolidityArg>> params) {

        checkParams(from, nrgLimit, nrgPrice, value, params);

        if (sourceDir == null || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("SourceDir#" + sourceDir);
        }

        ApiMsg apiMsg = Objects.requireNonNull(API.getTx()).compile(sourceDir.toPath());
        return processCompiledContracts(apiMsg, from, nrgLimit, nrgPrice, value, params);
    }

    private void checkParams(
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            final BigInteger value,
            Map<String, List<ISolidityArg>> params) {

        if (from == null || params == null || value == null) {
            throw new NullPointerException(
                    " from#" + from + " value#" + value + " inputParams#" + params);
        }

        if (nrgLimit < 0 || nrgPrice < 0) {
            throw new IllegalArgumentException("nrgConsumed#" + nrgLimit + " nrgPrice" + nrgPrice);
        }
    }

    private ApiMsg processCompiledContracts(
            ApiMsg apiMsg,
            final Address from,
            final long nrgLimit,
            final long nrgPrice,
            final BigInteger value,
            Map<String, List<ISolidityArg>> params) {
        if (apiMsg.isError()) {
            return apiMsg;
        }

        Map<String, CompileResponse> response = apiMsg.getObject();
        for (Map.Entry<String, CompileResponse> entry : response.entrySet()) {
            if (entry.getValue() == null) {
                throw new NullPointerException("null CompileResponse#" + entry.getKey());
            }

            boolean isInterface = entry.getValue().getCode().equals("0x");

            if (!isInterface) {
                String trimCtName = entry.getKey().replace("<stdin>:", "");
                ByteArrayWrapper data = ByteArrayWrapper.wrap(new byte[0]);
                boolean hasConstructor = false;
                for (ContractAbiEntry cae : entry.getValue().getAbiDefinition()) {
                    if (cae.isConstructor()) {

                        hasConstructor = true;

                        Contract ct =
                                new Contract(entry.getValue(), trimCtName)
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

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(
                                        "[createCtListFromSource] This contract has contractor!");
                            }
                        }

                        break;
                    }
                }

                ContractDeploy.ContractDeployBuilder cd =
                        new ContractDeploy.ContractDeployBuilder()
                                .compileResponse(entry.getValue())
                                .constructor(hasConstructor)
                                .data(data)
                                .from(from)
                                .nrgLimit(nrgLimit)
                                .nrgPrice(nrgPrice)
                                .value(value);

                apiMsg = API.getTx().contractDeploy(cd.createContractDeploy());

                if (apiMsg.isError()) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[createFromSource] {}", getErrString(-327L));
                    }
                    return apiMsg;
                }

                DeployResponse dr = apiMsg.getObject();

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(
                            "[createFromSource] Contract deployed - name:[{}] address:[{}], txhash: [{}]",
                            entry.getKey(),
                            dr.getAddress().toString(),
                            dr.getTxid().toString());
                }

                Contract.ContractBuilder builder =
                        new Contract.ContractBuilder()
                                .api(API)
                                .deployResponse(dr)
                                .compileResponse(entry.getValue())
                                .from(from)
                                .contractName(trimCtName);

                CONTAINER.put(dr.getAddress(), builder.createContract());
            }
        }

        return apiMsg.set(1);
    }

    public IContract getContractAt(final Address from, final Address contract, final String abi) {

        // generate type for Gson parsing
        Type abiType = new TypeToken<ArrayList<ContractAbiEntry>>() {}.getType();
        List<ContractAbiEntry> abiDef = new Gson().fromJson(abi, abiType);

        CompileResponse.CompileResponseBuilder builder =
                new CompileResponse.CompileResponseBuilder()
                        .abiDefinition(abiDef)
                        .abiDefString("")
                        .code("")
                        .compilerOptions("")
                        .compilerVersion("")
                        .language("")
                        .languageVersion("")
                        .userDoc(JsonFmt.JsonFmtBuilder.emptyJsonFmt())
                        .developerDoc(JsonFmt.JsonFmtBuilder.emptyJsonFmt())
                        .source("");

        DeployResponse dr = new DeployResponse(contract, Hash256.ZERO_HASH());

        Contract.ContractBuilder contractBuilder =
                new Contract.ContractBuilder()
                        .api(API)
                        .deployResponse(dr)
                        .compileResponse(builder.createCompileResponse())
                        .from(from)
                        .contractName("");

        CONTAINER.put(contract, contractBuilder.createContract());

        return CONTAINER.get(contract);
    }

    public final IContract getContract(Address addr) {
        return CONTAINER.get(addr);
    }

    public final IContract getContract() {
        return CONTAINER.entrySet().stream().findFirst().map(Entry::getValue).orElse(null);
    }

    public List<IContract> getContract(String contractName) {
        Objects.requireNonNull(contractName);

        return CONTAINER
                .entrySet()
                .stream()
                .filter(p -> p.getValue().getContractName().contentEquals(contractName))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public Map<Address, String> getContractMap() {
        Map<Address, String> rtn = new HashMap<>();

        for (Map.Entry<Address, IContract> ct : CONTAINER.entrySet()) {
            rtn.put(ct.getKey(), ct.getValue().getContractName());
        }

        return rtn;
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

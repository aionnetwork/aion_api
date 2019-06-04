package org.aion.api.impl;

import static org.aion.api.sol.impl.SolidityValue.SolidityTypeEnum.ADDRESS;
import static org.aion.api.sol.impl.SolidityValue.SolidityTypeEnum.BOOL;
import static org.aion.api.sol.impl.SolidityValue.SolidityTypeEnum.BYTES;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.aion.aion_types.NewAddress;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.IUtils;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.sol.impl.Bool;
import org.aion.api.sol.impl.Bytes;
import org.aion.api.sol.impl.DynamicBytes;
import org.aion.api.sol.impl.Int;
import org.aion.api.sol.impl.SString;
import org.aion.api.sol.impl.SolidityValue;
import org.aion.api.sol.impl.Uint;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.CompileResponse;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractAbiIOParam;
import org.aion.api.type.ContractEvent;
import org.aion.api.type.ContractEventFilter;
import org.aion.api.type.ContractResponse;
import org.aion.api.type.DeployResponse;
import org.aion.api.type.JsonFmt;
import org.aion.api.type.MsgRsp;
import org.aion.api.type.TxArgs;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;

/**
 * Returns a Contract class that sits above the NucoAPI layer that provides the user with a
 * convenient method of encoding and decoding contract calls and transactions from the Nuco backend.
 * The Contract class has no public constructors, instead the user utilizes two factory methods
 * {@link ContractController(String, IAionAPI) createFromSource} and {@link
 * ContractController(String, byte[], IAionAPI) createFromAddress} for class instantiation. This
 * class requires the user provide an active and connected {@link AionAPIImpl} object
 */
@SuppressWarnings("Annotator")
public final class Contract implements IContract {

    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.CNT.name());
    private static final String REGEX_SC_PATT = "\\[([0-9]*)\\]";
    private static final String ALLEVENTS = "ALLEVENTS";
    private static final String REGEX_NUMERIC = "-?\\d+(\\.\\d+)?";

    private static boolean nonBlock = false;

    public static final Pattern ELEMENT_PATTERN = Pattern.compile(REGEX_SC_PATT);
    public static final String SC_FN_CONSTRUCTOR = "constructor";
    public static final String SC_FN_FUNC = "function";
    public static final String SC_FN_EVENT = "event";
    public static final String SC_FN_FALLBACK = "fallback";

    private final AionAPIImpl api;
    private final String contractName;
    private final NewAddress contractAddress;
    private final List<ContractAbiEntry> abiDefinition;
    private final Map<String, List<ContractAbiEntry>> funcParams;
    private final String abiDefStr;
    private final String contractCode;
    private final String contractSource;
    private final String languageVersion;
    private final String compilerVersion;
    private final String compilerOptions;
    private final JsonFmt userDoc;
    private final JsonFmt developerDoc;
    private final Hash256 deployTxId;

    // Transaction relative settings
    private NewAddress from;
    private long txNrgLimit;
    private long txNrgPrice;
    private BigInteger txValue;
    private TxArgs txArgs;

    // event relative settings
    private final List<String> eventsName;
    private Map<String, String> eventMapping;
    private LRUMap<String, String> eventsIssued;
    private LRUMap<String, String> eventsIssuedRev;

    // function relative settings
    private final List<ISolidityArg> inputParams;
    private final List<ISolidityArg> outputParams;
    private String functionName;

    private ContractAbiEntry abiFunc;
    private Type abiType;

    // private internal state variables
    private Boolean functionDefined;
    private Boolean functionBuilt;
    private Boolean eventDefined;
    private String txHash;
    private boolean isConstant;
    private boolean isConstructor;
    private int errorCode;

    public static class ContractBuilder {

        private CompileResponse cr;
        private DeployResponse dr;
        private AionAPIImpl api;
        private NewAddress from;
        private String contractName;

        ContractBuilder() {}

        ContractBuilder compileResponse(final CompileResponse cr) {
            this.cr = cr;
            return this;
        }

        ContractBuilder deployResponse(final DeployResponse dr) {
            this.dr = dr;
            return this;
        }

        public ContractBuilder api(final AionAPIImpl api) {
            this.api = api;
            return this;
        }

        public ContractBuilder from(final NewAddress from) {
            this.from = from;
            return this;
        }

        ContractBuilder contractName(final String contractName) {
            this.contractName = contractName;
            return this;
        }

        Contract createContract() {
            if (cr == null || dr == null || api == null || from == null || contractName == null) {
                throw new NullPointerException(
                        "compileResponse#"
                                + cr
                                + " deployResponse#"
                                + dr
                                + " api#"
                                + api
                                + " from#"
                                + from
                                + " contractName#"
                                + contractName);
            }

            return new Contract(this);
        }
    }

    private Contract(final ContractBuilder contractBuilder) {
        this.api = contractBuilder.api;
        this.contractName = contractBuilder.contractName;
        this.contractAddress = contractBuilder.dr.getAddress();
        this.deployTxId = contractBuilder.dr.getTxid();
        this.from = contractBuilder.from;
        this.abiDefinition = contractBuilder.cr.getAbiDefinition();
        this.abiDefStr = contractBuilder.cr.getAbiDefString();
        this.contractCode = contractBuilder.cr.getCode();
        this.contractSource = contractBuilder.cr.getSource();
        this.languageVersion = contractBuilder.cr.getLanguageVersion();
        this.compilerOptions = contractBuilder.cr.getCompilerOptions();
        this.compilerVersion = contractBuilder.cr.getCompilerVersion();
        this.userDoc = contractBuilder.cr.getUserDoc();
        this.developerDoc = contractBuilder.cr.getDeveloperDoc();
        this.funcParams = setupAbiFunctionDefinitions(contractBuilder.cr.getAbiDefinition());

        this.inputParams = new ArrayList<>();
        this.outputParams = new ArrayList<>();
        this.eventsName = new ArrayList<>();
        int lrumapSize = 100;
        this.eventsIssued = new LRUMap<>(lrumapSize);
        this.eventsIssuedRev = new LRUMap<>(lrumapSize);
        reset();
    }

    /**
     * Helper constructor for deploy a contract including the initial parameters in the constructor.
     */
    protected Contract(final CompileResponse cr, final String contractName) {
        this.api = null;
        this.contractName = contractName;
        this.deployTxId = null;
        this.contractAddress = null;
        this.from = null;
        this.abiDefinition = cr.getAbiDefinition();
        this.abiDefStr = cr.getAbiDefString();
        this.contractCode = cr.getCode();
        this.contractSource = cr.getSource();
        this.languageVersion = cr.getLanguageVersion();
        this.compilerOptions = cr.getCompilerOptions();
        this.compilerVersion = cr.getCompilerVersion();
        this.userDoc = cr.getUserDoc();
        this.developerDoc = cr.getDeveloperDoc();
        this.funcParams = setupAbiFunctionDefinitions(cr.getAbiDefinition());

        this.inputParams = new ArrayList<>();
        this.outputParams = new ArrayList<>();
        this.eventsName = new ArrayList<>();
    }

    // for deployContractWithParam
    //    public Contract(CompileResponse cmplRsp, DeployResponse dplyRsp, IAionAPI api, Address
    // from, String name) {
    //        this.inputParams = new ArrayList<>();
    //        this.outputParams = new ArrayList<>();
    //
    //        this.funcParams = setupAbiFunctionDefinitions(cmplRsp.getAbiDefinition());
    //        if (this.funcParams == null) {
    //            return;
    //        }
    //        this.eventsName = new ArrayList<>();
    //        this.eventsIssued = new LRUMap<>(lrumapSize);
    //        this.eventsIssuedRev = new LRUMap<>(lrumapSize);
    //        reset();
    //  }

    public ContractAbiEntry getAbiFunction() {
        return abiFunc;
    }

    private Map<String, List<ContractAbiEntry>> setupAbiFunctionDefinitions(
            List<ContractAbiEntry> abiDef) {

        if (abiDef == null) {
            throw new NullPointerException();
        }

        Map<String, List<ContractAbiEntry>> functionMap = new HashMap<>();
        for (ContractAbiEntry entry : abiDef) {
            String key;
            if (entry.isConstructor()) {
                key = SC_FN_CONSTRUCTOR;
            } else if (entry.isEvent()) {
                key = entry.name;

                if (this.eventMapping == null) {
                    this.eventMapping = new HashMap<>();
                }

                this.eventMapping.put(entry.getHashed(), key);
            } else {
                key = entry.name;
            }

            List<ContractAbiEntry> l = functionMap.get(key);
            if (l == null) {
                functionMap.put(key, Collections.singletonList(entry));
            } else {
                l.add(entry);
                functionMap.put(key, l);
            }
        }

        return functionMap;
    }

    private static SolidityValue.SolidityTypeEnum getSolType(String type) {

        if (type == null) {
            throw new NullPointerException();
        }

        if (ApiUtils.isTypeAddress(type)) {
            return ADDRESS;
        } else if (ApiUtils.isTypeBoolean(type)) {
            return BOOL;
        } else if (ApiUtils.isTypeBytes(type)) {
            return BYTES;
        } else if (ApiUtils.isTypeDynamicBytes(type)) {
            return SolidityValue.SolidityTypeEnum.DYNAMICBYTES;
        } else if (ApiUtils.isTypeInt(type)) {
            return SolidityValue.SolidityTypeEnum.INT;
        } else if (ApiUtils.isTypeReal(type)) {
            return SolidityValue.SolidityTypeEnum.REAL;
        } else if (ApiUtils.isTypeString(type)) {
            return SolidityValue.SolidityTypeEnum.STRING;
        } else if (ApiUtils.isTypeUint(type)) {
            return SolidityValue.SolidityTypeEnum.UINT;
        } else if (ApiUtils.isTypeUreal(type)) {
            return SolidityValue.SolidityTypeEnum.UREAL;
        } else {
            throw new IllegalArgumentException("unknown solidity type#" + type);
        }
    }

    // END SETTERS & GETTERS
    // resets function call related parameters
    private void reset() {
        this.inputParams.clear();
        this.outputParams.clear();
        this.eventsName.clear();

        this.functionName = "";
        this.functionDefined = false;
        this.functionBuilt = false;
        this.eventDefined = false;
        this.abiFunc = null;
        this.isConstant = true;
        this.isConstructor = false;
        this.txNrgLimit = 0L;
        this.txNrgPrice = 0L;
        this.txValue = BigInteger.ZERO;

        nonBlock = false;
        this.errorCode = 1;
    }

    public Contract newFunction(String f) {

        if (f == null) {
            throw new NullPointerException();
        }

        // reset related entries
        reset();

        if (Objects.equals(f, SC_FN_CONSTRUCTOR)) {
            this.isConstructor = true;
        } else if (this.funcParams.containsKey(f)) {
            this.functionName = f;
            this.isConstant =
                    this.funcParams
                            .get(f)
                            .get(0)
                            .constant; // overloaded function should have the same constant.
            this.functionDefined = true;
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[newFunction] {} function#{}", ErrId.getErrString(-110L), f);
            }
            this.errorCode = -110;
        }

        return this;
    }

    public Contract setParam(ISolidityArg val) {

        if (val == null) {
            throw new NullPointerException();
        }

        if (this.functionDefined || this.isConstructor) {
            this.inputParams.add(val);
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[setParam] {}", ErrId.getErrString(-111L));
            }
            this.errorCode = -111;
        }

        return this;
    }

    TxArgs encodeParams(ContractAbiEntry func) {

        if (func == null) {
            throw new NullPointerException();
        }

        StringBuilder assembled = new StringBuilder();
        if (!this.isConstructor) {
            assembled.append(func.getHashed());
        }

        // offset
        int dynamicOffset = 0;
        int index = 0;

        for (ISolidityArg abs : inputParams) {
            abs.setDynamicParameters(func.inputs.get(index).getParamLengths());
            abs.setType(func.inputs.get(index).getType());
            dynamicOffset += abs.getStaticPartLength();
            index++;
        }

        for (ISolidityArg abs : inputParams) {
            if (abs.getIsDynamic()) {
                assembled.append(ApiUtils.toHexPadded16(ApiUtils.toBytes(dynamicOffset)));
                dynamicOffset += abs.getDynamicOffset();
            } else {
                assembled.append(abs.getInputFormat());
            }
        }

        for (ISolidityArg abs : inputParams) {
            if (abs.getIsDynamic()) {
                assembled.append(abs.getInputFormat());
                dynamicOffset += abs.getDynamicOffset();
            }
        }

        TxArgs.TxArgsBuilder builder =
                new TxArgs.TxArgsBuilder()
                        .value(this.txValue)
                        .nrgPrice(this.txNrgPrice)
                        .nrgLimit(this.txNrgLimit)
                        .from(this.from)
                        .to(isConstructor ? Utils.ZERO_ADDRESS() : this.contractAddress)
                        .data(
                                isConstructor
                                        ? ByteArrayWrapper.wrap(assembled.toString().getBytes())
                                        : ByteArrayWrapper.wrap(
                                                ApiUtils.hex2Bytes(assembled.toString())))
                        .nonce(BigInteger.ZERO);

        this.txArgs = builder.createTxArgs();
        return this.txArgs;
    }

    public Contract setTxNrgLimit(long limit) {

        if (limit < 0) {
            throw new IllegalArgumentException("NrgLimit#" + limit);
        }

        if (this.functionDefined || this.isConstructor) {
            this.txNrgLimit = limit;
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[setTxNrgLimit] {}", ErrId.getErrString(-111L));
            }
            this.errorCode = -111;
        }

        return this;
    }

    public Contract setTxNrgPrice(long price) {

        if (price < 0) {
            throw new IllegalArgumentException("NrgLimit#" + price);
        }

        if (this.functionDefined || this.isConstructor) {
            this.txNrgPrice = price;
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[setTxNrgPrice] {}", ErrId.getErrString(-111L));
            }
            this.errorCode = -111;
        }

        return this;
    }

    public NewAddress getFrom() {
        return this.from;
    }

    public Contract setFrom(NewAddress from) {
        if (from == null) {
            throw new NullPointerException();
        }

        this.from = from;
        return this;
    }

    /**
     * Sets the transaction value for certain functions.
     *
     * @param val the transaction value by long value of the desired transaction.
     * @return Contract object.
     */
    @Override
    public Contract setTxValue(long val) {
        return null;
    }

    public NewAddress getContractAddress() {
        return this.contractAddress;
    }

    /**
     * Get the transaction hash of the deployed contract.
     *
     * @return {@link String String}.
     */
    @Override
    public Hash256 getDeployTxId() {
        return this.deployTxId;
    }

    /**
     * Get the abiDefinition of the deployed contract.
     *
     * @return List of {@link ContractAbiEntry ContractAbiEntry}. The AbiDefinition of the deployed
     *     contract.
     */
    @Override
    public List<ContractAbiEntry> getAbiDefinition() {
        return this.abiDefinition;
    }

    /**
     * Get the abiDefinition of the deployed contract by String object.
     *
     * @return {@link String String}.
     */
    @Override
    public String getAbiDefToString() {
        return this.abiDefStr;
    }

    /**
     * Get compiled contract byte code of the deployed contract to string.
     *
     * @return {@link String String}.
     */
    @Override
    public String getCode() {
        return this.contractCode;
    }

    /**
     * Get contract source contained in the deployed contract.
     *
     * @return {@link String String}.
     */
    @Override
    public String getSource() {
        return this.contractSource;
    }

    /**
     * Get LanguageVersion information in the deployed contract.
     *
     * @return {@link String String}.
     */
    @Override
    public String getLanguageVersion() {
        return this.languageVersion;
    }

    /**
     * Get CompilerVersion information in the deployed contract.
     *
     * @return {@link String String}.
     */
    @Override
    public String getCompilerVersion() {
        return this.compilerVersion;
    }

    /**
     * Get CompilerOptions information in the deployed contract.
     *
     * @return {@link String String}.
     */
    @Override
    public String getCompilerOptions() {
        return this.compilerOptions;
    }

    /**
     * Get UserDoc information in the deployed contract. {@link JsonFmt}.
     *
     * @return return {@link JsonFmt}. The UserDoc field of the deployed contract.
     */
    @Override
    public JsonFmt getUserDoc() {
        return this.userDoc;
    }

    /**
     * Get DeveloperDoc information in the deployed contract.
     *
     * @return return {@link JsonFmt}. The DeveloperDoc field of the deployed contract.
     */
    @Override
    public JsonFmt getDeveloperDoc() {
        return this.developerDoc;
    }

    public ByteArrayWrapper getEncodedData() {
        return this.txArgs.getData();
    }

    public Contract setTxValue(BigInteger val) {

        if (val == null) {
            throw new NullPointerException();
        }

        if (this.functionDefined || this.isConstructor) {
            this.txValue = val;
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[setTxValue] {}", ErrId.getErrString(-111L));
            }
            this.errorCode = -111;
        }
        return this;
    }

    public Contract build() {

        if (!this.functionDefined && !this.isConstructor) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[build] {}", ErrId.getErrString(-111L));
            }
            this.errorCode = -111;
            return this;
        }

        if (!this.isConstant) {
            if (this.txNrgLimit == 0) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[build] {}", ErrId.getErrString(-138L));
                }
                this.errorCode = -138;
                return this;
            }

            if (this.txNrgPrice == 0) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[build] {}", ErrId.getErrString(-139L));
                }
                this.errorCode = -139;
                return this;
            }
        }

        List<ContractAbiEntry> func;
        if (this.isConstructor) {
            func = this.funcParams.get(SC_FN_CONSTRUCTOR);
        } else {
            func = this.funcParams.get(this.functionName);
        }

        List<Integer> al = new ArrayList<>();
        for (int i = 0; i < func.size(); ++i) {
            if (func.get(i).inputs.size() == inputParams.size()) {
                al.add(i);
            }
        }

        int matched_func = -1;
        if (al.isEmpty()) {
            StringBuilder exp = new StringBuilder();
            for (int i = 0; i < func.size(); ++i) {
                if (i > 0) {
                    exp.append("Override function ");
                }
                exp.append(" Expected Input Parameters size: ")
                        .append(func.get(i).inputs.size())
                        .append(" got size: ")
                        .append(inputParams.size())
                        .append("\n");
            }
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[build] {}", exp.toString());
            }
            this.errorCode = -131;
            return this;
        } else {
            // check parameter type have performance issue. Cause function isType using Regular
            // Expression method.
            // replace to type enum compare in later version.
            Boolean sameParams = true;
            for (Integer anAl : al) {
                sameParams = true;
                for (int k = 0; k < func.get(anAl).inputs.size(); ++k) {
                    if (!inputParams.get(k).isType(func.get(anAl).inputs.get(k).getType())) {
                        sameParams = false;
                        break;
                    }
                }

                if (sameParams) {
                    matched_func = anAl;
                    break;
                }
            }

            if (!sameParams) { // all overloaded function unmatched
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[build] {}", ErrId.getErrString(-112L));
                }
                this.errorCode = -112;
                return this;
            }
        }

        this.abiFunc = func.get(matched_func);

        if (this.functionDefined) {
            this.txHash = this.abiFunc.getHashed();
            this.functionBuilt = true;
        }

        if (this.functionBuilt) {
            this.txArgs = encodeParams(this.abiFunc);
            api.getTx().fastTxbuild(this.txArgs, this.abiFunc.constant);
        }

        return this;
    }

    /**
     * Executes the built transaction. Refer to {@link #newFunction(String)} for function use.
     *
     * @return returns {@link org.aion.api.type.ContractResponse} containing all relevant
     *     information
     */
    public ApiMsg execute() {

        if (this.error()) {
            return new ApiMsg(this.errorCode);
        }

        if (!this.functionBuilt) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[execute] {}", ErrId.getErrString(-113L));
            }
            return new ApiMsg(-113);
        }
        // execute
        if (this.isConstant) {
            // call
            ApiMsg apiMsg = this.api.getTx().call(this.txArgs);

            if (apiMsg.isError()) {
                return apiMsg;
            }

            byte[] rsp = apiMsg.getObject();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[execute] Contract msg: [{}]", IUtils.bytes2Hex(rsp));
            }
            List out = decodeParams(ByteArrayWrapper.wrap(rsp));

            ContractResponse.ContractResponseBuilder builder =
                    new ContractResponse.ContractResponseBuilder()
                            .data(out)
                            .constant(true)
                            .msgHash(ByteArrayWrapper.wrap(new byte[ApiUtils.MSG_HASH_LEN]))
                            .status((byte) 0)
                            .txHash(Hash256.ZERO_HASH());

            return apiMsg.set(
                    builder.createContractResponse(), org.aion.api.type.ApiMsg.cast.OTHERS);
        } else {
            // send transaction
            ApiMsg apiMsg =
                    (nonBlock
                            ? api.getTx().nonBlock().sendTransaction(null)
                            : api.getTx().sendTransaction(null));

            if (apiMsg.isError()) {
                return apiMsg;
            }

            MsgRsp msgRsp = apiMsg.getObject();

            List data = null;
            if (msgRsp.getTxResult() != null) {
                data = decodeParams(msgRsp.getTxResult());
            }

            ContractResponse.ContractResponseBuilder builder =
                    new ContractResponse.ContractResponseBuilder()
                            .data(data)
                            .constant(false)
                            .msgHash(msgRsp.getMsgHash())
                            .status(msgRsp.getStatus())
                            .txHash(msgRsp.getTxHash())
                            .error(msgRsp.getError());

            return apiMsg.set(
                    builder.createContractResponse(), org.aion.api.type.ApiMsg.cast.OTHERS);
        }
    }

    // TODO: ArrayList<Integer> vs int[]?
    private int[] getOffsets() {
        int[] ret = new int[this.outputParams.size()];

        for (int i = 1; i < this.outputParams.size(); i++) {
            ret[i] += ret[i - 1] + this.outputParams.get(i - 1).getStaticPartLength();
        }

        return ret;
    }

    private List decodeParams(ByteArrayWrapper data) {
        this.outputParams.clear();

        for (ContractAbiIOParam io : this.abiFunc.outputs) {
            switch (getSolType(io.getType())) {
                case ADDRESS:
                    org.aion.api.sol.impl.Address addressVal =
                            org.aion.api.sol.impl.Address.createForDecode();
                    addressVal.setDynamicParameters(io.getParamLengths());
                    addressVal.setType(io.getType());
                    this.outputParams.add(addressVal);
                    break;
                case BOOL:
                    Bool boolVal = Bool.createForDecode();
                    boolVal.setDynamicParameters(io.getParamLengths());
                    boolVal.setType(io.getType());
                    this.outputParams.add(boolVal);
                    break;
                case BYTES:
                    Bytes bytesVal = Bytes.createForDecode();
                    bytesVal.setDynamicParameters(io.getParamLengths());
                    bytesVal.setType(io.getType());
                    this.outputParams.add(bytesVal);
                    break;
                case DYNAMICBYTES:
                    DynamicBytes dynamicBytesVal = DynamicBytes.createForDecode();
                    dynamicBytesVal.setDynamicParameters(io.getParamLengths());
                    dynamicBytesVal.setType(io.getType());
                    this.outputParams.add(dynamicBytesVal);
                    break;
                case INT:
                    Int intVal = Int.createForDecode();
                    intVal.setDynamicParameters(io.getParamLengths());
                    intVal.setType(io.getType());
                    this.outputParams.add(intVal);
                    break;
                    // TODO: currently not support
                    //            case REAL:
                    //                Real realVal = Real.createForDecode();
                    //                realVal.setDynamicParameters(io.getParamLengths());
                    //                realVal.setType(io.getType());
                    //                this.outputParams.add(realVal);
                    //                break;
                case STRING:
                    SString stringVal = SString.createForDecode();
                    stringVal.setDynamicParameters(io.getParamLengths());
                    stringVal.setType(io.getType());
                    this.outputParams.add(stringVal);
                    break;
                case UINT:
                    Uint uintVal = Uint.createForDecode();
                    uintVal.setDynamicParameters(io.getParamLengths());
                    uintVal.setType(io.getType());
                    this.outputParams.add(uintVal);
                    break;
                    // TODO: currently not support
                    //            case UREAL:
                    //                Ureal urealVal = Ureal.createForDecode();
                    //                urealVal.setDynamicParameters(io.getParamLengths());
                    //                urealVal.setType(io.getType());
                    //                this.outputParams.add(urealVal);
                    //                break;
                default:
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("[decodeParams] {}", ErrId.getErrString(-125L));
                    }
                    throw new IllegalArgumentException("Unsupported solidity type");
            }
        }

        int[] offsets = getOffsets();
        ArrayList outArray = new ArrayList();

        int count = 0;
        for (ISolidityArg out : outputParams) {
            int offset = offsets[count++];
            outArray.add(out.decode(offset, data));
        }

        return outArray;
    }

    private List decodeParams(String event, ByteArrayWrapper data) {

        if (event == null || data == null) {
            throw new NullPointerException(
                    "event#" + String.valueOf(event) + " data#" + String.valueOf(data));
        }

        this.outputParams.clear();
        List<ContractAbiEntry> abi = this.funcParams.get(event);

        if (abi == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("can't find the input event name.");
            }

            return null;
        }

        for (ContractAbiEntry e : abi) {
            for (ContractAbiIOParam io : e.inputs) {
                switch (getSolType(io.getType())) {
                    case ADDRESS:
                        org.aion.api.sol.impl.Address addressVal =
                                org.aion.api.sol.impl.Address.createForDecode();
                        addressVal.setDynamicParameters(io.getParamLengths());
                        addressVal.setType(io.getType());
                        this.outputParams.add(addressVal);
                        break;
                    case BOOL:
                        Bool boolVal = Bool.createForDecode();
                        boolVal.setDynamicParameters(io.getParamLengths());
                        boolVal.setType(io.getType());
                        this.outputParams.add(boolVal);
                        break;
                    case BYTES:
                        Bytes BytesVal = Bytes.createForDecode();
                        BytesVal.setDynamicParameters(io.getParamLengths());
                        BytesVal.setType(io.getType());
                        this.outputParams.add(BytesVal);
                        break;
                    case DYNAMICBYTES:
                        DynamicBytes DynamicBytesVal = DynamicBytes.createForDecode();
                        DynamicBytesVal.setDynamicParameters(io.getParamLengths());
                        DynamicBytesVal.setType(io.getType());
                        this.outputParams.add(DynamicBytesVal);
                        break;
                    case INT:
                        Int IntVal = Int.createForDecode();
                        IntVal.setDynamicParameters(io.getParamLengths());
                        IntVal.setType(io.getType());
                        this.outputParams.add(IntVal);
                        break;
                        //                case REAL:
                        //                    Real RealVal = Real.createForDecode();
                        //                    RealVal.setDynamicParameters(io.getParamLengths());
                        //                    RealVal.setType(io.getType());
                        //                    this.outputParams.add(RealVal);
                        //                    break;
                    case STRING:
                        SString StringVal = SString.createForDecode();
                        StringVal.setDynamicParameters(io.getParamLengths());
                        StringVal.setType(io.getType());
                        this.outputParams.add(StringVal);
                        break;
                    case UINT:
                        Uint UintVal = Uint.createForDecode();
                        UintVal.setDynamicParameters(io.getParamLengths());
                        UintVal.setType(io.getType());
                        this.outputParams.add(UintVal);
                        break;
                        //                case UREAL:
                        //                    Ureal UrealVal = Ureal.createForDecode();
                        //                    UrealVal.setDynamicParameters(io.getParamLengths());
                        //                    UrealVal.setType(io.getType());
                        //                    this.outputParams.add(UrealVal);
                        //                    break;
                    default:
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("[decodeParams] {}", ErrId.getErrString(-125L));
                        }
                        return null;
                }
            }
        }

        int[] offsets = getOffsets();
        ArrayList outArray = new ArrayList();
        int count = 0;
        for (ISolidityArg arg : this.outputParams) {
            int offset = offsets[count++];
            outArray.add(arg.decode(offset, data));
        }

        return outArray;
    }

    public Contract nonBlock() {
        nonBlock = true;
        return this;
    }

    /**
     * Represent the isError code of the contract execution result.
     *
     * @return int value.
     */
    @Override
    public int getErrorCode() {
        return 0;
    }

    //    public void setErrorCode(int errorCode) {
    //        this.errorCode = errorCode;
    //    }

    public boolean error() {
        return (this.errorCode != 1);
    }

    /**
     * Get the contract function input parameters.
     *
     * @return list of SolidityAbstractType, need to cast to response solidity type by contract
     *     function.
     */
    @Override
    public List<ISolidityArg> getInputParams() {
        return this.inputParams;
    }

    /**
     * Get the contract function output parameters.
     *
     * @return list of SolidityAbstractType, need to cast to response solidity type by contract
     *     function.
     */
    @Override
    public List<ISolidityArg> getOutputParams() {
        return this.outputParams;
    }

    public String getErrString() {
        return ErrId.getErrString(this.errorCode);
    }

    public List<String> issuedEvents() {
        return new ArrayList<>(this.eventsIssued.keySet());
    }

    @Override
    public String getContractName() {
        return contractName;
    }

    public Contract newEvent(String e) {
        return newEvents(Collections.singletonList(e));
    }

    public Contract newEvents(List<String> e) {

        if (e == null) {
            throw new NullPointerException();
        }

        // reset related entries
        reset();

        for (String s : e) {
            if (this.funcParams.containsKey(s)) {
                this.eventsName.add(s);
            }
        }

        if (this.eventsName.isEmpty()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[newEvents] {}", ErrId.getErrString(-135L));
            }
            this.errorCode = -135;
        } else {
            this.eventDefined = true;
        }

        return this;
    }

    public Contract allEvents() {

        reset();

        for (Map.Entry<String, List<ContractAbiEntry>> entry : this.funcParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            for (ContractAbiEntry e : entry.getValue()) {
                if (e.isEvent()) {
                    this.eventsName.add(e.name);
                }
            }
        }

        if (this.eventsName.isEmpty()) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[newEvent] {}", ErrId.getErrString(-135L));
            }
            this.errorCode = -135;
        } else {
            this.eventDefined = true;
        }

        return this;
    }

    public ApiMsg register() {
        return register("latest");
    }

    public ApiMsg register(String s) {

        if (s == null) {
            throw new NullPointerException();
        }

        // if s doesn't equal to number, latest or pending, return isError code.
        if (!validString(s)) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[register] {}", ErrId.getErrString(-133L));
            }
            this.errorCode = -133;
            return new ApiMsg(this.errorCode);
        }

        ContractEventFilter.ContractEventFilterBuilder builder =
                new ContractEventFilter.ContractEventFilterBuilder()
                        .addresses(new ArrayList<>())
                        .expireTime(0)
                        .fromBlock(s)
                        .toBlock(s)
                        .topics(new ArrayList<>(this.eventMapping.values()));

        return register(builder.createContractEventFilter());
    }

    private boolean validString(String s) {

        return s != null && (s.equals("latest") || s.equals("pending") || s.matches(REGEX_NUMERIC));
    }

    public ApiMsg register(ContractEventFilter ef) {

        if (ef == null) {
            throw new NullPointerException();
        }

        if (!this.eventDefined) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[register] {}", ErrId.getErrString(-136L));
            }
            this.errorCode = -136;
            return new ApiMsg(this.errorCode);
        }

        Map<String, String> evtHash = new HashMap<>();
        for (String s : this.eventsName) {
            for (ContractAbiEntry e : this.funcParams.get(s)) {
                evtHash.put(s, e.getHashed());
            }
        }

        ApiMsg msg =
                new ApiMsg()
                        .set(
                                this.api
                                        .getTx()
                                        .eventRegister(
                                                new ArrayList<>(evtHash.values()),
                                                ef,
                                                this.getContractAddress()));

        if (!msg.isError()) {
            for (Map.Entry<String, String> ss : evtHash.entrySet()) {
                if (this.eventsIssued.get(ss.getKey()) == null) {
                    this.eventsIssued.put(ss.getKey(), ss.getValue());
                    this.eventsIssuedRev.put(ss.getValue(), ss.getKey());
                }

                ((Tx) this.api.getTx()).apiInst.msgExecutor.setEvent(ss.getValue());
            }
        }

        this.eventsName.clear();
        return msg;
    }

    public ApiMsg deregister(List<String> e) {

        if (e == null) {
            throw new NullPointerException();
        }

        if (e.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Map<String, String> evtHash = new HashMap<>();
        for (String s : e) {
            for (ContractAbiEntry entry : funcParams.get(s)) {
                evtHash.put(s, entry.getHashed());
            }
        }

        ApiMsg msg =
                new ApiMsg()
                        .set(
                                this.api
                                        .getTx()
                                        .eventDeregister(
                                                new ArrayList<>(evtHash.values()),
                                                this.getContractAddress()));

        if (!msg.isError()) {
            for (String s : e) {
                ((Tx) this.api.getTx()).apiInst.msgExecutor.removeEvent(this.eventsIssued.get(s));
                String val = this.eventsIssued.get(s);
                this.eventsIssuedRev.remove(val);
                this.eventsIssued.remove(s);
            }
        }

        this.eventsName.clear();
        return msg;
    }

    public ApiMsg deregisterAll() {

        Map<String, String> dr = new HashMap<>();
        for (Map.Entry<String, List<ContractAbiEntry>> entry : this.funcParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            for (ContractAbiEntry e : entry.getValue()) {
                if (e.isEvent()) {
                    dr.put(e.name, e.getHashed());
                }
            }
        }

        ApiMsg msg =
                new ApiMsg()
                        .set(
                                this.api
                                        .getTx()
                                        .eventDeregister(
                                                new ArrayList<>(dr.values()),
                                                this.contractAddress));

        if (!msg.isError()) {
            this.eventsIssued.clear();
            this.eventsIssuedRev.clear();
            ((Tx) this.api.getTx()).apiInst.msgExecutor.removeAllEvents();
        }

        this.eventsName.clear();
        return msg;
    }

    public ApiMsg call() {
        if (this.error()) {
            return new ApiMsg(this.errorCode);
        }

        if (!this.functionBuilt) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[contract.call] {}", ErrId.getErrString(-113L));
            }
            return new ApiMsg(-113);
        }

        // call
        ApiMsg apiMsg = this.api.getTx().call(this.txArgs);

        if (apiMsg.isError()) {
            return apiMsg;
        }

        byte[] rsp = apiMsg.getObject();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[contract.call] Contract msg: [{}]", IUtils.bytes2Hex(rsp));
        }
        List out = decodeParams(ByteArrayWrapper.wrap(rsp));

        ContractResponse.ContractResponseBuilder builder =
                new ContractResponse.ContractResponseBuilder()
                        .data(out)
                        .constant(true)
                        .msgHash(ByteArrayWrapper.wrap(new byte[ApiUtils.MSG_HASH_LEN]))
                        .status((byte) 0)
                        .txHash(Hash256.ZERO_HASH());

        return apiMsg.set(builder.createContractResponse(), org.aion.api.type.ApiMsg.cast.OTHERS);
    }

    @SuppressWarnings("unchecked")
    public List<ContractEvent> getEvents() {
        List<ContractEvent> evts =
                (List<ContractEvent>)
                        (List<?>)
                                (this.api.getTx() != null
                                        ? ((Tx) this.api.getTx())
                                                .apiInst.msgExecutor.getEvents(
                                                        new ArrayList<>(this.eventsIssued.values()))
                                        : null);

        if (evts == null) {
            return new ArrayList<>();
        }

        List<ContractEvent> res = new ArrayList<>();
        for (ContractEvent evt : evts) {
            String evtName = this.eventsIssuedRev.get(evt.getEventName());
            if (evtName != null) {
                ContractEvent.ContractEventBuilder builder =
                        new ContractEvent.ContractEventBuilder(evt)
                                .eventName(evtName)
                                .results(this.decodeParams(evtName, evt.getData()));
                res.add(builder.createContractEvent());
            }
        }

        return res;
    }

    // public ApiMsg queryEvents(ContractEventFilter ef) {
    //
    //    ContractEventFilter.ContractEventFilterBuilder efBuilder = new
    // ContractEventFilter.ContractEventFilterBuilder(ef);
    //    if (ef.getFromBlock().contains("latest")) {
    //        if (Integer.parseInt(ef.getToBlock()) > 50) {
    //            if (LOGGER.isInfoEnabled()) {
    //                LOGGER.info("[queryEvents] {}", "the fromblock is more than 50 blocks");
    //            }
    //            efBuilder.fromBlock("50");
    //        }
    //    }
    //
    //    List<String> hashedTopics = new ArrayList<>();
    //    if (ef.getTopics() != null) {
    //        for (String topic : ef.getTopics()) {
    //            for (ContractAbiEntry e : this.funcParams.get(topic)) {
    //                hashedTopics.add(e.getHashed());
    //            }
    //        }
    //    }
    //
    //    efBuilder.topics(hashedTopics);
    //
    //    ApiMsg msg = this.api.getTx().queryEvents(efBuilder.createContractEventFilter(),
    // this.contractAddress);
    //    if (msg.isError()) {
    //        return msg;
    //    }
    //
    //    List<ContractEvent> evts = msg.getObject();
    //
    //    List<ContractEvent> res = new ArrayList<>();
    //    for (ContractEvent evt : evts) {
    //        if (this.eventMapping.get(evt.getEventName()) != null) {
    //            ContractEvent.ContractEventBuilder eBuilder = new
    // ContractEvent.ContractEventBuilder(evt)
    //                    .eventName(this.eventMapping.get(evt.getEventName()))
    //                    .results(this.decodeParams(evt.getEventName(), evt.getData()));
    //
    //            res.add(eBuilder.createContractEvent());
    //        }
    //
    //    }
    //
    //    return msg.set(1, res, ApiMsg.cast.OTHERS);
    // }

    public List<String> getContractEventList() {
        if (this.eventMapping == null) {
            return new ArrayList<>();
        }

        List<String> rtn = new ArrayList<>();
        for (Map.Entry<String, String> e : eventMapping.entrySet()) {
            rtn.add(e.getValue());
        }

        return rtn;
    }
}

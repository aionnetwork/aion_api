package org.aion.api.type;

import java.util.List;

/**
 * Represents an internal representation of a single entry in the JSON ABI Contains all relevant
 * information pertaining to that entry. Typically the API does not require the manual creation of
 * such a class, instead it is returned by {@link org.aion.api.IContract IContract} or {@link
 * org.aion.api.ITx#compile(java.lang.String) compile} related API.
 *
 * @author Jay Tseng
 */
public final class ContractAbiEntry {

    // original abi structure
    public boolean constant;
    public boolean anonymous;
    public boolean payable;
    public String type;
    public String name;
    public List<ContractAbiIOParam> inputs;
    public List<ContractAbiIOParam> outputs;

    // additional member
    private boolean isEvent;
    private boolean isConstructor;
    private String hashed;
    private boolean isFallback;

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int _lv) {
        StringBuilder lv = new StringBuilder();
        int level = _lv;
        while (level-- > 0) {
            lv.append("  ");
        }

        StringBuilder sb =
                new StringBuilder()
                        .append(lv)
                        .append("constant: ")
                        .append(String.valueOf(constant))
                        .append(",\n")
                        .append(lv)
                        .append("anonymous: ")
                        .append(String.valueOf(anonymous))
                        .append(",\n")
                        .append(lv)
                        .append("payable: ")
                        .append(String.valueOf(payable))
                        .append(",\n")
                        .append(lv)
                        .append("type: ")
                        .append(type)
                        .append(",\n")
                        .append(lv)
                        .append("name: ")
                        .append(name)
                        .append(",\n")
                        .append(lv)
                        .append("inputs: ")
                        .append("\n");

        int cnt = inputs.size();
        for (ContractAbiIOParam io : inputs) {
            sb.append(lv)
                    .append("[")
                    .append("\n")
                    .append(lv)
                    .append(io.toString(++_lv))
                    .append("\n")
                    .append(lv)
                    .append("]");

            if (--cnt > 0) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append(lv).append("outputs: ").append("\n");
        cnt = outputs.size();
        for (ContractAbiIOParam io : outputs) {
            sb.append(lv)
                    .append("[")
                    .append("\n")
                    .append(lv)
                    .append(io.toString(_lv))
                    .append("\n")
                    .append(lv)
                    .append("]");

            if (--cnt > 0) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append(lv)
                .append("isEvent: ")
                .append(String.valueOf(isEvent))
                .append(",\n")
                .append(lv)
                .append("isConstructor: ")
                .append(String.valueOf(isConstructor))
                .append(",\n")
                .append(lv)
                .append("isFallback: ")
                .append(String.valueOf(isFallback))
                .append(",\n")
                .append(lv)
                .append("hashed: ")
                .append(hashed)
                .append("\n");

        return sb.toString();
    }

    public boolean isEvent() {
        return isEvent;
    }

    public void setEvent(boolean event) {
        isEvent = event;
    }

    public String getHashed() {
        return hashed;
    }

    void setHashed(String hashed) {
        this.hashed = hashed;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    void setConstructor() {
        isConstructor = true;
    }

    public boolean isFallback() {
        return isFallback;
    }

    void setFallback() {
        isFallback = true;
    }

    // original abi structure
    //    private final boolean constant;
    //    private final boolean anonymous;
    //    private final boolean payable;
    //    private final String type;
    //    private final String name;
    //    private final List<ContractAbiIOParam> inputs;
    //    private final List<ContractAbiIOParam> outputs;
    //
    //    // addtional member
    //    private final boolean isEvent;
    //    private final String hashed;

    //    private ContractAbiEntry(AbiEntryBuilder builder) {
    //        this.constant = builder.constant;
    //        this.isEvent = builder.isEvent;
    //        this.anonymous = builder.isAnonymous;
    //        this.payable = builder.payable;
    //        this.name = builder.name;
    //        this.type = builder.type;
    //        this.hashed = builder.hashed;
    //        this.inputs = builder.inputParams;
    //        this.outputs = builder.outputParams;
    //    }
    //
    //    public boolean getConstant() {
    //        return constant;
    //    }
    //
    //    public boolean getEvent() {
    //        return isEvent;
    //    }
    //
    //    public boolean getAnonymous() {
    //        return anonymous;
    //    }
    //
    //    public boolean getPayable() {
    //        return payable;
    //    }
    //
    //    public String getName() {
    //        return name;
    //    }
    //
    //    public String getType() {
    //        return type;
    //    }
    //
    //    public String getHashed() {
    //        return hashed;
    //    }
    //
    //    public List<ContractAbiIOParam> getInputs() {
    //        return inputs;
    //    }
    //
    //    public List<ContractAbiIOParam> getOutputs() {
    //        return outputs;
    //    }
    //
    //    /**
    //     * This Builder class is used to build a {@link ContractAbiEntry} instance.
    //     */
    //    public static class AbiEntryBuilder {
    //        private boolean constant;
    //        private boolean isEvent;
    //        private boolean isAnonymous;
    //        private boolean payable;
    //        private String name;
    //        private String type;
    //        private String hashed;
    //        private List<ContractAbiIOParam> inputParams;
    //        private List<ContractAbiIOParam> outputParams;
    //
    //        public AbiEntryBuilder() {}
    //
    //        public AbiEntryBuilder constant(final boolean constant) {
    //            this.constant = constant;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder isEvent(final boolean isEvent) {
    //            this.isEvent = isEvent;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder isAnonymous(final boolean isAnonymous) {
    //            this.isAnonymous = isAnonymous;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder payable(final boolean payable) {
    //            this.payable = payable;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder name(final String name) {
    //            this.name = name;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder type(final String type) {
    //            this.type = type;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder hashed(final String hashed) {
    //            this.hashed = hashed;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder inputParams(final List inputParams) {
    //            this.inputParams = inputParams;
    //            return this;
    //        }
    //
    //        public AbiEntryBuilder outputParams(final List outputParams) {
    //            this.outputParams = outputParams;
    //            return this;
    //        }
    //
    //        public ContractAbiEntry createAbiFunctionEntry() {
    //            if (name == null || hashed == null || inputParams == null || outputParams == null
    // || type == null) {
    //                throw new NullPointerException(
    //                        "name#" + String.valueOf(name) +
    //                                " type#" + String.valueOf(type) +
    //                                " hashed#" + String.valueOf(hashed) +
    //                                " outputs#" + String.valueOf(outputParams) +
    //                                " inputs#" + String.valueOf(inputParams));
    //            }
    //
    //            return new ContractAbiEntry(this);
    //        }
    //    }
}

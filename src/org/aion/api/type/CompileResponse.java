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

package org.aion.api.type;

import static org.aion.api.impl.Contract.ELEMENT_PATTERN;
import static org.aion.api.impl.Contract.SC_FN_CONSTRUCTOR;
import static org.aion.api.impl.Contract.SC_FN_EVENT;
import static org.aion.api.impl.Contract.SC_FN_FALLBACK;
import static org.aion.api.impl.Contract.SC_FN_FUNC;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import org.aion.api.IUtils;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.slf4j.Logger;

/**
 * Contains all relevant information to compile responses, note that some parameters are not yet
 * utilized, but may contain information in future versions. Utilized by {@link
 * org.aion.api.ITx#compile(java.lang.String) compile}.
 *
 * @author Jay Tseng
 */
public final class CompileResponse {

    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.CNT.name());

    private final String code;
    private final String source;
    private final String language;
    private final String languageVersion;
    private final String compilerVersion;
    private final String compilerOptions;
    private final String abiDefString;
    private final List<ContractAbiEntry> abiDefinition;
    private final JsonFmt userDoc;
    private final JsonFmt developerDoc;

    private CompileResponse(CompileResponseBuilder builder) {
        this.code = builder.code;
        this.source = builder.source;
        this.language = builder.language;
        this.languageVersion = builder.languageVersion;
        this.compilerVersion = builder.compilerVersion;
        this.compilerOptions = builder.compilerOptions;
        this.abiDefString = builder.abiDefString;
        this.abiDefinition = builder.abiDefinition;
        this.userDoc = builder.userDoc;
        this.developerDoc = builder.developerDoc;
    }

    public String getCode() {
        return code;
    }

    public String getSource() {
        return source;
    }

    public String getLanguage() {
        return language;
    }

    public String getLanguageVersion() {
        return languageVersion;
    }

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public String getCompilerOptions() {
        return compilerOptions;
    }

    public String getAbiDefString() {
        return abiDefString;
    }

    public List<ContractAbiEntry> getAbiDefinition() {
        return abiDefinition;
    }

    public JsonFmt getUserDoc() {
        return userDoc;
    }

    public JsonFmt getDeveloperDoc() {
        return developerDoc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("code :")
                .append(code)
                .append(",\n")
                .append("source :")
                .append(source)
                .append(",\n")
                .append("language :")
                .append(language)
                .append(",\n")
                .append("languageVersion: ")
                .append(languageVersion)
                .append(",\n")
                .append("compilerVersion: ")
                .append(compilerVersion)
                .append(",\n")
                .append("compilerOption: ")
                .append(compilerOptions)
                .append(",\n")
                .append("abiDefString: ")
                .append(abiDefString)
                .append(",\n")
                .append("abiDefinition: ")
                .append("\n");

        int cnt = abiDefinition.size();
        for (ContractAbiEntry e : abiDefinition) {
            sb.append("[").append("\n").append(e.toString(0)).append("\n").append("]");

            if (--cnt > 0) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append("userDoc: ")
                .append(userDoc.toString())
                .append(",\n")
                .append("developerDoc")
                .append(developerDoc.toString())
                .append("\n");

        return sb.toString();
    }

    /** This Builder class is used to build a {@link CompileResponse} instance. */
    public static class CompileResponseBuilder {

        private String code;
        private String source;
        private String language;
        private String languageVersion;
        private String compilerVersion;
        private String compilerOptions;
        private String abiDefString;
        private List<ContractAbiEntry> abiDefinition;
        private JsonFmt userDoc;
        private JsonFmt developerDoc;

        public CompileResponseBuilder() {}

        public CompileResponse.CompileResponseBuilder code(final String code) {
            this.code = code;
            return this;
        }

        public CompileResponse.CompileResponseBuilder source(final String source) {
            this.source = source;
            return this;
        }

        public CompileResponse.CompileResponseBuilder language(final String language) {
            this.language = language;
            return this;
        }

        public CompileResponse.CompileResponseBuilder languageVersion(
                final String languageVersion) {
            this.languageVersion = languageVersion;
            return this;
        }

        public CompileResponse.CompileResponseBuilder compilerVersion(
                final String compilerVersion) {
            this.compilerVersion = compilerVersion;
            return this;
        }

        public CompileResponse.CompileResponseBuilder compilerOptions(
                final String compilerOptions) {
            this.compilerOptions = compilerOptions;
            return this;
        }

        public CompileResponse.CompileResponseBuilder abiDefString(final String abiDefString) {
            this.abiDefString = abiDefString;
            return this;
        }

        public CompileResponse.CompileResponseBuilder abiDefinition(
                final List<ContractAbiEntry> abiDefinition) {

            List<ContractAbiEntry> abiDef = new ArrayList<>();
            for (ContractAbiEntry entry : abiDefinition) {
                switch (entry.type) {
                    case SC_FN_EVENT:
                        entry.setEvent(true);
                        entry.setHashed(
                                Objects.requireNonNull(
                                                IUtils.bytes2Hex(
                                                        ApiUtils.KC_256.digest(
                                                                assembleFunctionFn(entry)
                                                                        .getBytes())))
                                        .substring(0, 64));
                        break;
                    case SC_FN_FUNC:
                        entry.setHashed(
                                Objects.requireNonNull(
                                                IUtils.bytes2Hex(
                                                        ApiUtils.KC_256.digest(
                                                                assembleFunctionFn(entry)
                                                                        .getBytes())))
                                        .substring(0, 8));
                        break;
                    case SC_FN_CONSTRUCTOR:
                        entry.setConstructor();
                        break;
                    case SC_FN_FALLBACK:
                        entry.setFallback();
                        break;
                    default:
                        throw new IllegalArgumentException("entry.type#" + entry.type);
                }

                List<ContractAbiIOParam> in = new ArrayList<>();
                if (entry.inputs != null) {
                    for (ContractAbiIOParam io : entry.inputs) {
                        io.setParamLengths(setParametersList(io.getType()));
                        in.add(io);
                    }
                }
                entry.inputs = in;

                List<ContractAbiIOParam> out = new ArrayList<>();

                if (entry.outputs != null) {
                    for (ContractAbiIOParam io : entry.outputs) {
                        io.setParamLengths(setParametersList(io.getType()));
                        out.add(io);
                    }
                }

                entry.outputs = out;
                abiDef.add(entry);
            }

            this.abiDefinition = abiDef;
            return this;
        }

        public CompileResponse.CompileResponseBuilder userDoc(final JsonFmt userDoc) {
            this.userDoc = userDoc;
            return this;
        }

        public CompileResponse.CompileResponseBuilder developerDoc(final JsonFmt developerDoc) {
            this.developerDoc = developerDoc;
            return this;
        }

        public CompileResponse createCompileResponse() {

            if (code == null
                    || source == null
                    || language == null
                    || languageVersion == null
                    || compilerVersion == null
                    || compilerOptions == null
                    || abiDefString == null
                    || abiDefinition == null
                    || userDoc == null
                    || developerDoc == null) {
                throw new NullPointerException(
                        "code#"
                                + String.valueOf(code)
                                + " source#"
                                + String.valueOf(source)
                                + " language#"
                                + String.valueOf(language)
                                + " languageVersion#"
                                + String.valueOf(languageVersion)
                                + " compilerVersion#"
                                + String.valueOf(compilerVersion)
                                + " compilerOptions#"
                                + String.valueOf(compilerOptions)
                                + " abiDefString#"
                                + String.valueOf(abiDefString)
                                + " abiDefinition#"
                                + String.valueOf(abiDefinition)
                                + " userDoc#"
                                + String.valueOf(userDoc)
                                + " developerDoc#"
                                + String.valueOf(developerDoc));
            }

            return new CompileResponse(this);
        }

        static String assembleFunctionFn(ContractAbiEntry abiEntry) {

            StringBuilder sb = new StringBuilder(abiEntry.name);
            sb.append('(');

            int abiSize = abiEntry.inputs.size();
            for (int i = 0; i < abiSize; i++) {
                sb.append(abiEntry.inputs.get(i).getType());
                if (i != (abiSize - 1)) {
                    sb.append(",");
                }
            }

            sb.append(')');
            return sb.toString();
        }

        private List<Integer> setParametersList(String in) {
            final Matcher m = ELEMENT_PATTERN.matcher(in);
            List<Integer> dParams = new ArrayList<>();

            while (m.find()) {
                if (!m.group(1).equals("")) {
                    dParams.add(Integer.parseInt(m.group(1)));
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[setParamList] Unsupported parameter type?");
                    }
                    dParams.add(-1);
                }
            }
            return dParams;
        }
    }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Internal type used in {@link org.aion.api.IContract#build() build}, not user facing.
 *
 * @author Jay Tseng
 */

public final class JsonFmt {

    private final boolean constant;
    private final String name;
    private final String type;
    private final Map<String, String> inputs;
    private final Map<String, String> outputs;

    private JsonFmt(JsonFmtBuilder builder) {
        this.constant = builder.constant;
        this.name = builder.name;
        this.type = builder.type;
        this.inputs = builder.inputs;
        this.outputs = builder.outputs;
    }

    private JsonFmt() {
        constant = false;
        name = "";
        type = "";
        inputs = new HashMap<>();
        outputs = new HashMap<>();
    }

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

        StringBuilder sb = new StringBuilder()
            .append(lv).append("constant: ").append(String.valueOf(constant)).append(",\n")
            .append(lv).append("name: ").append(name).append(",\n")
            .append(lv).append("type: ").append(type).append(",\n")
            .append(lv).append("inputs: ").append("\n");

        int cnt = inputs.size();
        for (Entry e : inputs.entrySet()) {
            sb.append(lv).append("[").append("\n")
                .append(lv).append(e.getKey()).append(",").append(e.getValue()).append("\n")
                .append(lv).append("]");

            if (--cnt > 0) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append(lv).append("outputs: ").append("\n");
        cnt = outputs.size();
        for (Entry e : outputs.entrySet()) {
            sb.append(lv).append("[").append("\n")
                .append(lv).append(e.getKey()).append(",").append(e.getValue()).append("\n")
                .append(lv).append("]");

            if (--cnt > 0) {
                sb.append(",");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public boolean isConstant() {
        return constant;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    /**
     * This Builder class is used to build a {@link JsonFmt} instance.
     */
    public static class JsonFmtBuilder {

        private boolean constant;
        private String name;
        private String type;
        private Map<String, String> inputs;
        private Map<String, String> outputs;

        public JsonFmtBuilder() {
        }

        public static JsonFmt emptyJsonFmt() {
            return new JsonFmt();
        }

        public JsonFmt.JsonFmtBuilder constant(final boolean constant) {
            this.constant = constant;
            return this;
        }

        public JsonFmt.JsonFmtBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public JsonFmt.JsonFmtBuilder type(final String type) {
            this.type = type;
            return this;
        }

        public JsonFmt.JsonFmtBuilder inputs(final Map<String, String> inputs) {
            this.inputs = inputs;
            return this;
        }

        public JsonFmt.JsonFmtBuilder outputs(final Map<String, String> outputs) {
            this.outputs = outputs;
            return this;
        }

        public JsonFmt createJsonFmt() {
            if (name == null || type == null || inputs == null || outputs == null) {
                throw new NullPointerException(
                    "name#" + String.valueOf(name) +
                        " type#" + String.valueOf(type) +
                        " inputs#" + String.valueOf(inputs) +
                        " outputs#" + String.valueOf(outputs));
            }
            return new JsonFmt(this);
        }
    }
}

package org.aion.api.cfg;

import org.aion.api.log.LogEnum;
import org.aion.api.log.LogLevels;

public class CfgLogModule {

    protected String label;
    protected String level;

    public CfgLogModule() {}

    public CfgLogModule(String _label, String _level) {
        setLabel(_label);
        setLevel(_level);
    }

    // getters
    public String getLabel() {
        return this.label;
    }

    // setters
    public void setLabel(String _label) {
        if (LogEnum.contains(_label)) {
            this.label = _label;
        } else {
            this.label = LogEnum.GEN.name();
        }
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String _level) {
        if (LogLevels.contains(_level)) {
            this.level = _level;
        } else {
            this.level = LogLevels.INFO.name();
        }
    }
}

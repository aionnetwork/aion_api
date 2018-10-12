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

package org.aion.api.cfg;

import org.aion.api.log.LogEnum;
import org.aion.api.log.LogLevels;


public class CfgLogModule {

    protected String label;
    protected String level;

    public CfgLogModule() {

    }

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
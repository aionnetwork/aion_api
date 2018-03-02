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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
public class CfgLog {

    // xml properties
    @XmlElement(name = "module", required = true)
    private List<CfgLogModule> modules;

    // init
    public CfgLog() {
        modules = new ArrayList<>();
        modules.add(new CfgLogModule(LogEnum.BSE.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.CHN.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.CNT.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.NET.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.TRX.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.WLT.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.EXE.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.ADM.name(), LogLevels.INFO.name()));
        modules.add(new CfgLogModule(LogEnum.SOL.name(), LogLevels.INFO.name()));
    }

    // getters
    public List<CfgLogModule> getModules() {
        return this.modules;
    }

    // setters
    public void setModules(List<CfgLogModule> _modules) {
        this.modules = _modules;
    }

    // implements
    public boolean contains(String _module) {
        return false;
    }

}
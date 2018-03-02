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

package org.aion.api.log;

import javax.xml.bind.Marshaller.Listener;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class MarshallerComment extends Listener {

    private XMLStreamWriter xsw;

    public MarshallerComment(XMLStreamWriter xsw) {
        this.xsw = xsw;
    }

    @Override
    public void beforeMarshal(Object source) {
        try {

            switch (source.getClass().getSimpleName()) {
            case "ApiCfg":
            case "CfgConnect":
            case "CfgLogModule":
                break;
            case "CfgLog":
                xsw.writeComment("*** LOG ***");
                break;
            }
        } catch (XMLStreamException e) {
            System.out.println("Config comments isError.");
        }
    }
}

/**
 * ***************************************************************************** Copyright (c)
 * 2017-2018 Aion foundation.
 *
 * <p>This file is part of the aion network project.
 *
 * <p>The aion network project is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * <p>The aion network project is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with the aion network
 * project source files. If not, see <https://www.gnu.org/licenses/>.
 *
 * <p>Contributors: Aion foundation.
 *
 * <p>****************************************************************************
 */
package org.aion.api.cfg;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.aion.api.log.LogEnum;
import org.aion.api.log.LogLevels;

public class CfgLog {

    private Map<String, String> modules;

    CfgLog() {

        modules = new HashMap<>();
        modules.put(LogEnum.BSE.name(), LogLevels.INFO.name());
        modules.put(LogEnum.CHN.name(), LogLevels.INFO.name());
        modules.put(LogEnum.CNT.name(), LogLevels.INFO.name());
        modules.put(LogEnum.NET.name(), LogLevels.INFO.name());
        modules.put(LogEnum.TRX.name(), LogLevels.INFO.name());
        modules.put(LogEnum.WLT.name(), LogLevels.INFO.name());
        modules.put(LogEnum.EXE.name(), LogLevels.INFO.name());
        modules.put(LogEnum.ADM.name(), LogLevels.INFO.name());
        modules.put(LogEnum.SOL.name(), LogLevels.INFO.name());
    }

    void fromXML(final XMLStreamReader sr) throws XMLStreamException {
        this.modules = new HashMap<>();
        loop:
        while (sr.hasNext()) {
            int eventType = sr.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    String elementName = sr.getLocalName().toUpperCase();
                    if (LogEnum.contains(elementName))
                        this.modules.put(elementName, Cfg.readValue(sr).toUpperCase());
                    break;
                case XMLStreamReader.END_ELEMENT:
                    break loop;
                default:
                    // Cfg.skipElement(sr);
                    break;
            }
        }
    }

    String toXML() {
        final XMLOutputFactory output = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter;
        String xml;
        try {
            Writer strWriter = new StringWriter();
            xmlWriter = output.createXMLStreamWriter(strWriter);
            xmlWriter.writeCharacters("\r\n\t");
            xmlWriter.writeStartElement("log");
            xmlWriter.writeCharacters("\r\n");
            for (Map.Entry<String, String> module : this.modules.entrySet()) {
                xmlWriter.writeCharacters("\t\t");
                xmlWriter.writeStartElement(module.getKey().toUpperCase());
                xmlWriter.writeCharacters(module.getValue().toUpperCase());
                xmlWriter.writeEndElement();
                xmlWriter.writeCharacters("\r\n");
            }
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeEndElement();
            xml = strWriter.toString();
            strWriter.flush();
            strWriter.close();
            xmlWriter.flush();
            xmlWriter.close();
            return xml;
        } catch (IOException | XMLStreamException e) {
            return "";
        }
    }

    // getters
    public Map<String, String> getModules() {
        return this.modules;
    }
}

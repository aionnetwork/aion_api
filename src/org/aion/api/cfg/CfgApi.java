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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.*;
import java.io.*;

public class CfgApi extends Cfg {

    private CfgApi() {
        this.log = new CfgLog();

        // TODO :: test and open it later.
        //this.connect = new CfgConnect();

        if (!fromXML()) {
            toXML(null);
        }

    }

    public static CfgApi inst() {
        return ApiCfgHolder.inst;
    }




    public CfgLog getLog() {
        return this.log;
    }

    public void setLog(CfgLog _log) {
        this.log = _log;
    }

    public CfgConnect getConnect() {
        return this.connect;
    }

    public void setConnect(CfgConnect _cnt) {
        this.connect = _cnt;
    }

    private static class ApiCfgHolder {
        private static final CfgApi inst = new CfgApi();
    }

    @Override
    public boolean fromXML() {
        File cfgFile = new File(CONF_FILE_PATH);
        if(!cfgFile.exists())
            return false;
        XMLInputFactory input = XMLInputFactory.newInstance();
        FileInputStream fis;
        try {
            fis = new FileInputStream(cfgFile);
            XMLStreamReader sr = input.createXMLStreamReader(fis);
            loop: while (sr.hasNext()) {
                int eventType = sr.next();
                switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    String elementName = sr.getLocalName().toLowerCase();
                    switch (elementName) {
                    case "log":
                        this.log.fromXML(sr);
                        break;
                    case "connect":
                        //this.connect.fromXML(sr);
                        break;
                    default:
                        //skipElement(sr);
                        break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (sr.getLocalName().toLowerCase().equals("aion_api"))
                        break loop;
                    else
                        break;
                }
            }
            closeFileInputStream(fis);
        } catch (Exception e) {
            System.out.println("<error on-parsing-config-xml msg=" + e.getLocalizedMessage() + ">");
            System.exit(1);
        }
        return true;
    }

    @Override
    public void toXML(final String[] args) {

//        if (args != null) {
//            boolean override = false;
//            for (String arg : args) {
//                arg = arg.toLowerCase();
//                if (override)
//                    System.out.println("Config Override");
//            }
//        }

        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty("escapeCharacters", false);
        XMLStreamWriter sw = null;

        try {

            File file = new File(CONF_FILE_PATH);
            file.getParentFile().mkdirs();

            sw = output.createXMLStreamWriter(new FileWriter(file));
            sw.writeStartDocument("utf-8", "1.0");
            sw.writeCharacters("\r\n");
            sw.writeStartElement("aion_api");

            this.log = new CfgLog();
            sw.writeCharacters(this.log.toXML());
            //sw.writeCharacters(this.getConnect().toXML());

            sw.writeCharacters("\r\n");
            sw.writeEndElement();
            sw.flush();
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("<error on-write-config-xml-to-file>");
            System.exit(1);
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (XMLStreamException e) {
                    System.out.println("<error on-close-stream-writer>");
                    System.exit(1);
                }
            }
        }
    }

    private void closeFileInputStream(final FileInputStream fis){
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                System.out.println("<error on-close-file-input-stream>");
                System.exit(1);
            }
        }
    }
}
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

import org.aion.api.log.MarshallerComment;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

/**
 * Created by Jay Tseng on 18/04/17.
 */
@XmlRootElement(name = "javaapi")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "connect", "log" })
public class ApiCfg {
    private static final String CONF_FOLDER = "config";
    private static final String CONF_FILE_PATH = System.getProperty("user.dir") + "/config/apiconfig.xml";

    @XmlElement(name = "connect", required = false)
    protected CfgConnect connect;
    @XmlElement(name = "log", required = true)
    protected CfgLog log;

    // init
    private ApiCfg() {
        this.log = new CfgLog();
        // TODO :: test and open it later.
        //this.connect = new CfgConnect();
    }

    public static ApiCfg inst() {
        return ApiCfgHolder.inst;
    }

    public void load() {
        configFolderCheck();
        File file = new File(CONF_FILE_PATH);
        if (file.exists()) {
            try {
                JAXBContext jc = JAXBContext.newInstance(org.aion.api.cfg.ApiCfg.class);
                Unmarshaller um = jc.createUnmarshaller();
                ApiCfg _cfg = (ApiCfg) um.unmarshal(file);

                this.setLog(_cfg.getLog());
                this.setConnect(_cfg.getConnect());

            } catch (JAXBException e) {
                System.out.println(
                        "Could not load config file. Please check you config file or use default setting (delete the config file).");
                System.exit(1);
            }
        } else {
            save();
        }
    }

    // implements
    public void configFolderCheck() {
        boolean pass = true;
        String configFolderPath = System.getProperty("user.dir") + "/" + CONF_FOLDER;
        if (!Files.exists(Paths.get(configFolderPath), LinkOption.NOFOLLOW_LINKS)) {
            pass = false;
            System.out.println(
                    "Could not find folder \"" + CONF_FOLDER + "\" at \"" + System.getProperty("user.dir") + "\"");
        } else {
            if (!Files.exists(Paths.get(CONF_FILE_PATH), LinkOption.NOFOLLOW_LINKS)) {
                pass = false;
                System.out.println("\"" + CONF_FILE_PATH + "\" not exists. Create default config file");
            }
        }
        if (!pass) {
            save();
        }
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

    public void save() {
        try {

            JAXBContext ctx = JAXBContext.newInstance(org.aion.api.cfg.ApiCfg.class);

            /**
             * parse obj
             */
            Marshaller m = ctx.createMarshaller();
            StringWriter sw = new StringWriter();
            XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(sw);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_FRAGMENT, false);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            m.setListener(new MarshallerComment(xsw));
            m.marshal(this, xsw);
            xsw.close();

            /**
             * format intended
             */
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            new File(System.getProperty("user.dir") + "/" + CONF_FOLDER).mkdir();
            FileWriter fw = new FileWriter(new File(CONF_FILE_PATH));
            transformer.transform(new StreamSource(new StringReader(sw.toString())), new StreamResult(fw));

            System.out.println("New config file saved.");

        } catch (JAXBException | XMLStreamException | TransformerException | IOException | IndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Could not save " + CONF_FILE_PATH + "." + e);
        }
    }

    private static class ApiCfgHolder {
        private static final ApiCfg inst = new ApiCfg();
    }
}
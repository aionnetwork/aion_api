package org.aion.api.cfg;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class Cfg {

    private static final String BASE_PATH = System.getProperty("user.dir");
    protected static final String CONF_FILE_PATH = BASE_PATH + "/config/apiconfig.xml";

    protected CfgConnect connect;
    protected CfgLog log;
    protected Boolean secureConnectEnabled;

    public abstract boolean fromXML();

    public abstract void toXML(final String[] args);

    public String getBasePath() {
        return BASE_PATH;
    }

    public static String readValue(final XMLStreamReader sr) throws XMLStreamException {
        StringBuilder str = new StringBuilder();
        readLoop:
        while (sr.hasNext()) {
            int eventType = sr.next();
            switch (eventType) {
            case XMLStreamReader.CHARACTERS:
                str.append(sr.getText());
                break;
            case XMLStreamReader.END_ELEMENT:
                break readLoop;
            }
        }
        return str.toString();
    }

    public static void skipElement(final XMLStreamReader sr) throws XMLStreamException {
        skipLoop:
        while (sr.hasNext()) {
            int eventType = sr.next();
            switch (eventType) {
            case XMLStreamReader.END_ELEMENT:
                break skipLoop;
            }
        }
    }
}

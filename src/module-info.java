module aion.api.client {
    requires aion.base;
    requires aion.log;
    requires java.xml;
    requires slf4j.api;
    requires aion.rlp;
    requires aion.crypto;
    requires logback.core;
    requires logback.classic;
    requires jdk.sctp;

    exports org.aion.api;
    exports org.aion.api.type;
    exports org.aion.api.impl;
    // should probably move what we need from this pkg into some pkg not named 'internal'
    exports org.aion.api.impl.internal; 
}

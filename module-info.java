module aion.api.client {
    requires aion.base;
    requires aion.log;
    requires java.xml;
    requires slf4j.api;
    requires aion.rlp;
    requires aion.crypto;
    requires logback.core;
    requires logback.classic;

    exports org.aion.api;
}
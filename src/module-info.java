module aion.api.client {
    requires aion.base;
    requires aion.log;
    requires java.xml;
    requires slf4j.api;
    requires aion.rlp;
    requires aion.crypto;
    requires logback.core;
    requires logback.classic;
    requires commons.collections4;
    requires libnzmq;
    requires protobuf.java;

    exports org.aion.api;
    exports org.aion.api.type;

    // Warning: this export will be removed in the near future;
    // avoid taking a dependency on this package.
    exports org.aion.api.impl;
}

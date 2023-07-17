module es.iti.wakamiti.lsp {

    exports es.iti.wakamiti.lsp;

    requires transitive imconfig;
    requires commons.cli;
    requires transitive org.eclipse.lsp4j;
    requires transitive org.eclipse.lsp4j.jsonrpc;
    requires org.slf4j;
    requires es.iti.wakamiti.api;
    requires es.iti.wakamiti.core;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;
    requires org.yaml.snakeyaml;
    requires rgxgen;


}
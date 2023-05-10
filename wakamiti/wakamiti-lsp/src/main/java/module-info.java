module wakamiti.lsp {

    exports iti.wakamiti.lsp;

    requires transitive imconfig;
    requires commons.cli;
    requires transitive org.eclipse.lsp4j;
    requires transitive org.eclipse.lsp4j.jsonrpc;
    requires org.slf4j;
    requires wakamiti.api;
    requires wakamiti.core;
    requires transitive gherkin.parser;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;
    requires org.yaml.snakeyaml;
    requires rgxgen;


}
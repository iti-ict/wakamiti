module kukumo.lsp {

    exports iti.kukumo.lsp;

    requires transitive imconfig;
    requires commons.cli;
    requires transitive org.eclipse.lsp4j;
    requires transitive org.eclipse.lsp4j.jsonrpc;
    requires org.slf4j;
    requires kukumo.core;
    requires transitive gherkin.parser;
	requires org.apache.logging.log4j.core;
	requires org.apache.logging.log4j;
	requires org.yaml.snakeyaml;
	requires rgxgen;

}
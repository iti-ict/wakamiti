module iti.commons.jext {

    exports iti.commons.jext;
    requires transitive java.compiler;
    requires org.slf4j;
    uses javax.annotation.processing.Processor;
    uses iti.commons.jext.ExtensionLoader;
    provides javax.annotation.processing.Processor with iti.commons.jext.ExtensionProcessor;
}
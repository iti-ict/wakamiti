import es.iti.commons.jext.ExtensionLoader;
import es.iti.commons.jext.ExtensionProcessor;

module iti.commons.jext {

    exports es.iti.commons.jext;
    requires transitive java.compiler;
    requires org.slf4j;
    uses javax.annotation.processing.Processor;
    uses ExtensionLoader;
    provides javax.annotation.processing.Processor with ExtensionProcessor;
}
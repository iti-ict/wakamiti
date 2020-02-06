
module iti.commons.jext.spring {

    requires iti.commons.jext;
    requires org.slf4j;

    requires spring.context;
    requires spring.beans;
    requires spring.core;

    uses iti.commons.jext.ExtensionLoader;

}
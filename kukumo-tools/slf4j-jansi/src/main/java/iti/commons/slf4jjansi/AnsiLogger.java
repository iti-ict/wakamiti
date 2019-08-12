package iti.commons.slf4jjansi;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.spi.LocationAwareLogger;

import iti.commons.slf4jjansi.impl.LocationAwareAnsiLogger;
import iti.commons.slf4jjansi.impl.SimpleAnsiLogger;

public class AnsiLogger {

    public static final String ANSI_ENABLED = "ansi.enabled";
    
    

    public static Logger of(Logger logger) {
        Ansi.setEnabled(!"false".equals(System.getProperty(ANSI_ENABLED)));

        if (logger instanceof LocationAwareLogger) {
            return new LocationAwareAnsiLogger((LocationAwareLogger) logger);
        } else {
            return new SimpleAnsiLogger(logger);
        }
    }



}
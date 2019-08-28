package iti.commons.slf4jjansi;

import iti.commons.slf4jjansi.impl.LocationAwareAnsiLogger;
import iti.commons.slf4jjansi.impl.SimpleAnsiLogger;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.spi.LocationAwareLogger;

public class AnsiLogger {

    public static Logger of(Logger logger) {
        if (logger instanceof LocationAwareLogger) {
            return new LocationAwareAnsiLogger((LocationAwareLogger) logger);
        } else {
            return new SimpleAnsiLogger(logger);
        }
    }


    public static void setAnsiEnabled(boolean enabled) {
        Ansi.setEnabled(enabled);
    }
    
}
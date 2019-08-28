package iti.kukumo.util;

import iti.commons.slf4jjansi.impl.LocationAwareAnsiLogger;
import iti.commons.slf4jjansi.impl.SimpleAnsiLogger;
import iti.kukumo.api.plan.Result;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ITI
 * Created by ITI on 28/08/19
 */
public class KukumoLogger {

    private static Map<String,String> FORMATS = Stream.of(
            new AbstractMap.SimpleEntry<>("{error}",        "@|red,bold {}|@"),
            new AbstractMap.SimpleEntry<>("{warn}",         "@|yellow,bold {}|@"),
            new AbstractMap.SimpleEntry<>("{message}",      "@|magenta,bold {}|@"),
            new AbstractMap.SimpleEntry<>("{resourceType}", "@|blue,bold {}|@"),
            new AbstractMap.SimpleEntry<>("{uri}",          "@|yellow {}|@"),
            new AbstractMap.SimpleEntry<>("{contributor}",  "@|green,bold {}|@")
    ).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


    private static String format(String message) {
        for (Map.Entry<String,String> entry : FORMATS.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    public static String logo() {
        return
               "@|bold,magenta \n"+
               "----------------------------------------------\n"+
               "    | |/ /   _| | ___   _ _ __ ___   ___\n" +
               "    | ' / | | | |/ / | | | '_ ` _ \\ / _ \\ \n" +
               "    | . \\ |_| |   <| |_| | | | | | | (_) | \n"+
               "    |_|\\_\\__,_|_|\\_\\\\__,_|_| |_| |_|\\___/\n"+
               "----------------------------------------------\n"+
               "|@";
    }


    public static String resultColor(Result result) {
        if (result == null) {
            return "white";
        }
        String color;
        switch (result) {
            case PASSED:
                color = "green";
                break;
            case ERROR:
            case FAILED:
                color = "bold,red";
                break;
            case SKIPPED:
                color = "faint";
                break;
            case UNDEFINED:
                color = "yellow,faint";
                break;
            default:
                throw new IllegalStateException();
        }
        return color;
    }


    public static Logger forClass(Class<?> logger) {
        return of(LoggerFactory.getLogger(logger));
    }


    public static Logger of(Logger logger) {
        if (logger instanceof LocationAwareLogger) {
            return new LocationAwareKukumoLogger((LocationAwareLogger) logger);
        } else {
            return new SimpleKukumoLogger(logger);
        }
    }

    public static void setAnsiEnabled(boolean enabled) {
        Ansi.setEnabled(enabled);
    }



    private static class LocationAwareKukumoLogger extends LocationAwareAnsiLogger {

        LocationAwareKukumoLogger(LocationAwareLogger delegate) {
            super(delegate);
        }

        @Override
        protected String ansi(String message) {
            return super.ansi(KukumoLogger.format(message));
        }
    }


    private static class SimpleKukumoLogger extends SimpleAnsiLogger {

        public SimpleKukumoLogger(Logger delegate) {
            super(delegate);
        }

        @Override
        protected String ansi(String message) {
            return super.ansi(KukumoLogger.format(message));
        }
    }

}

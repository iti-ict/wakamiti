package iti.kukumo.lsp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggerUtil {


    private static Map<String,Logger> loggers = new HashMap<>();

    private LoggerUtil() { /* avoid instantiation */ }

    static Logger get(String logger) {
        return loggers.computeIfAbsent("iti.kukumo.lsp."+logger, LoggerFactory::getLogger);
    }

    public static void log(String logger, Object params) {
        get(logger).debug("EVENT {}\n{}",logger,params);
    }

}

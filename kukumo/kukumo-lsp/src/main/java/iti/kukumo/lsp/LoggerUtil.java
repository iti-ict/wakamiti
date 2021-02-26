package iti.kukumo.lsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class LoggerUtil {

    private static final String DASHES = " --------------------------------------------------- ";
    private static Map<String, Logger> loggers = new HashMap<>();

    private LoggerUtil() { /* avoid instantiation */ }

    static Logger get(String logger) {
        return loggers.computeIfAbsent("iti.kukumo.lsp." + logger, LoggerFactory::getLogger);
    }


    public static <T> T logEntry(String logger, T params) {
        get(logger).debug("{}\n >> REQUEST {}\n{}\n{}\n{}", DASHES, logger, DASHES, params, DASHES);
        return params;
    }


    public static <T> T logExit(String logger, T response) {
        get(logger).debug("{}\n << RESPONSE {}\n{}\n{}\n", DASHES, response, DASHES, DASHES);
        return response;
    }




}

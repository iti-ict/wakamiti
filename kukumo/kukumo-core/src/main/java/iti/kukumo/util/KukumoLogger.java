/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.util;


import iti.commons.configurer.Configuration;
import iti.commons.slf4jansi.AnsiLogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.RegexReplacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static iti.kukumo.api.KukumoConfiguration.*;


public class KukumoLogger {

    public static final String CONSOLE_APPENDER = "Console";
    public static final String FILE_APPENDER = "File";

    public static String logo() {
        return "\n" +
                "----------------------------------------------\n" +
                "    | |/ /   _| | ___   _ _ __ ___   ___\n" +
                "    | ' / | | | |/ / | | | '_ ` _ \\ / _ \\ \n" +
                "    | . \\ |_| |   <| |_| | | | | | | (_) | \n" +
                "    |_|\\_\\__,_|_|\\_\\\\__,_|_| |_| |_|\\___/\n" +
                "----------------------------------------------";
    }


    public static Logger forClass(Class<?> logger) {
        return of(LoggerFactory.getLogger(logger));
    }


    public static Logger of(Logger logger) {
        return AnsiLogger.of(logger);
    }


    public static void configure(Configuration configuration) {
        AnsiLogger.setAnsiEnabled(
                configuration.get(LOGS_ANSI_ENABLED, Boolean.class).orElse(true)
        );
        AnsiLogger.setStyles(
                configuration.inner(LOGS_ANSI_STYLES).asProperties()
        );

        if (configuration.get(LOGS_FILE_ENABLED, Boolean.class).get()) {

            // The file appender will always be written in debug mode
            LoggerContext ctx = LoggerContext.getContext(false);
            FileAppender fileAppender = FileAppender.newBuilder()
                    .setName(FILE_APPENDER)
                    .withFileName(configuration.get(LOGS_FILE_PATH, String.class).get())
                    .withAppend(false)
                    .setFilter(ThresholdFilter.createFilter(Level.DEBUG, Filter.Result.ACCEPT, Filter.Result.DENY))
                    .setLayout(PatternLayout.newBuilder()
                            .withPattern("%d  [%30C{1.}.%-20M] %6p -  %m%n")
                            .withRegexReplacement(RegexReplacement
                                    .createRegexReplacement(Pattern.compile("\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[mGK]"), ""))
                            .build()
                    ).build();
            fileAppender.start();
            ctx.getConfiguration().addAppender(fileAppender);
            ctx.getConfiguration().getRootLogger().addAppender(ctx.getConfiguration().getAppender(FILE_APPENDER), null, null);
            ctx.updateLoggers();
        }
    }

    public static void setLevel(String appenderName, Level level) {
        LoggerContext ctx = LoggerContext.getContext(false);
        AbstractFilterable appender = ctx.getConfiguration().getAppender(appenderName);
        appender.removeFilter(appender.getFilter());
        appender.addFilter(ThresholdFilter.createFilter(level, Filter.Result.ACCEPT, Filter.Result.DENY));
        ctx.updateLoggers();
    }
}

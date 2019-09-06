package iti.commons.slf4jjansi.impl;

import iti.commons.slf4jjansi.AnsiLogger;
import org.fusesource.jansi.Ansi;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ITI
 * Created by ITI on 6/9/19
 */
final class JAnsiSupport {

    public static final JAnsiSupport instance = new JAnsiSupport();
    private static final Pattern globalStylePattern = Pattern.compile("^\\{!([^}]*)\\}.*");

    private Map<String,String> styles;

    private JAnsiSupport() {
        AnsiLogger.addConfigurationChangeObserver(this::invalidateStyles);
    }

    private String replaceStyles(String globalStyle, String message) {
        if (styles == null) {
            styles = new HashMap<>();
            AnsiLogger.styles().forEach( (key, value) -> styles.put("{"+key+"}","@|"+value+" {}|@"));
        }
        Matcher globalStyleMatcher = globalStylePattern.matcher(message);
        if (globalStyleMatcher.matches()) {
            globalStyle = globalStyleMatcher.group(1);
            message = message.substring(message.indexOf('}')+1).trim();
        }
        for (Map.Entry<String,String> style : styles.entrySet()) {
            message = message.replace(style.getKey(),style.getValue());
        }
        if (globalStyle != null) {
            globalStyle = AnsiLogger.styles().getProperty(globalStyle);
        }
        if (globalStyle != null) {
            message = "$|"+message.replace("|@","|@$|").replace("@|","|$@|") + "|$";
            message = message.replace("$|","@|"+globalStyle+" ").replace("|$","|@");
        }
        return message;
    }

    private void invalidateStyles() {
        this.styles = null;
    }

    String ansi(String level, String message) {
        return Ansi.ansi().render(replaceStyles(level, message)).toString();
    }


}

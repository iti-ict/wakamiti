package iti.commons.slf4jansi.impl;

import iti.commons.slf4jansi.AnsiLogger;
import org.fusesource.jansi.Ansi;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


final class JAnsiSupport {

    public static final JAnsiSupport instance = new JAnsiSupport();
    private static final Pattern globalStylePattern = Pattern.compile("^\\{!([^}]*)\\}.*");
    private static final Pattern stylePattern = Pattern.compile("\\{([^}]*)\\}");

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
        Matcher styleMatcher = stylePattern.matcher(message);
        while (styleMatcher.find()) {
            String foundStyle = "{"+styleMatcher.group(1)+"}";
            String style = styles.getOrDefault(foundStyle,"{}");
            message = message.replace(foundStyle,style);
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
        return message == null ? null : Ansi.ansi().render(replaceStyles(level, message)).toString();
    }


}

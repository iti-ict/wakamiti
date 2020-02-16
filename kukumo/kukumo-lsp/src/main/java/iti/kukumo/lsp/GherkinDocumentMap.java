package iti.kukumo.lsp;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import iti.commons.gherkin.GherkinDialect;
import iti.commons.gherkin.GherkinDialectProvider;
import iti.commons.gherkin.GherkinLanguageConstants;
import iti.kukumo.util.LocaleLoader;


public class GherkinDocumentMap {

    private static final GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
    private final String[] lines;
    private final Locale locale;
    private final GherkinDialect dialect;



    public GherkinDocumentMap(String document) {
        this.lines = document.split("\n");
        this.locale = extractLocale(lines);
        this.dialect = dialectProvider.getDialect(locale);
    }


    private static Locale extractLocale(String[] lines) {
        Pattern pattern = Pattern.compile("\\s*#*\\s*language\\s*:\\s*([^\\s]+)\\s*");
        for (String line : lines) {
            var matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return LocaleLoader.forLanguage(matcher.group(1));
            }
        }
        return Locale.ENGLISH;
    }


    private String lastLineWithContent(int lineNumber) {
        for (int i = lineNumber; i>0; i--) {
            String line = lines[i];
            if (line.stripLeading().isEmpty()) {
                continue;
            }
            return line;
        }
        return null;
    }



    private String lastKeyword(int lineNumber) {
        for (int i = lineNumber; i>0; i--) {
            String line = lines[i].stripLeading();
            if (line.startsWith("#")) {
                continue;
            }
            int position = line.indexOf(':');
            if (position > -1) {
                return line.substring(0, position);
            }
        }
        return null;
    }


    public List<String> followingKeywords(int lineNumber) {

        String line = lastLineWithContent(lineNumber);
        if (line == null) {
            line = "";
        }
        line = line.stripLeading();
        if (line.startsWith(GherkinLanguageConstants.DOCSTRING_SEPARATOR) ||
            line.startsWith(GherkinLanguageConstants.DOCSTRING_ALTERNATIVE_SEPARATOR) ||
            line.startsWith(GherkinLanguageConstants.TABLE_CELL_SEPARATOR)) {
            return List.of();
        }

        String lastKeyword = lastKeyword(lineNumber);
        List<String> result;
        if (lastKeyword == null) {
            result = suffix(dialect.getFeatureKeywords(),":");
        } else if (dialect.getFeatureKeywords().contains(lastKeyword)) {
            result = suffix(dialect.getFeatureContentKeywords(),":");
        } else if (dialect.getFeatureContentKeywords().contains(lastKeyword)) {
            result = dialect.getStepKeywords();
        } else {
            result = List.of();
        }
        return result;
    }


    public boolean isStep(int lineNumber, String lineContent) {
        String strippedLine = lineContent;
        boolean startWithStepKeyword = false;
        for (String stepKeyword : dialect.getStepKeywords()) {
            if (strippedLine.startsWith(stepKeyword)) {
                startWithStepKeyword = true;
                break;
            }
        }
        if (startWithStepKeyword) {
            String lastKeyword = lastKeyword(lineNumber-1);
            return dialect.getFeatureContentKeywords().contains(lastKeyword);
        } else {
            return false;
        }
    }



    private static List<String> suffix(List<String> values, String suffix) {
        return values.stream().map(s -> s+suffix).collect(toList());
    }



    public Locale locale() {
        return locale;
    }


    public GherkinDialect dialect() {
        return dialect;
    }


}

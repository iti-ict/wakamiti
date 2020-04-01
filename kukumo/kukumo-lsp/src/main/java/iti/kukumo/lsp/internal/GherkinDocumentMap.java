package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import iti.commons.gherkin.GherkinDialect;
import iti.commons.gherkin.GherkinDialectProvider;
import iti.commons.gherkin.GherkinLanguageConstants;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.util.LocaleLoader;


public class GherkinDocumentMap {

    private static final List<String> propertiesRequiringParsing = List.of(
        KukumoConfiguration.LANGUAGE,
        KukumoConfiguration.DATA_FORMAT_LANGUAGE,
        KukumoConfiguration.MODULES
    );
    private static final Pattern propertyPattern = Pattern.compile("\\s*#*\\s*([^\\s]+)\\s*:\\s*([^\\s]+)\\s*");
    private static final GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
    private final Locale locale;
    private final GherkinDialect dialect;
    private List<String> lines = new ArrayList<>();


    public GherkinDocumentMap(String document) {
        this.lines = new ArrayList<>(Arrays.asList(document.split("\n")));
        if (document.endsWith("\n")) {
            this.lines.add("");
        }
        this.locale = extractLocale(lines);
        this.dialect = dialectProvider.getDialect(locale);
    }


    private static Locale extractLocale(List<String> lines) {
        Pattern pattern = Pattern.compile("\\s*#*\\s*language\\s*:\\s*([^\\s]+)\\s*");
        for (String line : lines) {
            var matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return LocaleLoader.forLanguage(matcher.group(1));
            }
        }
        return Locale.ENGLISH;
    }


    public String lineContent(int lineNumber, LineRange range) {
        return range.extractString(lines.get(lineNumber));
    }


    public boolean updateLine(int lineNumber, LineRange range, String delta) {
        boolean requireParsing = false;
        String strippedLineContent = lines.get(lineNumber).stripLeading();
        boolean isProperty = strippedLineContent.startsWith("#");
        if (isProperty) {
            var matcher = propertyPattern.matcher(strippedLineContent);
            if (matcher.matches() && propertiesRequiringParsing.contains(matcher.group(1))) {
                requireParsing = true;
            }
        } else {
            LineRange keyword = detectKeyword(strippedLineContent, dialect.getKeywords());
            if (!keyword.isEmpty() && range.intersect(keyword)) {
                requireParsing = true;
            }
        }
        lines.set(lineNumber, range.replaceString(lines.get(lineNumber),delta));
        if (delta.contains("\n")) {
            rearrangeLines();
        }
        return requireParsing;
    }




    private void rearrangeLines() {
        String raw = lines.stream().collect(Collectors.joining("\n"));
        lines.clear();
        lines.addAll(Arrays.asList(raw.split("\n")));
        if (raw.endsWith("\n")) {
            lines.add("");
        }
    }


    private String lastLineWithContent(int lineNumber) {
        for (int i = lineNumber; i>0; i--) {
            String line = lines.get(i);
            if (line.stripLeading().isEmpty()) {
                continue;
            }
            return line;
        }
        return null;
    }



    private String lastKeyword(int lineNumber) {
        for (int i = lineNumber; i>0; i--) {
            String line = lines.get(i).stripLeading();
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


    public boolean isStep(int lineNumber, String strippedLineContent) {
        LineRange keywordRange = detectKeyword(strippedLineContent,dialect.getStepKeywords());
        if (!keywordRange.isEmpty()) {
            String lastKeyword = lastKeyword(lineNumber-1);
            return dialect.getFeatureContentKeywords().contains(lastKeyword);
        } else {
            return false;
        }
    }


    public LineRange detectKeyword(String strippedLineContent, List<String> keywords) {
        LineRange keywordRange = LineRange.empty();
        for (String keyword : keywords) {
            if (strippedLineContent.startsWith(keyword)) {
                keywordRange = LineRange.of(0,keyword.length());
                break;
            }
        }
        return keywordRange;
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



    public String replaceDocumentSegment(TextRange range, String delta) {
        StringBuilder document = new StringBuilder();
        // pre-range lines
        for (int i=0; i<range.startLine(); i++) {
            document.append(lines.get(i)).append("\n");
        }
        // partial pre-range line
        String preLine = lines.get(range.startLine()).substring(range.startLinePosition());
        document.append(preLine);
        // delta
        document.append(delta);
        // partial post-range line
        String postLine = lines.get(range.endLine()).substring(0,range.startLinePosition());
        document.append(postLine);
        // post-range lines
        for (int i=range.endLine(); i<lines.size()-1; i++) {
            document.append(lines.get(i)).append("\n");
        }
        if (range.endLine()==lines.size()-1) {
            document.append(lines.get(range.endLine()));
        }
        return document.toString();
    }


    public String currentContent() {
        return lines.stream().collect(Collectors.joining("\n"));
    }


    public List<String> lines() {
        return lines;
    }




}

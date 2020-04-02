package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.toList;

import java.util.*;
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

    private Locale locale;
    private GherkinDialect dialect;
    private TextDocument document;


    public GherkinDocumentMap(String document) {
        this.document = new TextDocument(document);
        this.locale = extractProperty("language",this.document).map(Locale::new).orElse(Locale.ENGLISH);
        this.dialect = dialectProvider.getDialect(locale);
    }


    public String raw() {
        return document.raw();
    }

    public TextDocument document() {
        return document;
    }

    public GherkinDialect dialect() {
        return dialect;
    }

    public Locale locale() {
        return locale;
    }

    public boolean replace(TextRange range, String text) {
        boolean requireParsing = false;
        if (range.isSingleLine()) {
            requireParsing = checkReplaceSingleLineRequireParsing(range);
        } else {
            requireParsing = true;
        }
        document.replace(range,text);
        return requireParsing;
    }



    public boolean checkReplaceSingleLineRequireParsing(TextRange range) {
        boolean requireParsing = false;
        int lineNumber = range.startLine();
        String strippedLineContent = document.line(lineNumber).strip();
        boolean isProperty = strippedLineContent.startsWith("#");
        if (isProperty) {
            var matcher = propertyPattern.matcher(strippedLineContent);
            if (matcher.matches() && propertiesRequiringParsing.contains(matcher.group(1))) {
                requireParsing = true;
            }
        } else {
            TextRange keyword = detectKeyword(lineNumber,strippedLineContent,dialect.getKeywords());
            if (!keyword.isEmpty() && range.intersect(keyword)) {
                requireParsing = true;
            }
        }
        return requireParsing;
    }



    private TextRange detectKeyword(int lineNumber, String strippedLineContent, List<String> keywords) {
        TextRange keywordRange = TextRange.of(0,0,0,0);
        for (String keyword : keywords) {
            if (strippedLineContent.startsWith(keyword)) {
                keywordRange = TextRange.of(lineNumber,0,lineNumber,keyword.length());
                break;
            }
        }
        return keywordRange;
    }


    public boolean isStep(int lineNumber, String strippedLineContent) {
        TextRange keywordRange = detectKeyword(lineNumber,strippedLineContent,dialect.getStepKeywords());
        if (!keywordRange.isEmpty()) {
            String lastKeyword = lastKeyword(lineNumber-1);
            return dialect.getFeatureContentKeywords().contains(lastKeyword);
        } else {
            return false;
        }
    }


    private String lastKeyword(int lineNumber) {
        for (int i = lineNumber; i>0; i--) {
            String line = document.line(i).stripLeading();
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



    private String lastLineWithContent(int lineNumber) {
        for (int i = lineNumber; i>0; i--) {
            String line = document.line(i);
            if (line.stripLeading().isEmpty()) {
                continue;
            }
            return line;
        }
        return null;
    }


    private static List<String> suffix(List<String> values, String suffix) {
        return values.stream().map(s -> s+suffix).collect(toList());
    }

    /*

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
        String postLine = lines.get(range.endLine()).substring(range.endLinePosition());
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
        return lines.stream().collect(Collectors.joining(""));
    }


    public List<String> lines() {
        return lines;
    }





*/


    private static Optional<String> extractProperty(String property,TextDocument document) {
        Pattern pattern = Pattern.compile("\\s*#*\\s*"+property+"\\s*:\\s*([^\\s]+)\\s*");
        for (int l=0; l<document.lines(); l++) {
            var matcher = pattern.matcher(document.line(l));
            if (matcher.matches()) {
                return Optional.of(matcher.group(1));
            }
        }
        return Optional.empty();
    }


}

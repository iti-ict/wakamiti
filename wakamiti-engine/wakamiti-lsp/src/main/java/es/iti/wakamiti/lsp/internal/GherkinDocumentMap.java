/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.gherkin.parser.GherkinDialect;
import es.iti.wakamiti.core.gherkin.parser.GherkinDialectProvider;
import es.iti.wakamiti.core.gherkin.parser.GherkinLanguageConstants;


/**
 * Maintains positional mapping utilities over editable Gherkin text.
 * <p>
 * The map helps language-server features determine whether edits require
 * reparsing and locate structural segments (keywords, tags, properties) using
 * zero-based line and character coordinates.
 * </p>
 */
public class GherkinDocumentMap {

    private static final List<String> PROPERTIES_REQUIRING_PARSING = List.of(
            WakamitiConfiguration.LANGUAGE,
            WakamitiConfiguration.DATA_FORMAT_LANGUAGE,
            WakamitiConfiguration.MODULES
    );
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\s*+#*+\\s*+(\\S++)\\s*+:\\s*+(\\S++)\\s*+");
    private static final Pattern TAG_PATTERN = Pattern.compile("(\\s*+@\\w++\\s*+)*+");
    private static final GherkinDialectProvider DIALECT_PROVIDER = new GherkinDialectProvider();

    private Locale locale;
    private GherkinDialect dialect;
    private TextDocument document;

    /**
     * Creates and parses a map for the supplied Gherkin source.
     * <p>
     * The document locale is read from its {@code # language:} header. When no
     * valid header is present, English is used. The selected locale determines
     * the dialect used to recognize features, rules, scenarios, examples,
     * backgrounds and steps.
     *
     * @param document complete Gherkin source; subsequent line and character
     *                 positions refer to this text
     */
    public GherkinDocumentMap(
            String document
    ) {
        this.document = new TextDocument(document);
        this.locale = extractProperty("language", this.document).map(Locale::new).orElse(Locale.ENGLISH);
        this.dialect = DIALECT_PROVIDER.getDialect(locale);
    }

    /**
     * Returns the current source text, including replacements made through
     * this map.
     *
     * @return the complete, unparsed Gherkin source
     */
    public String rawContent() {
        return document.rawText();
    }

    /**
     * Returns the mutable text representation used for positional operations.
     * <p>
     * The returned value is the instance owned by this map, not a defensive
     * copy. Callers should normally use {@link #replace(TextRange, String)} so
     * they can also determine whether the parsed structure must be rebuilt.
     *
     * @return the current text document
     */
    public TextDocument document() {
        return document;
    }

    /**
     * Returns the dialect selected from the document language header.
     *
     * @return the dialect used for keyword recognition
     */
    public GherkinDialect dialect() {
        return dialect;
    }

    /**
     * Returns the locale inferred from the Gherkin language header.
     *
     * @return the document locale, or English when no locale was declared
     */
    public Locale locale() {
        return locale;
    }

    /**
     * Replaces a source range and reports whether the parsed Gherkin model may
     * have become stale.
     * <p>
     * A multiline edit always requires reparsing because it can shift every
     * subsequent node. For a single-line edit, reparsing is required when the
     * edited range intersects syntax that can alter the document structure,
     * such as a property, tag or Gherkin keyword. The text is updated before
     * the result is returned.
     *
     * @param range half-open, zero-based range to replace
     * @param text replacement text; it may contain line separators
     * @return {@code true} when the caller must rebuild the parsed document
     */
    public boolean replace(
            TextRange range,
            String text
    ) {
        boolean requireParsing = false;
        if (range.isSingleLine()) {
            requireParsing = checkReplaceSingleLineRequireParsing(range);
        } else {
            requireParsing = true;
        }
        document.replaceRange(range, text);
        return requireParsing;
    }

    /**
     * Determines whether a single-line edit intersects syntax that affects the
     * parsed Gherkin structure.
     * <p>
     * This method performs only the structural check and does not modify the
     * document. The range is expected to be confined to one line.
     *
     * @param range half-open, zero-based edit range
     * @return {@code true} for edits touching a relevant property, tag or
     *         recognized keyword
     */
    public boolean checkReplaceSingleLineRequireParsing(
            TextRange range
    ) {
        if (document.isEmpty()) {
            return true;
        }
        boolean requireParsing = false;
        int lineNumber = range.startLine();
        String rawLine = document.extractLine(lineNumber);
        String stripLineContent = rawLine.strip();
        boolean isProperty = stripLineContent.startsWith("#");
        if (isProperty) {
            var matcher = PROPERTY_PATTERN.matcher(stripLineContent);
            if (matcher.matches() && PROPERTIES_REQUIRING_PARSING.contains(matcher.group(1))) {
                requireParsing = true;
            }
        } else {
            boolean isTag = TAG_PATTERN.matcher(stripLineContent).matches();
            if (isTag) {
                requireParsing = true;
            } else {
                TextRange keywordRange = detectKeyword(
                        lineNumber,
                        stripLineContent,
                        GherkinDialect::getKeywords
                );
                if (!keywordRange.isEmpty() && range.intersect(keywordRange)) {
                    requireParsing = true;
                }
            }
        }
        return requireParsing;
    }

    /**
     * Detects the step keyword used by a line in the current dialect.
     *
     * @param lineNumber zero-based line number
     * @param stripLineContent line content with leading indentation removed
     * @return the keyword's half-open range, or an empty range when the line
     *         does not start with a step keyword
     */
    public TextRange detectStepKeyword(
            int lineNumber,
            String stripLineContent
    ) {
        return detectKeyword(
                lineNumber,
                stripLineContent,
                GherkinDialect::getStepKeywords
        );
    }

    /**
     * Detects a scenario or scenario-outline keyword in a line.
     *
     * @param lineNumber zero-based line number
     * @param stripLineContent line content with leading indentation removed
     * @return the matched keyword range, or an empty range
     */
    public TextRange detectScenarioKeyword(
            int lineNumber,
            String stripLineContent
    ) {
        return detectKeyword(
                lineNumber,
                stripLineContent,
                GherkinDialect::getScenarioKeywords,
                GherkinDialect::getScenarioOutlineKeywords
        );
    }

    /**
     * Detects the first keyword supplied by any requested dialect category.
     * <p>
     * Each function selects one keyword category from the active dialect, for
     * example {@link GherkinDialect#getScenarioKeywords()}. The returned range
     * uses the supplied line number and starts at character zero because the
     * input is expected to have its indentation removed.
     *
     * @param lineNumber zero-based line number
     * @param stripLineContent line content with leading indentation removed
     * @param keywordSets functions selecting eligible dialect keyword lists
     * @return the first matching keyword range, or an empty range
     */
    @SafeVarargs
    public final TextRange detectKeyword(
            int lineNumber,
            String stripLineContent,
            Function<GherkinDialect, List<String>>... keywordSets
    ) {
        TextRange keywordRange = TextRange.of(0, 0, 0, 0);
        for (var keywordSet : keywordSets) {
            for (String keyword : keywordSet.apply(dialect)) {
                if (stripLineContent.startsWith(keyword)) {
                    keywordRange = TextRange.of(lineNumber, 0, lineNumber, keyword.length());
                    break;
                }
            }
        }
        return keywordRange;
    }

    /**
     * Tests whether a line starts with a keyword from any requested category.
     *
     * @param lineNumber zero-based line number
     * @param stripLineContent line content with leading indentation removed
     * @param keywordSets functions selecting eligible dialect keyword lists
     * @return {@code true} when a requested keyword is detected
     */
    @SafeVarargs
    public final boolean hasKeyword(
            int lineNumber,
            String stripLineContent,
            Function<GherkinDialect, List<String>>... keywordSets
    ) {
        return !detectKeyword(lineNumber, stripLineContent, keywordSets).isEmpty();
    }

    /**
     * Determines whether a recognized step keyword is valid in its surrounding
     * Gherkin context.
     * <p>
     * The preceding structural keyword is considered so that step syntax is
     * accepted only within feature content rather than wherever a matching word
     * happens to occur.
     *
     * @param lineNumber zero-based line number
     * @param stripLineContent line content with leading indentation removed
     * @return {@code true} when the line is interpreted as a step
     */
    public boolean isStep(
            int lineNumber,
            String stripLineContent
    ) {
        TextRange keywordRange = detectKeyword(
                lineNumber,
                stripLineContent,
                GherkinDialect::getStepKeywords
        );
        if (!keywordRange.isEmpty()) {
            String lastKeyword = lastKeyword(lineNumber - 1);
            return dialect.getFeatureContentKeywords().contains(lastKeyword);
        } else {
            return false;
        }
    }

    /**
     * Removes any recognized Gherkin keyword prefix from a line.
     *
     * @param lineNumber zero-based line number
     * @param stripLineContent line content with leading indentation removed
     * @return content following the keyword, or the original content when no
     *         keyword matches
     */
    public String removeKeyword(
            int lineNumber,
            String stripLineContent
    ) {
        var keywordRange = detectKeyword(
                lineNumber,
                stripLineContent,
                GherkinDialect::getKeywords
        );
        if (keywordRange.isEmpty()) {
            return stripLineContent;
        }
        return stripLineContent.substring(keywordRange.endLinePosition());
    }

    private String lastKeyword(
            int lineNumber
    ) {
        for (int i = lineNumber; i >= 0; i--) {
            String line = document.extractLine(i).stripLeading();
            if (line.startsWith("#")) {
                continue;
            }
            int position = line.indexOf(':');
            if (position > -1) {
                String keyword = line.substring(0, position);
                if (dialect.getKeywords().contains(keyword)) {
                    return keyword;
                }
            }
        }
        return null;
    }

    /**
     * Computes the Gherkin keywords that are structurally valid after a line.
     * <p>
     * Suggestions are derived from the closest preceding significant syntax
     * and use the active dialect. No keywords are suggested from inside a doc
     * string or data table.
     *
     * @param lineNumber zero-based insertion line
     * @return dialect-specific keyword candidates in recommendation order
     */
    public List<String> followingKeywords(
            int lineNumber
    ) {
        String line = lastLineWithContent(lineNumber);
        if (line == null) {
            line = "";
        }
        line = line.stripLeading();
        if (line.startsWith(GherkinLanguageConstants.DOCSTRING_SEPARATOR)
                || line.startsWith(GherkinLanguageConstants.DOCSTRING_ALTERNATIVE_SEPARATOR)
                || line.startsWith(GherkinLanguageConstants.TABLE_CELL_SEPARATOR)) {
            return List.of();
        }

        String lastKeyword = lastKeyword(lineNumber);
        List<String> result;
        if (lastKeyword == null) {
            result = suffix(dialect.getFeatureKeywords(), ":");
        } else if (dialect.getFeatureKeywords().contains(lastKeyword)) {
            result = suffix(dialect.getFeatureContentKeywords(), ":");
        } else if (dialect.getFeatureContentKeywords().contains(lastKeyword)) {
            result = dialect.getStepKeywords();
        } else {
            result = List.of();
        }
        return result;
    }

    private String lastLineWithContent(
            int lineNumber
    ) {
        for (int i = lineNumber; i >= 0; i--) {
            String line = document.extractLine(i);
            if (line.stripLeading().isEmpty()) {
                continue;
            }
            return line;
        }
        return null;
    }

    private static List<String> suffix(
            List<String> values,
            String suffix
    ) {
        return values.stream().map(s -> s + suffix).collect(toList());
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
            var matcher = PROPERTY_PATTERN.matcher(strippedLineContent);
            if (matcher.matches() && PROPERTIES_REQUIRING_PARSING.contains(matcher.group(1))) {
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
    private static Optional<String> extractProperty(
            String property,
            TextDocument document
    ) {
        Pattern pattern = Pattern.compile("\\s*#*\\s*" + property + "\\s*:\\s*([^\\s]+)\\s*");
        for (int l = 0; l < document.numberOfLines(); l++) {
            var matcher = pattern.matcher(document.extractLine(l));
            if (matcher.matches()) {
                return Optional.of(matcher.group(1));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds regular-expression matches in an inclusive range of lines.
     *
     * @param startLine first zero-based line to inspect, inclusive
     * @param endLine last zero-based line to inspect, inclusive
     * @param pattern pattern applied independently to each line
     * @param regexGroup capture group whose bounds define each result
     * @return captured segments in document order
     */
    public List<TextSegment> segmentsInLines(
            int startLine,
            int endLine,
            Pattern pattern,
            int regexGroup
    ) {
        List<TextSegment> segments = new ArrayList<>();
        for (int lineNumber = startLine; lineNumber <= endLine; lineNumber++) {
            segments.addAll(document.extractSegments(lineNumber, pattern, regexGroup));
        }
        return segments;
    }

    /**
     * Finds Gherkin tag names in an inclusive range of lines.
     *
     * @param startLine first zero-based line to inspect, inclusive
     * @param endLine last zero-based line to inspect, inclusive
     * @return tag-name segments without their leading {@code @}, in document
     *         order
     */
    public List<TextSegment> tagsInLines(
            int startLine,
            int endLine
    ) {
        return segmentsInLines(startLine, endLine, Pattern.compile("@(\\w+)"), 1);
    }

    /**
     * Builds the range used to validate content following a keyword marker.
     *
     * @param lineNumber zero-based line number
     * @param keyword keyword whose marker is excluded from the result
     * @return a half-open range from immediately after the keyword occurrence
     *         to the end of the line
     */
    public TextRange lineRangeWithoutKeyword(
            int lineNumber,
            String keyword
    ) {
        String line = document.extractLine(lineNumber);
        return TextRange.of(lineNumber, line.indexOf(keyword) + 1, lineNumber, line.length());
    }

}

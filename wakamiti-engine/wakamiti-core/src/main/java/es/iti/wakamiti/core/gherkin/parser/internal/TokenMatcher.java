/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser.internal;

import static es.iti.wakamiti.core.gherkin.parser.internal.Parser.ITokenMatcher;
import static es.iti.wakamiti.core.gherkin.parser.internal.Parser.TokenType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.iti.wakamiti.core.gherkin.parser.GherkinDialect;
import es.iti.wakamiti.core.gherkin.parser.GherkinDialectProvider;
import es.iti.wakamiti.core.gherkin.parser.GherkinLanguageConstants;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.internal.GherkinLineSpan;
import es.iti.wakamiti.core.gherkin.parser.internal.Token;

public class TokenMatcher implements ITokenMatcher {
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^\\s*#\\s*language\\s*:\\s*([a-zA-Z\\-_]+)\\s*$");
    private final GherkinDialectProvider dialectProvider;
    private GherkinDialect currentDialect;
    private String activeDocStringSeparator = null;
    private int indentToRemove = 0;

    public TokenMatcher(GherkinDialectProvider dialectProvider) {
        this.dialectProvider = dialectProvider;
        reset();
    }

    public TokenMatcher() {
        this(new GherkinDialectProvider());
    }

    public TokenMatcher(String defaultDialectName) {
        this(new GherkinDialectProvider(defaultDialectName));
    }

    @Override
    public void reset() {
        activeDocStringSeparator = null;
        indentToRemove = 0;
        currentDialect = dialectProvider.getDefaultDialect();
    }

    public GherkinDialect getCurrentDialect() {
        return currentDialect;
    }

    protected void setTokenMatched(es.iti.wakamiti.core.gherkin.parser.internal.Token token, TokenType matchedType, String text, String keyword, Integer indent, List<GherkinLineSpan> items) {
        token.matchedType = matchedType;
        token.matchedKeyword = keyword;
        token.matchedText = text;
        token.mathcedItems = items;
        token.matchedGherkinDialect = getCurrentDialect();
        token.matchedIndent = indent != null ? indent : (token.line == null ? 0 : token.line.indent());
        token.location = new Location(token.location.getLine(), token.matchedIndent + 1);
    }

    @Override
    public boolean match_EOF(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        if (token.isEOF()) {
            setTokenMatched(token, TokenType.EOF, null, null, null, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_Other(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        String text = token.line.getLineText(indentToRemove); //take the entire line, except removing DocString indents
        setTokenMatched(token, TokenType.Other, unescapeDocString(text), null, 0, null);
        return true;
    }

    @Override
    public boolean match_Empty(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        if (token.line.isEmpty()) {
            setTokenMatched(token, TokenType.Empty, null, null, null, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_Comment(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.COMMENT_PREFIX)) {
            String text = token.line.getLineText(0); //take the entire line
            setTokenMatched(token, TokenType.Comment, text, null, 0, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_Language(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        Matcher matcher = LANGUAGE_PATTERN.matcher(token.line.getLineText(0));
        if (matcher.matches()) {
            String language = matcher.group(1);
            setTokenMatched(token, TokenType.Language, language, null, null, null);

            currentDialect = dialectProvider.getDialect(language, token.location);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_TagLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        if (Arrays.stream(token.getTokenValue().split("\\s+"))
                .allMatch(t -> t.startsWith(GherkinLanguageConstants.TAG_PREFIX))) {
            setTokenMatched(token, TokenType.TagLine, null, null, null, token.line.getTags());
            return true;
        }
        return false;
    }

    @Override
    public boolean match_FeatureLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        return matchTitleLine(token, TokenType.FeatureLine, currentDialect.getFeatureKeywords());
    }

    @Override
    public boolean match_BackgroundLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        return matchTitleLineEmpty(token, TokenType.BackgroundLine, currentDialect.getBackgroundKeywords());
    }

    @Override
    public boolean match_ScenarioLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        return matchTitleLine(token, TokenType.ScenarioLine, currentDialect.getScenarioKeywords());
    }

    @Override
    public boolean match_ScenarioOutlineLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        return matchTitleLine(token, TokenType.ScenarioOutlineLine, currentDialect.getScenarioOutlineKeywords());
    }

    @Override
    public boolean match_ExamplesLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        return matchTitleLineEmpty(token, TokenType.ExamplesLine, currentDialect.getExamplesKeywords());
    }

    private boolean matchTitleLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token, TokenType tokenType, List<String> keywords) {
        for (String keyword : keywords) {
            if (token.line.startsWithTitleKeyword(keyword)) {
                String title = token.line.getRestTrimmed(keyword.length() + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length());
                if (title.isEmpty()) return false;
                setTokenMatched(token, tokenType, title, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    private boolean matchTitleLineEmpty(es.iti.wakamiti.core.gherkin.parser.internal.Token token, TokenType tokenType, List<String> keywords) {
        for (String keyword : keywords) {
            if (token.line.startsWithTitleKeyword(keyword)) {
                String title = token.line.getRestTrimmed(keyword.length() + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length());
                setTokenMatched(token, tokenType, title, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match_DocStringSeparator(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        return activeDocStringSeparator == null
                // open
                ? match_DocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_SEPARATOR, true) ||
                match_DocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_ALTERNATIVE_SEPARATOR, true)
                // close
                : match_DocStringSeparator(token, activeDocStringSeparator, false);
    }

    private boolean match_DocStringSeparator(es.iti.wakamiti.core.gherkin.parser.internal.Token token, String separator, boolean isOpen) {
        if (token.line.startsWith(separator)) {
            String contentType = null;
            if (isOpen) {
                contentType = token.line.getRestTrimmed(separator.length());
                activeDocStringSeparator = separator;
                indentToRemove = token.line.indent();
            } else {
                activeDocStringSeparator = null;
                indentToRemove = 0;
            }

            setTokenMatched(token, TokenType.DocStringSeparator, contentType, null, null, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_StepLine(es.iti.wakamiti.core.gherkin.parser.internal.Token token) {
        List<String> keywords = currentDialect.getStepKeywords();
        for (String keyword : keywords) {
            if (token.line.startsWith(keyword)) {
                String stepText = token.line.getRestTrimmed(keyword.length());
                setTokenMatched(token, TokenType.StepLine, stepText, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match_TableRow(Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.TABLE_CELL_SEPARATOR)) {
            setTokenMatched(token, TokenType.TableRow, null, null, null, token.line.getTableCells());
            return true;
        }
        return false;
    }

    private String unescapeDocString(String text) {
        return activeDocStringSeparator != null ? text.replace("\\\"\\\"\\\"", "\"\"\"") : text;
    }
}
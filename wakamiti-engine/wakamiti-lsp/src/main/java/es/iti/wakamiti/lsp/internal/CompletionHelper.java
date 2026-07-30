/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.slf4j.Logger;


/**
 * Provides the Completion Helper functionality used by Wakamiti.
 */
public class CompletionHelper {

    private final GherkinDocumentAssessor assessor;
    private final Logger logger;

    /**
     * Creates a completion service for one assessed document.
     *
     * @param assessor source of document structure, configuration and Wakamiti
     *                 step hints
     * @param logger logger used to explain why step suggestions are unavailable
     */
    public CompletionHelper(
            GherkinDocumentAssessor assessor,
            Logger logger
    ) {
        this.assessor = assessor;
        this.logger = logger;
    }

    /**
     * Collects context-sensitive completion items before a cursor position.
     * <p>
     * Configuration comments complete property names, step lines complete
     * registered step definitions, and structural positions complete keywords
     * from the document's active Gherkin dialect.
     *
     * @param lineNumber zero-based cursor line
     * @param rowPosition zero-based character offset within that line
     * @return matching completion items, possibly empty
     */
    public List<CompletionItem> collectCompletions(
            int lineNumber,
            int rowPosition
    ) {
        var textDocument = assessor.documentMap.document();
        return collectCompletions(
                lineNumber,
                textDocument.extractRange(TextRange.of(lineNumber, 0, lineNumber, rowPosition))
        );
    }

    private List<CompletionItem> collectCompletions(
            int lineNumber,
            String lineContent
    ) {
        String strippedLine = lineContent.stripLeading();
        if (strippedLine.startsWith("#") && !strippedLine.contains(":")) {
            return completeConfigurationProperties(lineContent);
        } else if (assessor.documentMap.isStep(lineNumber, strippedLine)) {
            return completeSteps(strippedLine);
        } else {
            return completeKeywords(lineNumber, strippedLine);
        }
    }

    private List<CompletionItem> completeConfigurationProperties(
            String lineContent
    ) {
        String line = lineContent.strip().replace("#", "").strip();
        return assessor.hinter.getAvailableProperties()
                .stream()
                .filter(property -> property.startsWith(line))
                .map(this::completionProperty)
                .collect(toList());
    }

    private CompletionItem completionProperty(
            String property
    ) {
        String suggestion = property + ": <value>";
        var item = new CompletionItem(suggestion);
        item.setKind(CompletionItemKind.Property);
        return item;
    }

    private List<CompletionItem> completeSteps(
            String lineContent
    ) {
        for (String keyword : assessor.documentMap.dialect().getStepKeywords()) {
            if (lineContent.startsWith(keyword)) {
                lineContent = lineContent.substring(keyword.length());
                break;
            }
        }
        var suggestions = assessor.hinter.getExpandedAvailableSteps();
        if (suggestions.isEmpty()) {
            logger.debug(
                    "no steps available! used configuration for hinter is:\n{}",
                    assessor.effectiveConfiguration
            );
        }
        if (suggestions.size() > assessor.maxSuggestions) {
            suggestions = assessor.hinter.getCompactAvailableSteps();
        }
        String suggestionPrefix = lineContent;
        return suggestions.stream()
                .filter(suggestion -> suggestion.startsWith(suggestionPrefix))
                .map(this::completionStep)
                .collect(toList());
    }

    private CompletionItem completionStep(
            String step
    ) {
        var item = new CompletionItem(step);
        item.setKind(CompletionItemKind.Interface);
        String insertText = step;

        int snippetArgumentCount = 0;
        var stepSnippetPattern = Pattern.compile("\\*|\\{[^\\}]*\\}");
        Matcher m = stepSnippetPattern.matcher(step);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            snippetArgumentCount++;
            String capture = insertText.substring(m.start(), m.end());
            capture = capture.replace("}", "\\}");
            capture = "${" + snippetArgumentCount + ":" + capture + "}";
            m.appendReplacement(sb, Matcher.quoteReplacement(capture));
        }
        m.appendTail(sb);

        if (snippetArgumentCount > 0) {
            item.setInsertText(sb.toString());
            item.setInsertTextFormat(InsertTextFormat.Snippet);
        }

        item.setDocumentation(step);
        item.setDetail(assessor.hinter.getStepProviderByDefinition(step));
        return item;
    }

    private CompletionItem completionItem(
            String suggestion,
            CompletionItemKind kind
    ) {
        var item = new CompletionItem(suggestion);
        item.setKind(kind);
        return item;
    }

    private List<CompletionItem> completeKeywords(
            int lineNumber,
            String strippedLine
    ) {
        return assessor.documentMap.followingKeywords(lineNumber - 1).stream()
                .filter(k -> k.startsWith(strippedLine))
                .map(keyword -> completionItem(keyword, CompletionItemKind.Keyword))
                .collect(toList());
    }

}

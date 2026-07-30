/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import static java.util.stream.Collectors.joining;

import es.iti.wakamiti.core.gherkin.parser.ScenarioDefinition;
import es.iti.wakamiti.core.gherkin.parser.ScenarioOutline;
import es.iti.wakamiti.core.gherkin.parser.Step;


/**
 * Provides the Snippets functionality used by Wakamiti.
 */
public final class Snippets {

    private Snippets() {
    }

    /**
     * Creates the source fragment for implementing one definition scenario.
     * <p>
     * The generated scenario uses the implementation document's dialect when
     * available, otherwise the definition dialect. It includes the scenario ID,
     * an initial one-to-one step map and the definition steps as comments that
     * the user can replace with implementation steps.
     *
     * @param id scenario identifier without the leading {@code @}
     * @param definition assessor containing the source scenario
     * @param implementation existing implementation assessor, or {@code null}
     *                       when a new implementation file is being created
     * @return a fragment ready to append to an implementation feature
     * @throws java.util.NoSuchElementException if the definition has no
     *                                          scenario with {@code id}
     */
    public static String implementationScenarioSnippet(
            String id,
            GherkinDocumentAssessor definition,
            GherkinDocumentAssessor implementation
    ) {
        String template =
                "\n\n"
                        + "{margin}@{id}\n"
                        + "{margin}# redefinition.stepMap: {map}\n"
                        + "{margin}{keyword}: {name}\n"
                        + "{margin}# Replace the following steps with implementation:\n"
                        + "{margin}#\n";

        var implementationDialect = (implementation == null
                ? definition.documentMap.dialect()
                : implementation.documentMap.dialect()
        );
        ScenarioDefinition scenario = definition.obtainScenarioById(id).orElseThrow();
        boolean isOutline = (scenario instanceof ScenarioOutline);

        String margin = "    ";
        String map = "1" + "-1".repeat(scenario.getSteps().size() - 1);
        var acceptedKeywords = (isOutline
                ? implementationDialect.getScenarioOutlineKeywords()
                : implementationDialect.getScenarioKeywords()
        );
        String keyword = acceptedKeywords.get(0);
        String name = scenario.getName();
        String steps = scenario.getSteps().stream()
                .map(Step::getText)
                .collect(joining("\n" + margin + "# ", margin + "# ", ""));

        return template
                .replace("{margin}", margin)
                .replace("{map}", map)
                .replace("{id}", id)
                .replace("{keyword}", keyword)
                .replace("{name}", name)
                .concat(steps);
    }

    /**
     * Creates the header of an implementation feature corresponding to a
     * definition document.
     * <p>
     * The snippet carries over the language, feature name and configured
     * implementation tag, and identifies the definition file in its
     * description.
     *
     * @param definition assessor for the definition feature
     * @return complete feature header ending before its scenarios
     */
    public static String implementationFeatureSnippet(
            GherkinDocumentAssessor definition
    ) {
        var dialect = definition.documentMap.dialect();

        String template =
                "# language: {locale}\n"
                        + "\n"
                        + "@{implementationTag}\n"
                        + "{keyword}: {name}\n"
                        + "Implementation corresponding to definition {definitionFile}\n"
                        + "\n"
                        + "";

        return template
                .replace("{locale}", dialect.getLanguage())
                .replace("{implementationTag}", definition.implementationTag())
                .replace("{keyword}", dialect.getFeatureKeywords().get(0))
                .replace("{name}", definition.parsedDocument.getFeature().getName())
                .replace("{definitionFile}", definition.path().getFileName().toString());
    }

}

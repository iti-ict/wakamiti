/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Provides the Gherkin Dialect functionality used by Wakamiti.
 */
public class GherkinDialect {

    private static final String STEP = "step";
    private static final String GIVEN = "given";
    private static final String WHEN = "when";
    private static final String THEN = "then";
    private static final String AND = "and";
    private static final String BUT = "but";
    private static final String BACKGROUND = "background";
    private static final String SCENARIO = "scenario";
    private static final String SCENARIO_OUTLINE = "scenarioOutline";
    private static final String EXAMPLES = "examples";
    private static final String FEATURE = "feature";
    private static final String ALL = "all";
    private static final String FEATURE_CONTENT = "feature-content";

    private final Map<String, List<String>> keywords;
    private String language;

    /**
     * Creates a dialect and derives combined keyword groups used by the lexer.
     * Duplicate localized spellings are removed while first-seen order is
     * preserved.
     *
     * @param language dialect language code
     * @param keywords keyword categories loaded from the dialect definition
     */
    public GherkinDialect(
            String language,
            Map<String, List<String>> keywords
    ) {
        this.language = language;
        this.keywords = new HashMap<>(keywords);
        this.keywords.put(
                STEP,
                merge(keywords, GIVEN, WHEN, THEN, AND, BUT)
        );
        this.keywords.put(
                FEATURE_CONTENT,
                merge(keywords, BACKGROUND, SCENARIO, SCENARIO_OUTLINE)
        );
        this.keywords.put(
                ALL,
                merge(keywords, GIVEN, WHEN, THEN, AND, BUT, BACKGROUND, EXAMPLES, FEATURE, SCENARIO, SCENARIO_OUTLINE)
        );
    }

    /**
     * Returns localized spellings of the feature keyword.
     *
     * @return feature keywords accepted by this dialect
     */
    public List<String> getFeatureKeywords() {
        return keywords.get(FEATURE);
    }

    /**
     * Returns localized spellings of the concrete-scenario keyword.
     *
     * @return scenario keywords accepted by this dialect
     */
    public List<String> getScenarioKeywords() {
        return keywords.get(SCENARIO);
    }

    /**
     * Returns the merged Given, When, Then, And, and But keyword set.
     *
     * @return all spellings that can introduce a step
     */
    public List<String> getStepKeywords() {
        return keywords.get(STEP);
    }

    /**
     * Returns localized spellings of the background keyword.
     *
     * @return background keywords accepted by this dialect
     */
    public List<String> getBackgroundKeywords() {
        return keywords.get(BACKGROUND);
    }

    /**
     * Returns localized spellings of the scenario-outline keyword.
     *
     * @return outline keywords accepted by this dialect
     */
    public List<String> getScenarioOutlineKeywords() {
        return keywords.get(SCENARIO_OUTLINE);
    }

    /**
     * Returns keywords that can introduce a direct child of a feature.
     *
     * @return merged background, scenario, and outline keywords
     */
    public List<String> getFeatureContentKeywords() {
        return keywords.get(FEATURE_CONTENT);
    }

    /**
     * Returns localized spellings of the examples keyword.
     *
     * @return examples keywords accepted by this dialect
     */
    public List<String> getExamplesKeywords() {
        return keywords.get(EXAMPLES);
    }

    /**
     * Returns localized Given-style precondition keywords.
     *
     * @return Given keywords accepted by this dialect
     */
    public List<String> getGivenKeywords() {
        return keywords.get(GIVEN);
    }

    /**
     * Returns localized When-style action keywords.
     *
     * @return When keywords accepted by this dialect
     */
    public List<String> getWhenKeywords() {
        return keywords.get(WHEN);
    }

    /**
     * Returns localized Then-style outcome keywords.
     *
     * @return Then keywords accepted by this dialect
     */
    public List<String> getThenKeywords() {
        return keywords.get(THEN);
    }

    /**
     * Returns localized conjunction keywords that continue the previous step
     * type.
     *
     * @return And keywords accepted by this dialect
     */
    public List<String> getAndKeywords() {
        return keywords.get(AND);
    }

    /**
     * Returns localized contrast keywords that continue the previous step
     * type.
     *
     * @return But keywords accepted by this dialect
     */
    public List<String> getButKeywords() {
        return keywords.get(BUT);
    }

    /**
     * Returns every structural and step keyword recognized by the dialect.
     *
     * @return a de-duplicated merged keyword list
     */
    public List<String> getKeywords() {
        return keywords.get(ALL);
    }

    /**
     * Returns the language code identifying this dialect.
     *
     * @return the Gherkin language code
     */
    public String getLanguage() {
        return language;
    }

    private static List<String> merge(
            Map<String, List<String>> keywords,
            String... keys
    ) {
        List<String> merged = new ArrayList<>();
        for (String key : keys) {
            for (String value : keywords.get(key)) {
                if (!merged.contains(value)) {
                    merged.add(value);
                }
            }
        }
        return new ArrayList<>(merged);
    }

}

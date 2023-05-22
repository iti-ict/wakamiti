/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.gherkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    public GherkinDialect(String language, Map<String, List<String>> keywords) {
        this.language = language;
        this.keywords = new HashMap<>(keywords);
        this.keywords.put(
            STEP,
            merge(keywords, GIVEN,WHEN, THEN, AND, BUT)
        );
        this.keywords.put(
            FEATURE_CONTENT,
            merge(keywords, BACKGROUND, SCENARIO, SCENARIO_OUTLINE)
        );
        this.keywords.put(
            ALL,
            merge(keywords, GIVEN,WHEN, THEN, AND, BUT, BACKGROUND, EXAMPLES, FEATURE, SCENARIO, SCENARIO_OUTLINE)
        );
    }


    public List<String> getFeatureKeywords() {
        return keywords.get(FEATURE);
    }

    public List<String> getScenarioKeywords() {
        return keywords.get(SCENARIO);
    }

    public List<String> getStepKeywords() {
        return keywords.get(STEP);
    }

    public List<String> getBackgroundKeywords() {
        return keywords.get(BACKGROUND);
    }

    public List<String> getScenarioOutlineKeywords() {
        return keywords.get(SCENARIO_OUTLINE);
    }


    public List<String> getFeatureContentKeywords() {
        return keywords.get(FEATURE_CONTENT);
    }


    public List<String> getExamplesKeywords() {
        return keywords.get(EXAMPLES);
    }

    public List<String> getGivenKeywords() {
        return keywords.get(GIVEN);
    }

    public List<String> getWhenKeywords() {
        return keywords.get(WHEN);
    }

    public List<String> getThenKeywords() {
        return keywords.get(THEN);
    }

    public List<String> getAndKeywords() {
        return keywords.get(AND);
    }

    public List<String> getButKeywords() {
        return keywords.get(BUT);
    }

    public List<String> getKeywords() {
        return keywords.get(ALL);
    }

    public String getLanguage() {
        return language;
    }



    private static List<String> merge(Map<String, List<String>> keywords, String... keys) {
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
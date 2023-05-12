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
    private final Map<String, List<String>> keywords;
    private String language;

    public GherkinDialect(String language, Map<String, List<String>> keywords) {
        this.language = language;
        this.keywords = new HashMap<>(keywords);
        this.keywords.put(
            "step",
            merge(keywords,"given","when","then","and","but")
        );
        this.keywords.put(
            "feature-content",
            merge(keywords,"background","scenario","scenarioOutline")
        );
        this.keywords.put(
            "all",
            merge(keywords,"given","when","then","and","but","background","examples","feature","scenario","scenarioOutline")
        );
    }


    public List<String> getFeatureKeywords() {
        return keywords.get("feature");
    }

    public List<String> getScenarioKeywords() {
        return keywords.get("scenario");
    }

    public List<String> getStepKeywords() {
        return keywords.get("step");
    }

    public List<String> getBackgroundKeywords() {
        return keywords.get("background");
    }

    public List<String> getScenarioOutlineKeywords() {
        return keywords.get("scenarioOutline");
    }


    public List<String> getFeatureContentKeywords() {
        return keywords.get("feature-content");
    }


    public List<String> getExamplesKeywords() {
        return keywords.get("examples");
    }

    public List<String> getGivenKeywords() {
        return keywords.get("given");
    }

    public List<String> getWhenKeywords() {
        return keywords.get("when");
    }

    public List<String> getThenKeywords() {
        return keywords.get("then");
    }

    public List<String> getAndKeywords() {
        return keywords.get("and");
    }

    public List<String> getButKeywords() {
        return keywords.get("but");
    }

    public List<String> getKeywords() {
        return keywords.get("all");
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
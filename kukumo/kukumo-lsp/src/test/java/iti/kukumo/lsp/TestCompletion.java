/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.Test;

import iti.kukumo.core.Kukumo;
import iti.kukumo.lsp.internal.*;


public class TestCompletion {

    @Test
    public void testCompletionOnPartialComment() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor("# redef");
    	var completions = asStringList(completion.collectCompletions(0, 7));
        assertThat(completions).containsExactly(
            "redefinition.enabled: <value>",
            "redefinition.definitionTag: <value>",
            "redefinition.implementationTag: <value>"
        );
    }


    @Test
    public void testCompletionOnEmptyComment() {
        GherkinDocumentAssessor completion = new GherkinDocumentAssessor("# ");
        var completions = asStringList(completion.collectCompletions(0, 2));
        assertThat(completions).containsExactly(
            Kukumo.defaultConfiguration().keyStream().map(key -> key+": <value>").toArray(String[]::new)
        );
    }


    @Test
    public void testCompletionOnCompletedComment() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor("## redefinition.enabled: true");
    	var completions = asStringList(completion.collectCompletions(0, 29));
        assertThat(completions).isEmpty();
    }




    @Test
    public void testKeywordCompletionOnEmptyDocument() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor("");
    	var completions = asStringList(completion.collectCompletions(0, 0));
    	assertThat(completions).containsExactly(
            "Feature:",
            "Business Need:",
            "Ability:"
        );

    	completion.updateDocument(TextRange.of(0, 0, 0, 0), "Fea");
    	completions = asStringList(completion.collectCompletions(0, 3));
    	assertThat(completions).containsExactly("Feature:");
    }


    @Test
    public void testKeywordCompletionOnSpanishDocument() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor("# language: es\n");
    	var completions = asStringList(completion.collectCompletions(1, 0));
        assertThat(completions).containsExactly("Característica:");
    }



    @Test
    public void testEmptyStepKeywordCompletion() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor(
            "Feature: Test feature\n"+
            "Scenario: Test scenario\n"
        );
    	var completions = asStringList(completion.collectCompletions(2, 0));
        assertThat(completions).contains(
            "* ",
            "Given that ",
            "Given ",
            "When ",
            "Then ",
            "And ",
            "But "
        );
    }



    @Test
    public void testStepCompletionShort() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor(
			"# nonRegisteredStepProviders : iti.kukumo.lsp.KukumoSteps\n"+
            "Feature: Test feature\n"+
            "Scenario: Test scenario\n"+
            "   Given that "
        );
    	completion.setMaxSuggestions(2);
    	var completions = asStringList(completion.collectCompletions(3, 14));
        assertThat(completions).contains(
            "the set of real numbers ℝ",
            "this step has an integer that {integer-assertion}"
        );
    }



    @Test
    public void testStepCompletionLong() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor(
            "# nonRegisteredStepProviders : iti.kukumo.lsp.KukumoSteps\n"+
            "Feature: Test feature\n"+
            "Scenario: Test scenario\n"+
			"   Given that this step"
        );
    	var completions = asStringList(completion.collectCompletions(3, 23));
        assertThat(completions)
        	.isNotEmpty()
        	.allMatch(suggestion -> suggestion.startsWith("this step"));
    }


	private List<String> asStringList(List<CompletionItem> completions) {
		return completions.stream().map(CompletionItem::getLabel).collect(Collectors.toList());
	}


}
package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;


public class TestCompletion {

    KukumoCompletion completion = new KukumoCompletion();

    @Test
    public void testCompletionOnEmptyComment() {
        completion.updateDocument("");
        var suggestions = completion.suggest(0, "# ");
        assertThat(suggestions).containsExactly(
            Kukumo.defaultConfiguration().keyStream().toArray(String[]::new)
        );
    }


    @Test
    public void testCompletionOnPartialComment() {
        completion.updateDocument("");
        var suggestions = completion.suggest(0, "# redef");
        assertThat(suggestions).containsExactly(
            "redefinition.enabled",
            "redefinition.definitionTag",
            "redefinition.implementationTag"
        );
    }


    @Test
    public void testCompletionOnCompletedComment() {
        completion.updateDocument("");
        var suggestions = completion.suggest(0, "## redefinition.enabled: true");
        assertThat(suggestions).isEmpty();
    }


    @Test
    public void testKeywordCompletionOnEmptyDocument() {
        completion.updateDocument("");
        assertThat(completion.suggest(0, ""))
            .containsExactly("Feature:","Business Need:","Ability:");
        assertThat(completion.suggest(0,"Fea"))
            .containsExactly("Feature:");
        assertThat(completion.suggest(0,"Feature: Something"))
            .isEmpty();
    }


    @Test
    public void testKeywordCompletionOnSpanishDocument() {
        completion.updateDocument("# language: es");
        var suggestions = completion.suggest(1, "");
        assertThat(suggestions).containsExactly("Característica:");
    }


    @Test
    public void testEmptyStepKeywordCompletion() {
        completion.updateDocument(new StringBuilder()
            .append("Feature: Test feature\n")
            .append("Scenario: Test scenario\n")
            .toString()
        );
        var suggestions = completion.suggest(2, "");
        assertThat(suggestions).contains(
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
        completion
        .setMaxSuggestions(2)
        .updateDocument(new StringBuilder()
            .append("# nonRegisteredStepProviders : iti.kukumo.lsp.KukumoSteps\n")
            .append("Feature: Test feature\n")
            .append("Scenario: Test scenario\n")
            .toString()
        );
        var suggestions = completion.suggest(3, "Given that ");
        assertThat(suggestions).containsExactly(
            "the set of real numbers ℝ",
            "this step has an integer that {integer-assertion}"
        );
    }


    @Test
    public void testStepCompletionLong() {
        completion
        .updateDocument(new StringBuilder()
            .append("# nonRegisteredStepProviders : iti.kukumo.lsp.KukumoSteps\n")
            .append("Feature: Test feature\n")
            .append("Scenario: Test scenario\n")
            .toString()
        );
        var suggestions = completion.suggest(3, "Given that this step");
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions).allMatch(suggestion -> suggestion.startsWith("this step"));
    }

}

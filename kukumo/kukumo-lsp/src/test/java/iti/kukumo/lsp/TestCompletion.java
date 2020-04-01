package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.Test;

import iti.kukumo.api.Kukumo;
import iti.kukumo.lsp.internal.GherkinCompleter;


public class TestCompletion {

    GherkinCompleter completion = new GherkinCompleter();

    @Test
    public void testCompletionOnEmptyComment() {
        completion.resetDocument("");
        var suggestions = completion.suggest(0, "# ");
        assertThat(asStringList(suggestions)).containsExactly(
            Kukumo.defaultConfiguration().keyStream().map(key -> key+": <value>").toArray(String[]::new)
        );
    }




    @Test
    public void testCompletionOnPartialComment() {
        completion.resetDocument("");
        var suggestions = asStringList(completion.suggest(0, "# redef"));
        assertThat(suggestions).containsExactly(
            "redefinition.enabled: <value>",
            "redefinition.definitionTag: <value>",
            "redefinition.implementationTag: <value>"
        );
    }




    @Test
    public void testCompletionOnCompletedComment() {
        completion.resetDocument("");
        var suggestions = completion.suggest(0, "## redefinition.enabled: true");
        assertThat(suggestions).isEmpty();
    }


    @Test
    public void testKeywordCompletionOnEmptyDocument() {
        completion.resetDocument("");
        assertThat(asStringList(completion.suggest(0, "")))
            .containsExactly("Feature:","Business Need:","Ability:");
        assertThat(asStringList(completion.suggest(0,"Fea")))
            .containsExactly("Feature:");
        assertThat(asStringList(completion.suggest(0,"Feature: Something")))
            .isEmpty();
    }


    @Test
    public void testKeywordCompletionOnSpanishDocument() {
        completion.resetDocument("# language: es");
        var suggestions = asStringList(completion.suggest(1, ""));
        assertThat(suggestions).containsExactly("Característica:");
    }


    @Test
    public void testEmptyStepKeywordCompletion() {
        completion.resetDocument(new StringBuilder()
            .append("Feature: Test feature\n")
            .append("Scenario: Test scenario\n")
            .toString()
        );
        var suggestions = asStringList(completion.suggest(2, ""));
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
        .resetDocument(new StringBuilder()
            .append("# nonRegisteredStepProviders : iti.kukumo.lsp.KukumoSteps\n")
            .append("Feature: Test feature\n")
            .append("Scenario: Test scenario\n")
            .toString()
        );
        var suggestions = asStringList(completion.suggest(3, "Given that "));
        assertThat(suggestions).containsExactly(
            "the set of real numbers ℝ",
            "this step has an integer that {integer-assertion}"
        );
    }


    @Test
    public void testStepCompletionLong() {
        completion
        .resetDocument(new StringBuilder()
            .append("# nonRegisteredStepProviders : iti.kukumo.lsp.KukumoSteps\n")
            .append("Feature: Test feature\n")
            .append("Scenario: Test scenario\n")
            .toString()
        );
        var suggestions = asStringList(completion.suggest(3, "Given that this step"));
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions).allMatch(suggestion -> suggestion.startsWith("this step"));
    }



    private List<String> asStringList(List<CompletionItem> suggestions) {
        return suggestions.stream().map(CompletionItem::getLabel).collect(Collectors.toList());
    }


}

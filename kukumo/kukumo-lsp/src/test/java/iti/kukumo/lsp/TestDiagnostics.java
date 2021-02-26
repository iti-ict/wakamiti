package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.Test;

import iti.kukumo.lsp.internal.*;


public class TestDiagnostics {

    @Test
    public void correctDocumentHasNoDiagnostics() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor(
    		"# language: en\n"+
    		"# comment\n"+
    		"Feature: This is a feature\n"+
    		"   Scenario: This is my scenario\n"
		);
    	var diagnostics = asStringList(completion.collectDiagnostics());
        assertThat(diagnostics).isEmpty();
    }


    @Test
    public void documentWithWrongLanguageHasDiagnostics() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor(
    		"# language: es\n"+
    		"# comment\n"+
    		"Feature: This is a feature\n"+
    		"   Scenario: This is my scenario\n"
		);
    	var diagnostics = asStringList(completion.collectDiagnostics());
        assertThat(diagnostics).containsExactly(
        	"(3:1): expected: #TagLine, #FeatureLine, #Comment, #Empty, got 'Feature: This is a feature'",
        	"(4:4): expected: #TagLine, #FeatureLine, #Comment, #Empty, got 'Scenario: This is my scenario'",
        	"(5:0): unexpected end of file, expected: #TagLine, #FeatureLine, #Comment, #Empty"
		);
    }


    @Test
    public void documentWithWrongLanguageHasNoDiagnosticsAfterFixingLanguage() {
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor(
    		"# language: es\n"+
    		"# comment\n"+
    		"Feature: This is a feature\n"+
    		"   Scenario: This is my scenario\n"
		);
    	var diagnostics = asStringList(completion.collectDiagnostics());
    	assertThat(diagnostics).containsExactly(
        	"(3:1): expected: #TagLine, #FeatureLine, #Comment, #Empty, got 'Feature: This is a feature'",
        	"(4:4): expected: #TagLine, #FeatureLine, #Comment, #Empty, got 'Scenario: This is my scenario'",
        	"(5:0): unexpected end of file, expected: #TagLine, #FeatureLine, #Comment, #Empty"
		);
    	completion.updateDocument(TextRange.of(0, 12, 0, 14),"en");
    	diagnostics = asStringList(completion.collectDiagnostics());
    	assertThat(diagnostics).isEmpty();
    }


    @Test
    public void documentWithWrongLanguageChangeDiagnosticsAfterFixingKeyword() {
    	/*
    	 * The Gherkin parser is not smart enough to check if a given keyword
    	 * is either wrongly written or part of the description of the previous
    	 * section. Temporary, the diagnostics will reflect the situation
    	 * using a indirect warning when the parsed document has no scenarios
    	 * as a consequence of wrong keywords.
    	 */
    	GherkinDocumentAssessor completion = new GherkinDocumentAssessor(
    		"# language: es\n"+
    		"# comment\n"+
    		"Feature: This is a feature\n"+
    		"   Scenario: This is my scenario\n"
		);
    	var diagnostics = asStringList(completion.collectDiagnostics());
    	assertThat(diagnostics).containsExactly(
        	"(3:1): expected: #TagLine, #FeatureLine, #Comment, #Empty, got 'Feature: This is a feature'",
        	"(4:4): expected: #TagLine, #FeatureLine, #Comment, #Empty, got 'Scenario: This is my scenario'",
        	"(5:0): unexpected end of file, expected: #TagLine, #FeatureLine, #Comment, #Empty"
		);
    	completion.updateDocument(TextRange.of(2, 0, 2, 7),"Característica");
    	diagnostics = asStringList(completion.collectDiagnostics());
    	assertThat(diagnostics).containsExactly(
        	"No scenarios defined"
		);
    }



	private List<String> asStringList(List<Diagnostic> diagnostics) {
		return diagnostics.stream().map(Diagnostic::getMessage).collect(Collectors.toList());
	}



}

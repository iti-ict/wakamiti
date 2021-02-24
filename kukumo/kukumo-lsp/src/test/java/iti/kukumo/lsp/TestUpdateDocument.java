package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.*;

import org.junit.Test;

import iti.kukumo.lsp.internal.*;

public class TestUpdateDocument {


    @Test
    public void deleteEmptyLine() {
        GherkinDocumentAssessor document = new GherkinDocumentAssessor(lines(
    		"line1",
    		"line2",
    		"",
    		"line4"
		));
        document.updateDocument(TextRange.of(2,0,3,0), "");
        assertThat(document.content()).isEqualTo(lines(
    		"line1",
    		"line2",
    		"line4"
		));
    }


    @Test
    public void insertEmptyLine() {
    	GherkinDocumentAssessor document = new GherkinDocumentAssessor(lines(
			"line1",
			"line2",
			"line3",
			"line4"
		));
        document.updateDocument(TextRange.of(2,5,2,5), "\n");
        assertThat(document.content()).isEqualTo(lines(
    		"line1",
    		"line2",
    		"line3",
    		"",
    		"line4"
		));
    }


    @Test
    public void insertEnterInLine() {
    	GherkinDocumentAssessor document = new GherkinDocumentAssessor(lines(
			"line1",
			"line2",
			"line3",
			"line4"
		));
        document.updateDocument(TextRange.of(2,2,2,2), "\n");
        assertThat(document.content()).isEqualTo(lines(
    		"line1",
    		"line2",
    		"li",
    		"ne3",
        	"line4"
		));
    }


    @Test
    public void insertCharacterInLine() {
    	GherkinDocumentAssessor document = new GherkinDocumentAssessor(lines(
			"line1",
			"line2",
			"line3",
			"line4"
		));
        document.updateDocument(TextRange.of(2,2,2,2), " ");
        assertThat(document.content()).isEqualTo(lines("line1",
    		"line2",
    		"li ne3",
    		"line4"
		));
    }


    @Test
    public void pasteLinesAtBegining() {
    	GherkinDocumentAssessor document = new GherkinDocumentAssessor(lines(
			"line1",
			"line2",
			"line3",
			"line4"
		));
        document.updateDocument(TextRange.of(0,0,0,0), "xxxxx\nyyyyy");
        assertThat(document.content()).isEqualTo(lines(
    		"xxxxx",
    		"yyyyyline1",
    		"line2",
    		"line3",
    		"line4"
		));
    }


    @Test
    public void pasteLinesAtEnd() {
    	GherkinDocumentAssessor document = new GherkinDocumentAssessor(lines(
			"line1",
			"line2",
			"line3",
			"line4",
			""
		));
        document.updateDocument(TextRange.of(4,0,4,0), "xxxxx\nyyyyy");
        assertThat(document.content()).isEqualTo(lines("line1",
    		"line2",
    		"line3",
    		"line4",
    		"xxxxx",
    		"yyyyy"
		));
    }


    @Test
    public void deleteNewLineSymbol() {
    	GherkinDocumentAssessor document = new GherkinDocumentAssessor(lines(
			"line1",
			"line2",
			"line3",
			"line4",
			""
		));
        document.updateDocument(TextRange.of(3,5,4,0), "");
        assertThat(document.content()).isEqualTo(lines("line1",
    		"line2",
    		"line3",
    		"line4"
		));
    }


    private String lines(String...lines) {
        return Stream.of(lines).collect(Collectors.joining("\n"));
    }

}

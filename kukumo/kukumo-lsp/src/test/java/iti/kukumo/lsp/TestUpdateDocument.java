package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import iti.kukumo.lsp.internal.GherkinDocument;
import iti.kukumo.lsp.internal.TextRange;

public class TestUpdateDocument {


    @Test
    public void deleteEmptyLine() {
        GherkinDocument document = new GherkinDocument();
        document.resetDocument(lines("line1","line2","","line4"));
        document.updateDocument(TextRange.of(2,0,3,0), "");
        assertThat(document.content()).isEqualTo(lines("line1","line2","line4"));
    }


    @Test
    public void insertEmptyLine() {
        GherkinDocument document = new GherkinDocument();
        document.resetDocument(lines("line1","line2","line3","line4"));
        document.updateDocument(TextRange.of(2,5,2,5), "\n");
        assertThat(document.content()).isEqualTo(lines("line1","line2","line3","","line4"));
    }


    @Test
    public void insertEnterInLine() {
        GherkinDocument document = new GherkinDocument();
        document.resetDocument(lines("line1","line2","line3","line4"));
        document.updateDocument(TextRange.of(2,2,2,2), "\n");
        assertThat(document.content()).isEqualTo(lines("line1","line2","li","ne3","line4"));
    }

    @Test
    public void insertCharacterInLine() {
        GherkinDocument document = new GherkinDocument();
        document.resetDocument(lines("line1","line2","line3","line4"));
        document.updateDocument(TextRange.of(2,2,2,2), " ");
        assertThat(document.content()).isEqualTo(lines("line1","line2","li ne3","line4"));
    }

    @Test
    public void pasteLinesAtBegining() {
        GherkinDocument document = new GherkinDocument();
        document.resetDocument(lines("line1","line2","line3","line4"));
        document.updateDocument(TextRange.of(0,0,0,0), "xxxxx\nyyyyy");
        assertThat(document.content()).isEqualTo(lines("xxxxx","yyyyyline1","line2","line3","line4"));
    }


    @Test
    public void pasteLinesAtEnd() {
        GherkinDocument document = new GherkinDocument();
        document.resetDocument(lines("line1","line2","line3","line4",""));
        document.updateDocument(TextRange.of(4,0,4,0), "xxxxx\nyyyyy");
        assertThat(document.content()).isEqualTo(lines("line1","line2","line3","line4","xxxxx","yyyyy"));
    }


    @Test
    public void deleteNewLineSymbol() {
        GherkinDocument document = new GherkinDocument();
        document.resetDocument(lines("line1","line2","line3","line4",""));
        document.updateDocument(TextRange.of(3,5,4,0), "");
        assertThat(document.content()).isEqualTo(lines("line1","line2","line3","line4"));
    }


    private String lines(String...lines) {
        return Stream.of(lines).collect(Collectors.joining("\n"));
    }

}

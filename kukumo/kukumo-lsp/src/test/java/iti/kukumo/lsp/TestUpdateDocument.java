package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import iti.kukumo.lsp.internal.GherkinCompleter;
import iti.kukumo.lsp.internal.TextRange;

public class TestUpdateDocument {


    @Test
    public void deleteEmptyLine() {
        GherkinCompleter completer = new GherkinCompleter();
        completer.resetDocument(lines("line1","line2","","line4"));
        completer.updateDocument(TextRange.of(2,0,3,0), "");
        assertThat(completer.currentContent()).isEqualTo(lines("line1","line2","line4"));
    }


    @Test
    public void insertEmptyLine() {
        GherkinCompleter completer = new GherkinCompleter();
        completer.resetDocument(lines("line1","line2","line3","line4"));
        completer.updateDocument(TextRange.of(2,5,2,5), "\n");
        assertThat(completer.currentContent()).isEqualTo(lines("line1","line2","line3","","line4"));
    }


    @Test
    public void insertEnterInLine() {
        GherkinCompleter completer = new GherkinCompleter();
        completer.resetDocument(lines("line1","line2","line3","line4"));
        completer.updateDocument(TextRange.of(2,2,2,2), "\n");
        assertThat(completer.currentContent()).isEqualTo(lines("line1","line2","li","ne3","line4"));
    }

    @Test
    public void insertCharacterInLine() {
        GherkinCompleter completer = new GherkinCompleter();
        completer.resetDocument(lines("line1","line2","line3","line4"));
        completer.updateDocument(TextRange.of(2,2,2,2), " ");
        assertThat(completer.currentContent()).isEqualTo(lines("line1","line2","li ne3","line4"));
    }

    @Test
    public void pasteLinesAtBegining() {
        GherkinCompleter completer = new GherkinCompleter();
        completer.resetDocument(lines("line1","line2","line3","line4"));
        completer.updateDocument(TextRange.of(0,0,0,0), "xxxxx\nyyyyy");
        assertThat(completer.currentContent()).isEqualTo(lines("xxxxx","yyyyyline1","line2","line3","line4"));
    }


    @Test
    public void pasteLinesAtEnd() {
        GherkinCompleter completer = new GherkinCompleter();
        completer.resetDocument(lines("line1","line2","line3","line4",""));
        completer.updateDocument(TextRange.of(4,0,4,0), "xxxxx\nyyyyy");
        assertThat(completer.currentContent()).isEqualTo(lines("line1","line2","line3","line4","xxxxx","yyyyy"));
    }


    @Test
    public void deleteNewLineSymbol() {
        GherkinCompleter completer = new GherkinCompleter();
        completer.resetDocument(lines("line1","line2","line3","line4",""));
        completer.updateDocument(TextRange.of(2,5,3,0), "xxxxx\nyyyyy");
        assertThat(completer.currentContent()).isEqualTo(lines("line1","line2","line3","line4","xxxxx","yyyyy"));
    }


    private String lines(String...lines) {
        return Stream.of(lines).collect(Collectors.joining("\n"));
    }

}

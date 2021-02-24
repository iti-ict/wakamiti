package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import iti.kukumo.lsp.internal.*;


public class TestTextDocument {

    @Test
    public void testLines() {
        assertThat(new TextDocument("line0\nline1").numberOfLines()).isEqualTo(2);
        assertThat(new TextDocument("line0\nline1\n").numberOfLines()).isEqualTo(2);
    }


    @Test
    public void testLine() {
        var document = document();
        assertThat(document.extractLine(0)).isEqualTo("line0");
        assertThat(document.extractLine(1)).isEqualTo("line1");
        assertThat(document.extractLine(2)).isEqualTo("line2");
        assertThat(document.extractLine(3)).isEqualTo("x");
    }

    @Test
    public void testExtract() {
        var document = document();
        var wholeRange = TextRange.of(0,0,3,1);
        assertThat(document.extractRange(wholeRange)).isEqualTo("line0\nline1\nline2\nx");
        var middleRange = TextRange.of(1,2,2,4);
        assertThat(document.extractRange(middleRange)).isEqualTo("ne1\nline");
    }


    @Test
    public void testReplace() {
        var range = TextRange.of(1,2,2,4);
        assertThat(document().replaceRange(range,"X").rawText()).isEqualTo("line0\nliX2\nx");
        assertThat(document().replaceRange(range,"XXXXXX").rawText()).isEqualTo("line0\nliXXXXXX2\nx");
        assertThat(document().replaceRange(range,"XXX\nYYY").rawText()).isEqualTo("line0\nliXXX\nYYY2\nx");
        assertThat(document().replaceRange(range,"XXX\nYYY").extractLine(1)).isEqualTo("liXXX");
        assertThat(document().replaceRange(range,"XXX\nYYY").extractLine(2)).isEqualTo("YYY2");
    }


    private TextDocument document() {
        return new TextDocument("line0\nline1\nline2\nx");
    }


}

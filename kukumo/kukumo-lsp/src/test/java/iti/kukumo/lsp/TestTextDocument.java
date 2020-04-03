package iti.kukumo.lsp;

import iti.kukumo.lsp.internal.TextDocument;
import iti.kukumo.lsp.internal.TextRange;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


public class TestTextDocument {

    @Test
    public void testLines() {
        assertThat(new TextDocument("line0\nline1").lines()).isEqualTo(2);
        assertThat(new TextDocument("line0\nline1\n").lines()).isEqualTo(2);
    }


    @Test
    public void testLine() {
        var document = document();
        assertThat(document.line(0)).isEqualTo("line0");
        assertThat(document.line(1)).isEqualTo("line1");
        assertThat(document.line(2)).isEqualTo("line2");
        assertThat(document.line(3)).isEqualTo("x");
    }

    @Test
    public void testExtract() {
        var document = document();
        var wholeRange = TextRange.of(0,0,3,1);
        assertThat(document.extract(wholeRange)).isEqualTo("line0\nline1\nline2\nx");
        var middleRange = TextRange.of(1,2,2,4);
        assertThat(document.extract(middleRange)).isEqualTo("ne1\nline");
    }


    @Test
    public void testReplace() {
        var range = TextRange.of(1,2,2,4);
        assertThat(document().replace(range,"X").raw()).isEqualTo("line0\nliX2\nx");
        assertThat(document().replace(range,"XXXXXX").raw()).isEqualTo("line0\nliXXXXXX2\nx");
        assertThat(document().replace(range,"XXX\nYYY").raw()).isEqualTo("line0\nliXXX\nYYY2\nx");
        assertThat(document().replace(range,"XXX\nYYY").line(1)).isEqualTo("liXXX");
        assertThat(document().replace(range,"XXX\nYYY").line(2)).isEqualTo("YYY2");
    }


    private TextDocument document() {
        return new TextDocument("line0\nline1\nline2\nx");
    }


}

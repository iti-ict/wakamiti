package iti.kukumo.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import iti.kukumo.lsp.internal.LineRange;

public class TestRange {



    @Test
    public void testTextReplacement() {
        LineRange range = LineRange.of(1, 2);
        assertThat(range.replaceString("abcdef","XYZ")).isEqualTo("aXYZdef");
    }

    @Test
    public void testTextExtract() {
        LineRange range = LineRange.of(1, 2);
        assertThat(range.extractString("abcdef")).isEqualTo("bc");
    }

}

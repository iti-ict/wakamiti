/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;

import iti.wakamiti.lsp.internal.*;


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


    @Test
    public void testSegment() {
    	var document = new TextDocument(
    		"#language: es\n"+
    		"\n"+
    		"@definition\n"+
    		"Característica: Operaciones con usuarios\n"+
    		"\n"+
    		"   @ID-1\n"+
    		"	Escenario: Se consulta un usuario existente\n"+
    		"	  Dado un usuario existente\n"+
    		"	  Cuando se consultan los datos del usuario\n"+
    		"\n"+
    		"   @ID-2\n"+
    		"   Escenario: Se hace alguna otra cosa\n"+
    		"      Dado que hay una cosa\n"+
    		"      Cuando se hace algo sobre esa cosa\n"+
    		"      Entonces pasa algo maravilloso\n"+
    		"\n"
    	);
    	assertThat(document.extractSegments(Pattern.compile("@ID-(\\w+)")))
    	.containsExactly(
			TextSegment.of(TextRange.of(5,3,5,8), "@ID-1"),
			TextSegment.of(TextRange.of(10,3,10,8), "@ID-2")
		);
    	assertThat(document.extractSegments(Pattern.compile("@ID-(\\w+)"),1))
    	.containsExactly(
			TextSegment.of(TextRange.of(5,3,5,8), "1"),
			TextSegment.of(TextRange.of(10,3,10,8), "2")
		);

    }


    private TextDocument document() {
        return new TextDocument("line0\nline1\nline2\nx");
    }


}
package iti.kukumo.lsp.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Store a text document marking positions of 'End-of-Line' symbols
 */
public class TextDocument {

    private static final char EOL = '\n';

    private String rawDocument;
    private int[] indexes;

    public TextDocument(String rawDocument) {
        this.rawDocument = rawDocument;
        this.indexes = locateLines(rawDocument);
    }

    public String line(int lineNumber) {
        int start = start(lineNumber);
        int end = start(lineNumber+1);
        if (lineNumber < indexes.length) {
            end--;
        }
        return rawDocument.substring(start,end);
    }

    public String extract(TextRange range) {
        int start = start(range.startLine()) + range.startLinePosition();
        int end = start(range.endLine()) + range.endLinePosition();
        return rawDocument.substring(start,end);
    }

    public TextDocument replace(TextRange range, String text) {
        int start = start(range.startLine()) + range.startLinePosition();
        int end = start(range.endLine()) + range.endLinePosition();
        this.rawDocument = rawDocument.substring(0,start) + text + rawDocument.substring(end);
        this.indexes = locateLines(rawDocument);
        return this;
    }

    public String raw() {
        return rawDocument;
    }

    public int lines() {
        return indexes[indexes.length-1] < rawDocument.length()-1 ? indexes.length + 1 : indexes.length;
    }


    private int start(int line) {
        if (line > indexes.length) {
            return rawDocument.length();
        }
        return line == 0 ? 0 : indexes[line-1]+1;
    }




    private static int[] locateLines(String rawDocument) {
        int index = 0;
        int start = 0;
        int occurrences = -1;
        while (index != -1) {
            occurrences ++;
            index = rawDocument.indexOf(EOL,start);
            start = index + 1;
        }
        int[] indexes = new int[occurrences];
        index = -1;
        for (int i=0;i<occurrences;i++) {
            index = rawDocument.indexOf(EOL,index+1);
            indexes[i] = index;
        }
        return indexes;
    }

}

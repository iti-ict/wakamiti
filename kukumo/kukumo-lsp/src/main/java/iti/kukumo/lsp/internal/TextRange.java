package iti.kukumo.lsp.internal;

import java.util.List;

public class TextRange {

    public static TextRange of(int startLine, int startLinePosition, int endLine, int endLinePosition) {
        TextRange range = new TextRange();
        range.startLine = startLine;
        range.startLinePosition = startLinePosition;
        range.endLine = endLine;
        range.endLinePosition = endLinePosition;
        return range;
    }

    public static TextRange empty() {
        return of(-1,-1,-1,-1);
    }


    private int startLine;
    private int startLinePosition;
    private int endLine;
    private int endLinePosition;


    public int startLine() {
        return startLine;
    }

    public int startLinePosition() {
        return startLinePosition;
    }

    public int endLine() {
        return endLine;
    }

    public int endLinePosition() {
        return endLinePosition;
    }

    public boolean isEmpty() {
        return startLine == endLine && startLinePosition == endLinePosition;
    }

    public boolean isSingleLine() {
        return startLine == endLine;
    }

    public boolean intersect(TextRange range) {
        if (startLine < range.startLine && endLine > range.startLine) {
            return true;
        }
        if (endLine == range.startLine &&
            startLinePosition < range.startLinePosition && endLinePosition > range.startLinePosition) {
            return true;
        }
        return false;
    }
}

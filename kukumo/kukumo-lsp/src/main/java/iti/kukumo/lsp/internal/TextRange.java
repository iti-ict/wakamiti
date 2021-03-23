package iti.kukumo.lsp.internal;

import org.eclipse.lsp4j.*;

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
        return (
            endLine == range.startLine &&
            startLinePosition < range.startLinePosition && endLinePosition > range.startLinePosition
        );
    }


    public TextSegment withContent(String content) {
    	return TextSegment.of(this, content);
    }


	@Override
	public String toString() {
		return String.format("[%d,%d - %d,%d]",startLine,startLinePosition,endLine,endLinePosition);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + endLine;
		result = prime * result + endLinePosition;
		result = prime * result + startLine;
		result = prime * result + startLinePosition;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextRange other = (TextRange) obj;
		if (endLine != other.endLine)
			return false;
		if (endLinePosition != other.endLinePosition)
			return false;
		if (startLine != other.startLine)
			return false;
		if (startLinePosition != other.startLinePosition)
			return false;
		return true;
	}


	public Range toLspRange() {
		return new Range(
			new Position(startLine, startLinePosition),
			new Position(endLine, endLinePosition)
		);
	}

}

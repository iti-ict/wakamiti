package iti.kukumo.lsp.internal;

public class LineRange {

    public static LineRange of(int start, int end) {
        LineRange range = new LineRange();
        range.start = start;
        range.end = end;
        return range;
    }

    public static LineRange empty() {
        return of(-1,-1);
    }


    private int start;
    private int end;


    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public String extractString(String string) {
        return isEmpty() ? "" : string.substring(start, end);
    }

    public boolean isEmpty() {
        return start == end;
    }

    public boolean intersect(LineRange range) {
        return this.start < range.end;
    }

    public String replaceString(String original, String delta) {
        return original.substring(0,start) + delta + ( end == original.length() ? "" : original.substring(end) );
    }


}

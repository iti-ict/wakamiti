package iti.kukumo.api.plan;

import java.util.Arrays;
import java.util.function.UnaryOperator;

public class DataTable {

    private final String[][] values;

    public DataTable(String[][] values) {
        this.values = values;
    }

    public String[][] getValues() {
        return values;
    }

    public int rows() {
        return values.length;
    }

    public int columns() {
        return (values.length == 0 ? 0 : values[0].length);
    }

    public String value(int row, int column) {
        return values[row][column];
    }


    public DataTable copy() {
        return new DataTable(copy(values,UnaryOperator.identity()));
    }

    public DataTable copy(UnaryOperator<String> transformer) {
        return new DataTable(copy(values,transformer));
    }

    private static String[][] copy(String[][] src, UnaryOperator<String> transformer) {
        final String[][] dst = new String[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = Arrays.copyOf(src[i], src[i].length);
            for (int j = 0; j<dst[i].length; j++) {
                dst[i][j] = transformer.apply(dst[i][j]);
            }
        }
        return dst;
    }

}

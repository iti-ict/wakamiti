/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.plan;


import java.util.Arrays;
import java.util.function.UnaryOperator;


public class DataTable implements PlanNodeData {

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


    @Override
    public PlanNodeData copy() {
        return new DataTable(copy(values, UnaryOperator.identity()));
    }


    @Override
    public PlanNodeData copyReplacingVariables(UnaryOperator<String> replacer) {
        return new DataTable(copy(values, replacer));
    }


    private static String[][] copy(String[][] src, UnaryOperator<String> replacer) {
        final String[][] dst = new String[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = Arrays.copyOf(src[i], src[i].length);
            for (int j = 0; j < dst[i].length; j++) {
                dst[i][j] = replacer.apply(dst[i][j]);
            }
        }
        return dst;
    }

}
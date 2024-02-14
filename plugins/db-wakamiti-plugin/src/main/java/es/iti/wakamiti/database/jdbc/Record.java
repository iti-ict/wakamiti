/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import java.util.Arrays;


public class Record {

    private final String[] data;
    private double score = 0;

    public Record(String[] data) {
        this.data = data;
    }

    public Record(String[] data, double score) {
        this.data = data;
        this.score = score;
    }

    public String[] data() {
        return data;
    }

    public Record score(double score) {
        this.score = score;
        return this;
    }

    public double score() {
        return score;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("Record[");
        if (score > 0) {
            builder.append("score=").append(score).append(", ");
        }
        builder.append("data=").append(Arrays.deepToString(data)).append("]");
        return builder.toString();
    }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import java.util.Arrays;


/**
 * Represents a record retrieved from a database query, used for
 * comparing values against predefined criteria.
 * The score field indicates the similarity of the values obtained
 * from the query with the predefined criteria.
 */
public class Record {

    private final String[] data;
    private double score = 0;

    /**
     * Constructs a new Record instance with the specified data.
     *
     * @param data The array of data retrieved from the query
     */
    public Record(String[] data) {
        this.data = data;
    }

    /**
     * Constructs a new Record instance with the specified data and score.
     *
     * @param data  The array of data retrieved from the query
     * @param score The score indicating the similarity of the data with predefined criteria
     */
    public Record(String[] data, double score) {
        this.data = data;
        this.score = score;
    }

    /**
     * Retrieves the data array of the record.
     *
     * @return The array of data retrieved from the query
     */
    public String[] data() {
        return data;
    }

    /**
     * Sets the score for the record.
     *
     * @param score The score indicating the similarity of the data with predefined criteria
     * @return The modified Record instance
     */
    public Record score(double score) {
        this.score = score;
        return this;
    }

    /**
     * Retrieves the score associated with the record.
     *
     * @return The score indicating the similarity of the data with predefined criteria
     */
    public double score() {
        return score;
    }

    /**
     * Returns a string representation of the Record, including its score and data.
     *
     * @return A string representing the Record
     */
    public String toString() {
        StringBuilder builder = new StringBuilder("Record[");
        if (score > 0) {
            builder.append("score=").append(score).append(", ");
        }
        builder.append("data=").append(Arrays.deepToString(data)).append("]");
        return builder.toString();
    }

}

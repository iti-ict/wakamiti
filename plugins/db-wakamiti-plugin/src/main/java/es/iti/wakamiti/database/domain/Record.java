package es.iti.wakamiti.database.domain;

import java.util.Arrays;
import java.util.Map;

public class Record {

    private final String[] data;
    private final double score;

    public Record(String[] data, double score) {
        this.data = data;
        this.score = score;
    }

    public String[] data() {
        return data;
    }

    public double score() {
        return score;
    }

    public String toString() {
        return "Record[score=" + score + ", data=" + Arrays.deepToString(data) + "]";
    }
}

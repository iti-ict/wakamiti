package es.iti.wakamiti.database;

public interface Similarity {

    double score(String value1, String value2);
}

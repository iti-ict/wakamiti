/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;

import java.util.function.UnaryOperator;

/**
 * Defines the case sensitivity used with the database
 */
public enum CaseSensitivity {

    /** Identifiers can be upper-cased or lower-cased indistinctly */
    INSENSITIVE(UnaryOperator.identity()),

    /** Identifiers should be lower-cased prior using them */
    LOWER_CASED(String::toLowerCase),

    /** Identifiers should be upper-cased prior using them */
    UPPER_CASED(String::toUpperCase);



    private final UnaryOperator<String> transformer;

    CaseSensitivity(UnaryOperator<String> transformer) {
        this.transformer = transformer;
    }


    public String format(String string) {
        return transformer.apply(string);
    }
}

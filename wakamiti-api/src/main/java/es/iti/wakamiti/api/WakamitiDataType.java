/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;


/**
 * Represents a data type in the context of Wakamiti.
 *
 * @param <T> The Java type associated with the Wakamiti data type.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface WakamitiDataType<T> {

    /**
     * Gets the name of the data type.
     *
     * @return The name of the data type.
     */
    String getName();

    /**
     * Gets the Java type associated with the data type.
     *
     * @return The Java type of the data.
     */
    Class<T> getJavaType();

    /**
     * Get the regular expression associated with the data
     * type for a specific locale.
     *
     * @param locale The locale for which to retrieve the
     *               regular expression.
     * @return The regular expression for the data type in
     * the specified locale.
     */
    String getRegex(Locale locale);

    /**
     * Gets a list of hints associated with the data type
     * for a specific locale.
     *
     * @param locale The locale for which to retrieve the hints.
     * @return A list of hints for the data type in the specified
     * locale.
     */
    List<String> getHints(Locale locale);

    /**
     * Parse a string value into an object of the data type for
     * a specific locale.
     *
     * @param locale The locale for which to perform the parsing.
     * @param value  The string value to parse.
     * @return An object of the data type parsed from the input
     * string.
     */
    T parse(Locale locale, String value);

    /**
     * Create and return a matcher for performing matches with the
     * data type in a character sequence for a specific locale.
     *
     * @param locale The locale for which to create the matcher.
     * @param value  The character sequence in which to perform
     *               matches.
     * @return A matcher for the data type in the specified character
     * sequence.
     */
    Matcher matcher(Locale locale, CharSequence value);


}
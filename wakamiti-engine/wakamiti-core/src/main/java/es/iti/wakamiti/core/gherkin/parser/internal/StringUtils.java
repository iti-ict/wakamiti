/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser.internal;


import java.util.List;


/**
 * Internal string helper methods used by the generated parser runtime.
 */
public class StringUtils {

    /**
     * Joins string items using a separator.
     *
     * @param separator separator inserted between consecutive items
     * @param items     ordered items to concatenate
     * @return joined text
     */
    public static String join(
            String separator,
            List<String> items
    ) {
        return join(ToString.DEFAULT, separator, items);
    }

    /**
     * Joins arbitrary items using a custom string converter.
     *
     * @param toString converter applied to each item
     * @param separator separator inserted between consecutive items
     * @param items ordered items to concatenate
     * @param <T> item type
     * @return joined text
     */
    public static <T> String join(
            ToString<T> toString,
            String separator,
            Iterable<T> items
    ) {
        StringBuilder sb = new StringBuilder();
        boolean useSeparator = false;
        for (T item : items) {
            if (useSeparator) sb.append(separator);
            useSeparator = true;
            sb.append(toString.toString(item));
        }
        return sb.toString();
    }

    /**
     * Removes leading Unicode whitespace characters from a string.
     *
     * @param s source text
     * @return string without leading whitespace
     */
    public static String ltrim(
            String s
    ) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return s.substring(i);
    }

    /**
     * Functional converter used by {@link #join(ToString, String, Iterable)}.
     *
     * @param <T> item type
     */
    public interface ToString<T> {

        /** Identity converter for string items. */
        ToString<String> DEFAULT = o -> o;

        String toString(
                T o
        );

    }

}

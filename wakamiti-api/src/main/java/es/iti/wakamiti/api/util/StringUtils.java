/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.text.StringEscapeUtils.escapeEcmaScript;


public class StringUtils {

    /**
     * Replaces the text values, formatted {@code {parameter}}, from the parameter map values.
     *
     * @param message The string message
     * @param parameters The parameters
     * @return The formatted text
     * @throws NoSuchFieldException If a parameter is not in the parameters map
     */
    public static String format(String message, Map<String, ?> parameters) throws NoSuchFieldException {
        Pattern pattern = Pattern.compile("\\{(\\w+?)}");
        List<String> missing = pattern.matcher(message).results().map(r -> r.group(1)).distinct()
                .filter(r -> !parameters.containsKey(r)).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new NoSuchFieldException("Missing parameters " + missing);
        }
        return pattern.matcher(message).replaceAll((r) -> escapeEcmaScript(Objects.toString(parameters.get(r.group(1)))));
    }

    /**
     * Replaces the text values, formatted {@code {}}, from the object array.
     *
     * @param message The string message
     * @param args The object array
     * @return The formatted text
     */
    public static String format(String message, Object... args) {
        StringBuilder s = new StringBuilder(message);
        for (Object arg : args) {
            int pos = s.indexOf("{}");
            if (pos == -1) {
                break;
            }
            s.replace(pos, pos + 2, String.valueOf(arg));
        }
        return s.toString();
    }
}

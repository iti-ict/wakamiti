/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.util;


import java.util.Locale;


/**
 * Utility class for loading Locale instances based on language information.
 * It provides a static method to create Locale instances from language strings.
 *
 * <p>This class is designed to work with language strings that follow the BCP
 * 47-language tag format.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class LocaleLoader {

    private LocaleLoader() {
        // avoid instantiation
    }

    /**
     * Creates a Locale instance based on the provided language string.
     *
     * <p>If the language string is null or empty, it returns the default locale (English).
     *
     * <p>The language string can be in the form of "language", "language-country", or "language-country-variant".
     * For example, "en", "en-US", or "en-US-POSIX".
     *
     * @param language The language string.
     * @return A Locale instance based on the provided language string.
     * @throws IllegalArgumentException If the language string is malformed.
     */
    public static Locale forLanguage(String language) {
        Locale locale = null;
        if (language == null || language.isEmpty()) {
            return Locale.ENGLISH;
        }
        String[] segments = language.split("[-_]");
        switch (segments.length) {
        case 1:
            locale = new Locale(segments[0]);
            break;
        case 2:
            locale = new Locale(segments[0], segments[1]);
            break;
        case 3:
            locale = new Locale(segments[0], segments[1], segments[2]);
            break;
        default:
            throw new IllegalArgumentException("Malformed language name :" + language);
        }
        return locale;

    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.core.util;


import java.util.Locale;


public class LocaleLoader {

    private LocaleLoader() {
        // avoid instantiation
    }


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
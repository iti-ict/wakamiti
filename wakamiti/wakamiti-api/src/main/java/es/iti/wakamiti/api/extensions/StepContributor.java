/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.Extension;
import es.iti.commons.jext.ExtensionPoint;
import es.iti.commons.jext.LoadStrategy;
import es.iti.wakamiti.api.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;


@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface StepContributor extends Contributor {

    /**
     * Cut the string into blocks of words separated by the indicated tokens
     * until the maximum number of characters allowed is reached.
     *
     * @param str        The origin string
     * @param maxLength  The total maximum number of characters
     * @param separators The word separator tokens
     * @return The abbreviated string
     */
    private static String abbreviate(String str, int maxLength, String... separators) {
        String tokens = String.join("", separators);
        str = str.length() < maxLength + 1 ? str + "."
                : StringUtils.abbreviate(str, " ", maxLength + 2).trim();
        String aux = str.replaceAll(String.format("(?:.(?![%s]))+$", tokens), "");
        return aux.isBlank()
                ? str.length() < maxLength + 1 ? str : str.substring(0, maxLength)
                : aux;
    }

    default String info() {
        Pair<String, String> info = Optional.ofNullable(this.getClass().getAnnotation(Extension.class))
                .map(ext -> new Pair<>(ext.provider(), ext.name()))
                .orElseGet(() -> new Pair<>(getClass().getPackageName(), getClass().getSimpleName()))
                .mapEach(str -> abbreviate(str.toString(), 14, ".", "\\-", "_", "A-Z"));

        return String.format("%s:%s", info.key(), info.value());
    }

}
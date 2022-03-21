/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin.internal;

// http://rosettacode.org/wiki/String_length#Java
public class SymbolCounter {
    public static int countSymbols(String string) {
        return string.codePointCount(0, string.length());
    }
}
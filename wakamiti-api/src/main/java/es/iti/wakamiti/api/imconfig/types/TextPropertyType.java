/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.types;


import es.iti.wakamiti.api.imconfig.PropertyType;

import java.util.regex.Pattern;


public class TextPropertyType implements PropertyType {

    private final Pattern pattern;

    public TextPropertyType(String pattern) {
        this.pattern = (pattern == null ? null : Pattern.compile(pattern));
    }

    @Override
    public String name() {
        return "text";
    }

    @Override
    public boolean accepts(String value) {
        return pattern == null || pattern.matcher(value).matches();
    }

    @Override
    public String hint() {
        return pattern == null ? "Any text" : "Text satisfying regex //"+pattern+"//";
    }

    public String pattern() {
        return this.pattern.pattern();
    }
}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.imconfig.types;


import java.util.List;
import java.util.stream.Collectors;
import es.iti.commons.imconfig.PropertyType;


public class EnumPropertyType implements PropertyType {

    private final List<String> values;

    public EnumPropertyType(List<String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Enumeration values cannot be empty");
        }
        this.values = values.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "enum";
    }

    @Override
    public String hint() {
        return "One of the following: "+ String.join(", ", values);
    }

    @Override
    public boolean accepts(String value) {
        return values.contains(value.toLowerCase());
    }

}
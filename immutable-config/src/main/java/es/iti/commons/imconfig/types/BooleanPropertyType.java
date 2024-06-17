/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.imconfig.types;


import es.iti.commons.imconfig.PropertyType;


public class BooleanPropertyType implements PropertyType {

    @Override
    public String name() {
        return "boolean";
    }

    @Override
    public boolean accepts(String value) {
        return "true".equals(value) || "false".equals(value);
    }

    @Override
    public String hint() {
        return "true | false";
    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.imconfig.types;


import es.iti.commons.imconfig.PropertyType;


public class DecimalPropertyType implements PropertyType {

    private final Double min;
    private final Double max;

    public DecimalPropertyType(Number min, Number max) {
        this.min = (min == null ? null : min.doubleValue());
        this.max = (max == null ? null : max.doubleValue());
        if (this.min != null && this.max != null && this.min > this.max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
    }

    @Override
    public String name() {
        return "decimal";
    }

    @Override
    public String hint() {
        String hint;
        if (min==null && max==null) {
            hint = "Any decimal number";
        } else if (min==null) {
            hint = "Decimal number less than "+max;
        } else if (max==null) {
            hint = "Decimal number greater than "+min;
        } else {
            hint = "Decimal number between "+min+" and "+max;
        }
        return hint;
    }

    @Override
    public boolean accepts(String value) {
        boolean accepted = true;
        try {
            Double parsedValue = Double.valueOf(value);
            if (min != null && parsedValue.compareTo(min) < 0) {
                accepted = false;
            }
            if (max != null && parsedValue.compareTo(max) > 0) {
                accepted = false;
            }
            return accepted;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
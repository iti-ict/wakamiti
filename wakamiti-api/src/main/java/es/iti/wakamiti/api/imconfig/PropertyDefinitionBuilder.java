/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import es.iti.wakamiti.api.imconfig.types.*;

import java.util.List;
import java.util.Objects;


/**
 * This class allows you to create new {@link PropertyDefinition} objects in a fluent
 * manner, setting only the actual information you required. Invoke {@link #build()}
 * after setting the attributes to obtain the created object.
 *
 */
public class PropertyDefinitionBuilder {

    private String property;
    private String description;
    private boolean required;
    private boolean multivalue;
    private String defaultValue;
    private PropertyType propertyType;


    public PropertyDefinitionBuilder property(String property) {
        this.property = property;
        return this;
    }

    public PropertyDefinitionBuilder description(String description) {
        this.description = description;
        return this;
    }

    public PropertyDefinitionBuilder required() {
        this.required = true;
        return this;
    }


    public PropertyDefinitionBuilder required(Boolean required) {
        this.required = Boolean.TRUE.equals(required);
        return this;
    }


    public PropertyDefinitionBuilder multivalue() {
        this.multivalue = true;
        return this;
    }


    public PropertyDefinitionBuilder multivalue(Boolean multivalue) {
        this.multivalue = Boolean.TRUE.equals(multivalue);
        return this;
    }


    public PropertyDefinitionBuilder defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }


    public PropertyDefinitionBuilder textType(String pattern) {
        this.propertyType = new TextPropertyType(pattern);
        return this;
    }

    public PropertyDefinitionBuilder textType() {
        return textType(null);
    }


    public PropertyDefinitionBuilder integerType(Number min, Number max) {
        this.propertyType = new IntegerPropertyType(min, max);
        return this;
    }

    public PropertyDefinitionBuilder integerType() {
        return integerType(null,null);
    }

    public PropertyDefinitionBuilder decimalType(Number min, Number max) {
        this.propertyType = new DecimalPropertyType(min, max);
        return this;
    }

    public PropertyDefinitionBuilder decimalType() {
        return decimalType(null,null);
    }

    public PropertyDefinitionBuilder enumType(String... values) {
        this.propertyType = new EnumPropertyType(List.of(values));
        return this;
    }

    public PropertyDefinitionBuilder booleanType() {
        this.propertyType = new BooleanPropertyType();
        return this;
    }


    public PropertyDefinitionBuilder propertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
        return this;
    }


    public PropertyDefinition build() {
        Objects.requireNonNull(property);
        Objects.requireNonNull(propertyType);
        return new PropertyDefinition(property, description, required, multivalue, defaultValue, propertyType);
    }




}
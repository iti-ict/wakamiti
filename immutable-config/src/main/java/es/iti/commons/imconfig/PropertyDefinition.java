/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.imconfig;


import java.util.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;


/**
 * This class instantiates an immutable value object that represents
 * the definition of a given property.
 * <p>
 * Property definitions can be created manually or read from a YAML file.
 *
 */
public class PropertyDefinition {

    /**
     * Get a new builder
     */
    public static PropertyDefinitionBuilder builder() {
        return new PropertyDefinitionBuilder();
    }


    /**
     * Get a new builder for the given property
     */
    public static PropertyDefinitionBuilder builder(String property) {
        return new PropertyDefinitionBuilder().property(property);
    }


    private final String property;
    private final String description;
    private final boolean required;
    private final boolean multivalue;
    private final String defaultValue;
    private final PropertyType propertyType;


    PropertyDefinition(
        String property,
        String description,
        boolean required,
        boolean multivalue,
        String defaultValue,
        PropertyType type
    ) {
        this.property = property;
        this.description = (description == null ? "" : description);
        this.defaultValue = defaultValue;
        this.multivalue = multivalue;
        this.required = required;
        this.propertyType = type;
    }


    public String property() {
        return property;
    }

    public String description() {
        return description;
    }

    public boolean required() {
        return required;
    }

    public Optional<String> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public String type() {
        return propertyType.name();
    }

    public boolean multivalue() {
        return multivalue;
    }

    public String hint() {
        return String.format(
            "%s%s%s",
            propertyType.hint(),
            defaultValue != null ? " [default: "+defaultValue+"]" : "",
            required ? " (required)" : ""
        );
    }


    public Optional<String> validate(String value) {
        if (value == null || value.isBlank()) {
            if (required) {
                return Optional.of("Property is required but not present");
            }
        } else if (!propertyType.accepts(value)) {
            return Optional.of("Invalid value '"+value+"', expected: "+hint());
        }
        return Optional.empty();
    }


    @Override
    public String toString() {
       var hint = multivalue ?
           "List of "+hint().substring(0,1).toLowerCase()+hint().substring(1) :
           hint();
       return String.format(
           "- %s: %s%s",
           property,
           description.isBlank() ? hint : description,
           description.isBlank() ? "" : "\n  "+hint
       );
    }



}

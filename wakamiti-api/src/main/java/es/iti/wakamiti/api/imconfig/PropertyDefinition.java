/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import java.util.Optional;


/**
 * This class instantiates an immutable value object that represents
 * the definition of a given property.
 * <p>
 * Property definitions can be created manually or read from a YAML file.
 */
public class PropertyDefinition {

    /**
     * Get a new builder
      *
      * @return the resulting value
     */
    public static PropertyDefinitionBuilder builder() {
        return new PropertyDefinitionBuilder();
    }

    /**
     * Get a new builder for the given property
      *
      * @param property the property value
      * @return the resulting value
     */
    public static PropertyDefinitionBuilder builder(
            String property
    ) {
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

    /**
     * Returns the configuration key governed by this definition.
     *
     * @return the property key
     */
    public String property() {
        return property;
    }

    /**
     * Returns the human-readable purpose of the property.
     *
     * @return the description, or an empty string when none was supplied
     */
    public String description() {
        return description;
    }

    /**
     * Indicates whether a non-blank value must be present.
     *
     * @return {@code true} when absence is a validation error
     */
    public boolean required() {
        return required;
    }

    /**
     * Returns the value to apply when the configuration omits this property.
     *
     * @return the configured default, or an empty optional when no default is
     * defined
     */
    public Optional<String> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    /**
     * Returns the logical name of the value type used for validation.
     *
     * @return the underlying {@link PropertyType} name, such as
     * {@code text}, {@code integer}, or {@code enum}
     */
    public String type() {
        return propertyType.name();
    }

    /**
     * Indicates whether the property represents a collection of independently
     * validated values.
     *
     * @return {@code true} for multi-valued properties
     */
    public boolean multivalue() {
        return multivalue;
    }

    /**
     * Builds a compact usage hint from the type constraints, default value, and
     * required flag.
     *
     * @return a user-facing description of the accepted value
     */
    public String hint() {
        return String.format(
                "%s%s%s",
                propertyType.hint(),
                defaultValue != null ? " [default: " + defaultValue + "]" : "",
                required ? " (required)" : ""
        );
    }

    /**
     * Validates one textual value against this definition.
     * <p>
     * Blank values are accepted unless the property is required. Non-blank
     * values are delegated to the configured {@link PropertyType}.
     * </p>
     *
     * @param value the value to validate; may be {@code null} to represent an
     *              absent property
     * @return an explanatory validation error, or an empty optional when the
     * value is valid
     */
    public Optional<String> validate(
            String value
    ) {
        if (value == null || value.isBlank()) {
            if (required) {
                return Optional.of("Property is required but not present");
            }
        } else if (!propertyType.accepts(value)) {
            return Optional.of("Invalid value '" + value + "', expected: " + hint());
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        var hint = multivalue
                ? "List of " + hint().substring(0, 1).toLowerCase() + hint().substring(1)
                : hint();
        return String.format(
                "- %s: %s%s",
                property,
                description.isBlank() ? hint : description,
                description.isBlank() ? "" : "\n  " + hint
        );
    }

}

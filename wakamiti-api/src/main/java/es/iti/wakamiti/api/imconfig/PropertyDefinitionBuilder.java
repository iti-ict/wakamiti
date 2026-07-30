/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import java.util.List;
import java.util.Objects;

import es.iti.wakamiti.api.imconfig.types.BooleanPropertyType;
import es.iti.wakamiti.api.imconfig.types.DecimalPropertyType;
import es.iti.wakamiti.api.imconfig.types.EnumPropertyType;
import es.iti.wakamiti.api.imconfig.types.IntegerPropertyType;
import es.iti.wakamiti.api.imconfig.types.TextPropertyType;


/**
 * This class allows you to create new {@link PropertyDefinition} objects in a fluent
 * manner, setting only the actual information you required. Invoke {@link #build()}
 * after setting the attributes to obtain the created object.
 */
public class PropertyDefinitionBuilder {

    private String property;
    private String description;
    private boolean required;
    private boolean multivalue;
    private String defaultValue;
    private PropertyType propertyType;

    /**
     * Selects the configuration key described by the definition.
     *
     * @param property the non-null property key
     * @return this builder
     */
    public PropertyDefinitionBuilder property(
            String property
    ) {
        this.property = property;
        return this;
    }

    /**
     * Sets the user-facing explanation shown when configuration help is
     * generated.
     *
     * @param description the property purpose, or {@code null} for no
     *                    description
     * @return this builder
     */
    public PropertyDefinitionBuilder description(
            String description
    ) {
        this.description = description;
        return this;
    }

    /**
     * Marks the property as mandatory.
     *
     * @return this builder
     */
    public PropertyDefinitionBuilder required() {
        this.required = true;
        return this;
    }

    /**
     * Configures whether the property is mandatory.
     *
     * @param required {@code true} to reject absent or blank values;
     *                 {@code null} is treated as {@code false}
     * @return this builder
     */
    public PropertyDefinitionBuilder required(
            Boolean required
    ) {
        this.required = Boolean.TRUE.equals(required);
        return this;
    }

    /**
     * Marks the property as accepting multiple independently validated values.
     *
     * @return this builder
     */
    public PropertyDefinitionBuilder multivalue() {
        this.multivalue = true;
        return this;
    }

    /**
     * Configures whether the property accepts multiple values.
     *
     * @param multivalue {@code true} for a collection-valued property;
     *                   {@code null} is treated as {@code false}
     * @return this builder
     */
    public PropertyDefinitionBuilder multivalue(
            Boolean multivalue
    ) {
        this.multivalue = Boolean.TRUE.equals(multivalue);
        return this;
    }

    /**
     * Sets the textual value used when the property is not explicitly
     * configured.
     *
     * @param defaultValue the default value, or {@code null} to leave it
     *                     undefined
     * @return this builder
     */
    public PropertyDefinitionBuilder defaultValue(
            String defaultValue
    ) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Constrains the property to text matching a regular expression.
     *
     * @param pattern the complete-match regular expression, or {@code null} to
     *                accept any text
     * @return this builder
     * @throws java.util.regex.PatternSyntaxException if {@code pattern} is not
     *                                               a valid expression
     */
    public PropertyDefinitionBuilder textType(
            String pattern
    ) {
        this.propertyType = new TextPropertyType(pattern);
        return this;
    }

    /**
     * Configures an unconstrained textual property.
     *
     * @return this builder
     */
    public PropertyDefinitionBuilder textType() {
        return textType(null);
    }

    /**
     * Constrains the property to an integer within optional inclusive bounds.
     *
     * @param min the minimum accepted value, or {@code null} for no lower bound
     * @param max the maximum accepted value, or {@code null} for no upper bound
     * @return this builder
     * @throws IllegalArgumentException if the minimum exceeds the maximum
     */
    public PropertyDefinitionBuilder integerType(
            Number min,
            Number max
    ) {
        this.propertyType = new IntegerPropertyType(min, max);
        return this;
    }

    /**
     * Configures an integer property without range limits.
     *
     * @return this builder
     */
    public PropertyDefinitionBuilder integerType() {
        return integerType(null, null);
    }

    /**
     * Constrains the property to a decimal number within optional inclusive
     * bounds.
     *
     * @param min the minimum accepted value, or {@code null} for no lower bound
     * @param max the maximum accepted value, or {@code null} for no upper bound
     * @return this builder
     * @throws IllegalArgumentException if the minimum exceeds the maximum
     */
    public PropertyDefinitionBuilder decimalType(
            Number min,
            Number max
    ) {
        this.propertyType = new DecimalPropertyType(min, max);
        return this;
    }

    /**
     * Configures a decimal property without range limits.
     *
     * @return this builder
     */
    public PropertyDefinitionBuilder decimalType() {
        return decimalType(null, null);
    }

    /**
     * Restricts the property to one of a fixed set of case-insensitive values.
     *
     * @param values the accepted values; at least one value is required
     * @return this builder
     * @throws IllegalArgumentException if no values are provided
     */
    public PropertyDefinitionBuilder enumType(
            String... values
    ) {
        this.propertyType = new EnumPropertyType(List.of(values));
        return this;
    }

    /**
     * Configures a Boolean property.
     *
     * @return this builder
     */
    public PropertyDefinitionBuilder booleanType() {
        this.propertyType = new BooleanPropertyType();
        return this;
    }

    /**
     * Uses a custom value validator instead of one of the built-in property
     * types.
     *
     * @param propertyType the non-null validator and hint provider
     * @return this builder
     */
    public PropertyDefinitionBuilder propertyType(
            PropertyType propertyType
    ) {
        this.propertyType = propertyType;
        return this;
    }

    /**
     * Creates the immutable definition represented by the current builder
     * state.
     *
     * @return a new property definition
     * @throws NullPointerException if the property key or property type has not
     *                              been configured
     */
    public PropertyDefinition build() {
        Objects.requireNonNull(property);
        Objects.requireNonNull(propertyType);
        return new PropertyDefinition(property, description, required, multivalue, defaultValue, propertyType);
    }

}

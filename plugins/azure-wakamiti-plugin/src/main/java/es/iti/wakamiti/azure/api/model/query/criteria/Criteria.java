/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query.criteria;


import static es.iti.wakamiti.azure.internal.Util.path;
import static org.apache.commons.text.StringEscapeUtils.escapeEcmaScript;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.iti.wakamiti.azure.api.model.query.Field;
import es.iti.wakamiti.azure.internal.Util;


/**
 * Provides the Criteria functionality used by Wakamiti.
 */
public final class Criteria implements Expression {

    private static final String NULL = "NULL";

    private final Field field;
    private Operator operator;
    private String value;

    private Criteria(
            Field field
    ) {
        this.field = field;
    }

    /**
     * Starts a WIQL condition for an Azure field name.
     *
     * @param field reference name, with or without square brackets
     * @return incomplete criteria ready for an operator
     */
    public static Criteria field(
            String field
    ) {
        return new Criteria(Field.of(field));
    }

    /**
     * Starts a WIQL criterion for an already normalized field token.
     *
     * @param field normalized field token
     * @return incomplete criteria for that field
     */
    public static Criteria field(
            Field field
    ) {
        return new Criteria(field);
    }

    /**
     * Creates an equality predicate. Values beginning with {@code @} remain
     * WIQL macros; other values are escaped and quoted.
     *
     * @param value literal value or macro such as {@code @Project}
     * @return completed predicate
     */
    public Predicate isEqualsTo(
            String value
    ) {
        this.operator = Operator.EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Creates an equality predicate after normalizing a classification path.
     *
     * @param value classification path
     * @return equality predicate using Azure path separators
     */
    public Predicate isEqualsTo(
            Path value
    ) {
        return isEqualsTo(path(value));
    }

    /**
     * Creates an inequality predicate for a literal value or WIQL macro.
     *
     * @param value literal or WIQL macro
     * @return inequality predicate
     */
    public Predicate isNotEqualsTo(
            String value
    ) {
        this.operator = Operator.NOT_EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Creates an inequality predicate after normalizing a classification path.
     *
     * @param value classification path
     * @return path inequality predicate
     */
    public Predicate isNotEqualsTo(
            Path value
    ) {
        return isNotEqualsTo(path(value));
    }

    /**
     * Creates a strict upper-bound comparison for this field.
     *
     * @param value comparison value or WIQL macro
     * @return strict less-than predicate
     */
    public Predicate isLessThan(
            String value
    ) {
        this.operator = Operator.LESS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Creates an inclusive upper-bound comparison for this field.
     *
     * @param value comparison value or WIQL macro
     * @return inclusive upper-bound predicate
     */
    public Predicate isLessThanOrEqualTo(
            String value
    ) {
        this.operator = Operator.LESS_OR_EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Creates a strict lower-bound comparison for this field.
     *
     * @param value comparison value or WIQL macro
     * @return strict greater-than predicate
     */
    public Predicate isGreaterThan(
            String value
    ) {
        this.operator = Operator.GREATER;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Creates an inclusive lower-bound comparison for this field.
     *
     * @param value comparison value or WIQL macro
     * @return inclusive lower-bound predicate
     */
    public Predicate isGreaterThanOrEqualTo(
            String value
    ) {
        this.operator = Operator.GREATER_OR_EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Creates an {@code IN} predicate with escaped, quoted values or unquoted
     * WIQL macros.
     *
     * @param value allowed values
     * @return membership predicate
     */
    public Predicate isIn(
            String... value
    ) {
        this.operator = Operator.IN;
        this.value = Stream.of(value).map(this::getValue)
                .collect(Collectors.joining(", ", "(", ")"));
        return new Predicate(this);
    }

    /**
     * Creates a membership predicate after normalizing classification paths.
     *
     * @param value allowed classification paths
     * @return path-membership predicate
     */
    public Predicate isIn(
            Path... value
    ) {
        return isIn(Stream.of(value)
                .map(Util::path)
                .toArray(String[]::new)
        );
    }

    /**
     * Creates a non-membership predicate for literal values or WIQL macros.
     *
     * @param value excluded literal values or macros
     * @return non-membership predicate
     */
    public Predicate isNotIn(
            String... value
    ) {
        this.operator = Operator.NOT_IN;
        this.value = Stream.of(value).map(this::getValue)
                .collect(Collectors.joining(", ", "(", ")"));
        return new Predicate(this);
    }

    /**
     * Creates a non-membership predicate after normalizing classification paths.
     *
     * @param value excluded classification paths
     * @return path non-membership predicate
     */
    public Predicate isNotIn(
            Path... value
    ) {
        return isNotIn(Stream.of(value)
                .map(Util::path)
                .toArray(String[]::new)
        );
    }

    /**
     * Tests whether an identity-valued field belongs to an Azure DevOps group.
     *
     * @param name group display name or descriptor
     * @return {@code IN GROUP} predicate
     */
    public Predicate isInGroup(
            String name
    ) {
        this.operator = Operator.IN_GROUP;
        this.value = getValue(name);
        return new Predicate(this);
    }

    /**
     * Excludes values belonging to the named Azure DevOps identity group.
     *
     * @param name excluded group name or descriptor
     * @return {@code NOT IN GROUP} predicate
     */
    public Predicate isNotInGroup(
            String name
    ) {
        this.operator = Operator.NOT_IN_GROUP;
        this.value = getValue(name);
        return new Predicate(this);
    }

    /**
     * Requires the field's text to contain the supplied fragment.
     *
     * @param value text that must occur in the field
     * @return {@code CONTAINS} predicate
     */
    public Predicate isContains(
            String value
    ) {
        this.operator = Operator.CONTAINS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Requires the field's text not to contain the supplied fragment.
     *
     * @param value text that must not occur in the field
     * @return negative containment predicate
     */
    public Predicate isNotContains(
            String value
    ) {
        this.operator = Operator.NOT_CONTAINS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Matches classification paths nested below the supplied ancestor.
     *
     * @param value ancestor classification path
     * @return hierarchical {@code UNDER} predicate
     */
    public Predicate isUnder(
            Path value
    ) {
        return isUnder(path(value));
    }

    /**
     * Tests whether a classification-path field is at or below an ancestor.
     *
     * @param value Azure path string
     * @return hierarchical {@code UNDER} predicate
     */
    public Predicate isUnder(
            String value
    ) {
        this.operator = Operator.UNDER;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Excludes classification paths nested below a normalized ancestor.
     *
     * @param value excluded ancestor path
     * @return hierarchical negative predicate
     */
    public Predicate isNotUnder(
            Path value
    ) {
        return isNotUnder(path(value));
    }

    /**
     * Excludes classification paths nested below the supplied Azure path string.
     *
     * @param value excluded Azure path string
     * @return {@code NOT UNDER} predicate
     */
    public Predicate isNotUnder(
            String value
    ) {
        this.operator = Operator.NOT_UNDER;
        this.value = getValue(value);
        return new Predicate(this);
    }

    /**
     * Tests whether the selected field has no value.
     *
     * @return {@code IS NULL} predicate
     */
    public Predicate isNull() {
        this.operator = Operator.IS;
        this.value = NULL;
        return new Predicate(this);
    }

    /**
     * Tests whether the selected field has a value.
     *
     * @return {@code IS NOT NULL} predicate
     */
    public Predicate isNotNull() {
        this.operator = Operator.IS_NOT;
        this.value = NULL;
        return new Predicate(this);
    }

    @Override
    public String toString() {
        return String.format(operator.toString(), field, value);
    }

    private String getValue(
            String value
    ) {
        if (value.startsWith("@")) {
            return value;
        } else {
            return String.format("'%s'", escapeEcmaScript(value));
        }
    }

    /**
     * Defines the values supported by Operator.
     */
    enum Operator {

        /** Matches work items whose field equals the supplied value. */
        EQUALS("%s = %s"),
        /** Matches work items whose field differs from the supplied value. */
        NOT_EQUALS("%s <> %s"),
        /** Matches field values strictly below the supplied value. */
        LESS("%s < %s"),
        /** Matches field values below or equal to the supplied value. */
        LESS_OR_EQUALS("%s <= %s"),
        /** Matches field values strictly above the supplied value. */
        GREATER("%s > %s"),
        /** Matches field values above or equal to the supplied value. */
        GREATER_OR_EQUALS("%s >= %s"),
        /** Matches a field value contained in a supplied value list. */
        IN("%s IN %s"),
        /** Excludes field values contained in a supplied value list. */
        NOT_IN("%s NOT IN %s"),
        /** Matches identity fields belonging to a supplied Azure DevOps group. */
        IN_GROUP("%s IN GROUP %s"),
        /** Excludes identity fields belonging to a supplied Azure DevOps group. */
        NOT_IN_GROUP("%s NOT IN GROUP %s"),
        /** Matches text fields containing the supplied fragment. */
        CONTAINS("%s CONTAINS %s"),
        /** Excludes text fields containing the supplied fragment. */
        NOT_CONTAINS("%s NOT CONTAINS %s"),
        /** Matches classification paths nested below the supplied path. */
        UNDER("%s UNDER %s"),
        /** Excludes classification paths nested below the supplied path. */
        NOT_UNDER("%s NOT UNDER %s"),
        /** Applies Azure WIQL's {@code IS} comparison, commonly to empty values. */
        IS("%s IS %s"),
        /** Applies Azure WIQL's negated {@code IS NOT} comparison. */
        IS_NOT("%s IS NOT %s");

        private final String string;

        Operator(
                String string
        ) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }

    }

}

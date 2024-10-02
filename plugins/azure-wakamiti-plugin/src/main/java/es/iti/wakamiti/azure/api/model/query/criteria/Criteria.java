/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query.criteria;


import es.iti.wakamiti.azure.api.model.query.Field;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.text.StringEscapeUtils.escapeEcmaScript;


public class Criteria implements Expression {

    private static final String NULL = "NULL";

    private final Field field;
    private Operator operator;
    private String value;

    private Criteria(Field field) {
        this.field = field;
    }

    public static Criteria field(String field) {
        return new Criteria(Field.of(field));
    }

    public static Criteria field(Field field) {
        return new Criteria(field);
    }

    public Predicate isEqualsTo(String value) {
        this.operator = Operator.EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isEqualsTo(Path value) {
        return isEqualsTo(path(value));
    }

    public Predicate isNotEqualsTo(String value) {
        this.operator = Operator.NOT_EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isNotEqualsTo(Path value) {
        return isNotEqualsTo(path(value));
    }

    public Predicate isLessThan(String value) {
        this.operator = Operator.LESS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isLessThanOrEqualTo(String value) {
        this.operator = Operator.LESS_OR_EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isGreaterThan(String value) {
        this.operator = Operator.GREATER;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isGreaterThanOrEqualTo(String value) {
        this.operator = Operator.GREATER_OR_EQUALS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isIn(String... value) {
        this.operator = Operator.IN;
        this.value = Stream.of(value).map(this::getValue)
                .collect(Collectors.joining(", ", "(", ")"));
        return new Predicate(this);
    }

    public Predicate isIn(Path... value) {
        return isIn(Stream.of(value)
                .map(this::path)
                .toArray(String[]::new)
        );
    }

    public Predicate isNotIn(String... value) {
        this.operator = Operator.NOT_IN;
        this.value = Stream.of(value).map(this::getValue)
                .collect(Collectors.joining(", ", "(", ")"));
        return new Predicate(this);
    }

    public Predicate isNotIn(Path... value) {
        return isNotIn(Stream.of(value)
                .map(this::path)
                .toArray(String[]::new)
        );
    }

    public Predicate isInGroup(String name) {
        this.operator = Operator.IN_GROUP;
        this.value = getValue(name);
        return new Predicate(this);
    }

    public Predicate isNotInGroup(String name) {
        this.operator = Operator.NOT_IN_GROUP;
        this.value = getValue(name);
        return new Predicate(this);
    }

    public Predicate isContains(String value) {
        this.operator = Operator.CONTAINS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isNotContains(String value) {
        this.operator = Operator.NOT_CONTAINS;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isUnder(Path value) {
        return isUnder(path(value));
    }

    public Predicate isUnder(String value) {
        this.operator = Operator.UNDER;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isNotUnder(Path value) {
        return isNotUnder(path(value));
    }

    public Predicate isNotUnder(String value) {
        this.operator = Operator.NOT_UNDER;
        this.value = getValue(value);
        return new Predicate(this);
    }

    public Predicate isNull() {
        this.operator = Operator.IS;
        this.value = NULL;
        return new Predicate(this);
    }

    public Predicate isNotNull() {
        this.operator = Operator.IS_NOT;
        this.value = NULL;
        return new Predicate(this);
    }

    @Override
    public String toString() {
        return String.format(operator.toString(), field, value);
    }

    private String getValue(String value) {
        if (value.startsWith("@")) {
            return value;
        } else {
            return String.format("'%s'", escapeEcmaScript(value));
        }
    }

    private String path(Path path) {
        return path.toString().replace("/", "\\");
    }

    enum Operator {

        EQUALS("%s = %s"),
        NOT_EQUALS("%s <> %s"),
        LESS("%s < %s"),
        LESS_OR_EQUALS("%s <= %s"),
        GREATER("%s > %s"),
        GREATER_OR_EQUALS("%s >= %s"),
        IN("%s IN %s"),
        NOT_IN("%s NOT IN %s"),
        IN_GROUP("%s IN GROUP %s"),
        NOT_IN_GROUP("%s NOT IN GROUP %s"),
        CONTAINS("%s CONTAINS %s"),
        NOT_CONTAINS("%s NOT CONTAINS %s"),
        UNDER("%s UNDER %s"),
        NOT_UNDER("%s NOT UNDER %s"),
        IS("%s IS %s"),
        IS_NOT("%s IS NOT %s"),;

        private final String string;

        Operator(String string) {
            this.string = string;
        }

        public String toString() {
            return string;
        }

    }

}

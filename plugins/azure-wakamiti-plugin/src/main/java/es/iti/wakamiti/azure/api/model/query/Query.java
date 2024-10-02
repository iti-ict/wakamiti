/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


import es.iti.wakamiti.azure.api.model.query.criteria.Expression;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;


/**
 *
 * @see <a href="https://learn.microsoft.com/en-us/azure/devops/boards/queries/wiql-syntax">wiql syntax</a>
 */
public abstract class Query {

    private final List<Field> fields = new LinkedList<>();
    private Expression criteria;
    private final List<OrderElement> orderElements = new LinkedList<>();
    private String asof;
    protected Mode mode;

    public abstract String getEntity();

    public Query select(List<?> fields) {
        this.fields.clear();
        this.fields.addAll(fields.stream()
                .map(f -> f instanceof Field ? (Field) f : Field.of(f.toString()))
                .collect(Collectors.toList()));
        return this;
    }

    public Query select(String... fields) {
        return select(List.of(fields));
    }

    public Query select(Field... fields) {
        return select(List.of(fields));
    }

    public Query select() {
        return select("System.Id");
    }

    public Query where(Expression criteria) {
        this.criteria = criteria;
        return this;
    }

    public Query orderBy(List<?> elements) {
        this.orderElements.clear();
        this.orderElements.addAll(elements.stream()
                        .map(e -> e instanceof OrderElement ? (OrderElement) e
                                : OrderElement.of(e.toString()))
                .collect(Collectors.toList()));
        return this;
    }

    public Query orderBy(OrderElement... elements) {
        return orderBy(List.of(elements));
    }

    public Query orderBy(Field... elements) {
        return orderBy(List.of(elements));
    }

    public Query orderBy(String... elements) {
        return orderBy(List.of(elements));
    }

    public Query asof(String date) {
        this.asof = date;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SELECT ").append(join(fields, ", "))
                .append(" FROM ").append(getEntity());
        if (criteria != null) {
            builder.append(" WHERE ").append(criteria);
        }
        if (!orderElements.isEmpty()) {
            builder.append(" ORDER BY ").append(join(orderElements, ", "));
        }
        if (mode != null) {
            builder.append(" MODE (").append(mode).append(")");
        }
        if (asof != null) {
            builder.append(" ASOF '").append(asof).append("'");
        }
        return builder.toString();
    }

    public enum Mode {
        MUST_CONTAIN("MustContain"),
        MAY_CONTAIN("MayContain"),
        DOES_NOT_CONTAIN("DoesNotContain"),
        RECURSIVE("Recursive");

        private final String mode;

        Mode(String mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return mode;
        }
    }
}

/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


import static org.apache.commons.lang3.StringUtils.join;

import java.util.LinkedList;
import java.util.List;

import es.iti.wakamiti.azure.api.model.query.criteria.Expression;


/**
 * Base class for building Azure DevOps work item queries using WIQL syntax.
 * Concrete queries provide the entity to query and may add selection,
 * filtering, ordering and temporal constraints.
 *
 * @see <a href="https://learn.microsoft.com/en-us/azure/devops/boards/queries/wiql-syntax">wiql syntax</a>
 */
public abstract class Query {

    private final List<Field> fields = new LinkedList<>();
    private Expression criteria;
    private final List<OrderElement> orderElements = new LinkedList<>();
    private String asof;
    protected Mode mode;

    /**
     * Returns the WIQL source entity selected by this query.
     *
     * @return for example {@code WorkItems} or {@code WorkItemLinks}
     */
    public abstract String getEntity();

    /**
     * Replaces the selected fields, accepting {@link Field} values or names.
     *
     * @param fields fields rendered after {@code SELECT}
     * @return this query
     */
    public Query select(
            List<?> fields
    ) {
        this.fields.clear();
        this.fields.addAll(fields.stream()
                .map(f -> f instanceof Field field ? field : Field.of(f.toString()))
                .toList());
        return this;
    }

    /**
     * Replaces the WIQL projection with fields created from their Azure names.
     *
     * @param fields Azure field names
     * @return this query
     */
    public Query select(
            String... fields
    ) {
        return select(List.of(fields));
    }

    /**
     * Replaces the WIQL projection with pre-normalized field expressions.
     *
     * @param fields normalized WIQL fields
     * @return this query
     */
    public Query select(
            Field... fields
    ) {
        return select(List.of(fields));
    }

    /**
     * Selects only {@code System.Id}, the minimal useful work-item projection.
     *
     * @return this query
     */
    public Query select() {
        return select("System.Id");
    }

    /**
     * Sets the expression rendered after {@code WHERE}.
     *
     * @param criteria WIQL criteria or compound predicate
     * @return this query
     */
    public Query where(
            Expression criteria
    ) {
        this.criteria = criteria;
        return this;
    }

    /**
     * Replaces ordering elements, accepting {@link OrderElement}, {@link Field}
     * or field-name values.
     *
     * @param elements ordering definitions
     * @return this query
     */
    public Query orderBy(
            List<?> elements
    ) {
        this.orderElements.clear();
        this.orderElements.addAll(elements.stream()
                .map(e -> e instanceof OrderElement orderElement
                        ? orderElement
                        : OrderElement.of(e.toString()))
                .toList());
        return this;
    }

    /**
     * Replaces the query's complete sequence of explicit sort expressions.
     *
     * @param elements explicit order elements
     * @return this query
     */
    public Query orderBy(
            OrderElement... elements
    ) {
        return orderBy(List.of(elements));
    }

    /**
     * Sorts by the supplied fields using Azure DevOps' default direction.
     *
     * @param elements fields using Azure's default sort direction
     * @return this query
     */
    public Query orderBy(
            Field... elements
    ) {
        return orderBy(List.of(elements));
    }

    /**
     * Sorts by fields resolved from their Azure DevOps reference names.
     *
     * @param elements field names using Azure's default direction
     * @return this query
     */
    public Query orderBy(
            String... elements
    ) {
        return orderBy(List.of(elements));
    }

    /**
     * Sets the historical instant used by the WIQL {@code ASOF} clause.
     *
     * @param date Azure-supported date/time literal
     * @return this query
     */
    public Query asof(
            String date
    ) {
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

    /**
     * Defines the values supported by Mode.
     */
    public enum Mode {

        /** Requires every selected link relationship to be present. */
        MUST_CONTAIN("MustContain"),
        /** Accepts work items when any selected link relationship is present. */
        MAY_CONTAIN("MayContain"),
        /** Excludes work items containing any selected link relationship. */
        DOES_NOT_CONTAIN("DoesNotContain"),
        /** Traverses matching links recursively instead of only direct relationships. */
        RECURSIVE("Recursive");

        private final String name;

        Mode(
                String name
        ) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

}

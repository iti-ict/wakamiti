/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query.criteria;


/**
 * Provides the Predicate functionality used by Wakamiti.
 */
public class Predicate implements Expression {

    private final Expression left;
    private Operator operator;
    private Expression right;

    /**
     * Wraps a completed criterion or expression for optional composition.
     *
     * @param criteria left-hand expression
     */
    public Predicate(
            Expression criteria
    ) {
        this.left = criteria;
    }

    /**
     * Combines this predicate and another criterion with a grouped logical AND.
     *
     * @param criteria expression that must also hold
     * @return grouped conjunction
     */
    public Predicate and(
            Expression criteria
    ) {
        this.operator = Operator.AND;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    /**
     * Combines this predicate and another criterion with a grouped logical OR.
     *
     * @param criteria alternative expression
     * @return grouped disjunction
     */
    public Predicate or(
            Expression criteria
    ) {
        this.operator = Operator.OR;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    /**
     * Combines historical conditions with WIQL {@code AND EVER}.
     *
     * @param criteria expression that must have held at some revision
     * @return grouped historical conjunction
     */
    public Predicate andEver(
            Expression criteria
    ) {
        this.operator = Operator.AND_EVER;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    /**
     * Combines historical conditions with WIQL {@code OR EVER}.
     *
     * @param criteria alternative that may have held at some revision
     * @return grouped historical disjunction
     */
    public Predicate orEver(
            Expression criteria
    ) {
        this.operator = Operator.OR_EVER;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    @Override
    public String toString() {
        if (operator == null) {
            return left.toString();
        }

        String r = this.right instanceof PredicateGroup ? "(" + this.right + ")" : this.right.toString();
        return String.format(operator.toString(), left, r);
    }

    /**
     * Defines the values supported by Operator.
     */
    enum Operator {

        /** Requires both adjacent work-item query criteria to match. */
        AND("%s AND %s"),
        /** Requires at least one of the adjacent work-item query criteria to match. */
        OR("%s OR %s"),
        /** Applies conjunction to each element of a link-query collection. */
        AND_EVER("%s AND EVER %s"),
        /** Applies disjunction to elements of a link-query collection. */
        OR_EVER("%s OR EVER %s");

        /** Field value. */
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

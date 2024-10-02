/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query.criteria;


public class Predicate implements Expression {

    private final Expression left;
    private Operator operator;
    private Expression right;

    public Predicate(Expression criteria) {
        this.left = criteria;
    }

    public Predicate and(Expression criteria) {
        this.operator = Operator.AND;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    public Predicate or(Expression criteria) {
        this.operator = Operator.OR;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    public Predicate andEver(Expression criteria) {
        this.operator = Operator.AND_EVER;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    public Predicate orEver(Expression criteria) {
        this.operator = Operator.OR_EVER;
        this.right = criteria;
        return new PredicateGroup(this);
    }

    @Override
    public String toString() {
        if (operator == null) {
            return left.toString();
        }

        String right = this.right instanceof PredicateGroup ? "(" + this.right + ")" : this.right.toString();
        return String.format(operator.toString(), left, right);
    }

    enum Operator {
        AND("%s AND %s"),
        OR("%s OR %s"),
        AND_EVER("%s AND EVER %s"),
        OR_EVER("%s OR EVER %s")
        ;

        private final String string;

        Operator(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

}

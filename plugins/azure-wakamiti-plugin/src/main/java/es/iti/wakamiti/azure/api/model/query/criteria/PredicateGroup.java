/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query.criteria;


/**
 * Provides the Predicate Group functionality used by Wakamiti.
 */
public class PredicateGroup extends Predicate {

    /**
     * Creates a grouping boundary for a compound WIQL expression.
     *
     * @param criteria expression whose subsequent right-hand group is rendered
     *                 between parentheses
     */
    public PredicateGroup(
            Expression criteria
    ) {
        super(criteria);
    }

}

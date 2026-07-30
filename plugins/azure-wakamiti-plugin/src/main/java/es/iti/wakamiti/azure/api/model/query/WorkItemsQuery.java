/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


/**
 * Provides the Work Items Query functionality used by Wakamiti.
 */
public class WorkItemsQuery extends Query {

    /** WIQL entity used for flat work-item queries. */
    public static final String ENTITY = "WorkItems";

    @Override
    public String getEntity() {
        return ENTITY;
    }

}

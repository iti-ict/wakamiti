/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


/**
 * Provides the Work Item Links Query functionality used by Wakamiti.
 */
public class WorkItemLinksQuery extends Query {

    /** WIQL entity used for relation and hierarchy queries. */
    public static final String ENTITY = "WorkItemLinks";

    @Override
    public String getEntity() {
        return ENTITY;
    }

    /**
     * Sets how source and target work items must participate in returned links.
     *
     * @param mode WIQL link-query mode, such as recursive hierarchy traversal
     * @return this query
     */
    public WorkItemLinksQuery mode(
            Mode mode
    ) {
        this.mode = mode;
        return this;
    }

}

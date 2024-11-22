/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


public class WorkItemLinksQuery extends Query {

    public static final String ENTITY = "WorkItemLinks";

    @Override
    public String getEntity() {
        return ENTITY;
    }

    public WorkItemLinksQuery mode(Mode mode) {
        this.mode = mode;
        return this;
    }

}

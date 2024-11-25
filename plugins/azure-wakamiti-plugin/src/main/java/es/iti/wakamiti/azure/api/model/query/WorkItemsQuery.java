/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


public class WorkItemsQuery extends Query {

    public static final String ENTITY = "WorkItems";

    @Override
    public String getEntity() {
        return ENTITY;
    }
}

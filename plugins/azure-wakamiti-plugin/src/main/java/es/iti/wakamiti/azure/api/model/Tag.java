/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


public class Tag extends BaseModel {

    private final String name;

    public Tag(String name) {
        this.name = name;
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{name};
    }

    @Override
    public String toString() {
        return name;
    }

}
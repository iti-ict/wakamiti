/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.azure.AzureSynchronizer;


public class FeatureMapper extends Mapper {

    public FeatureMapper(String suiteBase) {
        super(suiteBase);
    }

    public String type() {
        return AzureSynchronizer.GHERKIN_TYPE_FEATURE;
    }


}

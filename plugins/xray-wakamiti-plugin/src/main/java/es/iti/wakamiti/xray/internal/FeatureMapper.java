/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;


import es.iti.wakamiti.xray.XRaySynchronizer;


/**
 * Maps Feature data between its external and internal representations.
 */
public class FeatureMapper extends Mapper {

    /**
     * Creates a mapper that produces one Xray test for each Wakamiti feature.
     *
     * @param suiteBase base directory used to relativize generated test-set paths
     */
    public FeatureMapper(
            String suiteBase
    ) {
        super(suiteBase);
    }

    /**
     * Identifies features as the Gherkin level consumed by this mapper.
     *
     * @return the feature Gherkin type identifier
     */
    @Override
    public String type() {
        return XRaySynchronizer.GHERKIN_TYPE_FEATURE;
    }

}

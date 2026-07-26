/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;

import es.iti.wakamiti.xray.XRaySynchronizer;


public class FeatureMapper extends Mapper {

    public FeatureMapper(String suiteBase) {
        super(suiteBase);
    }

    public String type() {
        return XRaySynchronizer.GHERKIN_TYPE_FEATURE;
    }


}

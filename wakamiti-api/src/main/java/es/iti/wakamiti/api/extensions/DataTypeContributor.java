/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import java.util.List;

import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.WakamitiDataType;


/**
 * This interface defines a contract for contributors that provide additional
 * data types to the Wakamiti system.
 *
 */
@ExtensionPoint
public interface DataTypeContributor extends Contributor {

    /**
     * Contributes a list of Wakamiti data types.
     *
     * @return The list of contributed Wakamiti data types.
     */
    List<WakamitiDataType<?>> contributeTypes();

}

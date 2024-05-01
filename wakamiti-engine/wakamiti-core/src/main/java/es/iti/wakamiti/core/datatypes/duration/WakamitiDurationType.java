/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.duration;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.extensions.DataTypeContributor;
import es.iti.wakamiti.core.datatypes.WakamitiDataTypeBase;

import java.time.Duration;
import java.util.List;


/**
 * The {@code WakamitiDurationType} class contributes the duration data type to Wakamiti.
 * It defines the behavior for handling duration data and provides methods for contributing
 * the duration data type to the Wakamiti system.
 *
 * @author María Galbis Calomarde - mgalbis@iti.es
 */
@Extension(provider = "es.iti.wakamiti", name = "duration-types", version = "2.6")
public class WakamitiDurationType implements DataTypeContributor {

    private static final DurationProvider PROVIDER = new DurationProvider();

    /**
     * Contributes the duration data type to Wakamiti.
     *
     * @return A list containing the duration data type.
     */
    @Override
    public List<WakamitiDataType<?>> contributeTypes() {
        return List.of(new WakamitiDataTypeBase<>(
                "duration",
                Duration.class,
                locale -> {
                    String[] expressions = PROVIDER.regex(locale).toArray(new String[0]);
                    return "(" + String.join("|", expressions) + ")";
                },
                DurationProvider::getAllExpressions,
                locale -> expression -> PROVIDER.durationFromExpression(locale, expression).orElse(null)
        ) {
        });
    }

}

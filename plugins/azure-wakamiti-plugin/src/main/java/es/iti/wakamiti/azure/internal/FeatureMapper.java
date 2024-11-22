/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.azure.AzureSynchronizer;


/**
 * A specific implementation of {@link Mapper} for handling
 * Gherkin features.
 * <p>
 * This class maps Gherkin feature nodes to Azure test
 * artifacts, such as test cases and suites.
 * </p>
 */
public class FeatureMapper extends Mapper {

    /**
     * Constructs a FeatureMapper with a specified base directory for test suites.
     *
     * @param suiteBase the base directory for mapping test suites.
     */
    public FeatureMapper(String suiteBase) {
        super(suiteBase);
    }

    /**
     * Returns the type of this mapper as a string.
     *
     * @return the type of the mapper, which is {@code "feature"}.
     */
    public String type() {
        return AzureSynchronizer.GHERKIN_TYPE_FEATURE;
    }

}

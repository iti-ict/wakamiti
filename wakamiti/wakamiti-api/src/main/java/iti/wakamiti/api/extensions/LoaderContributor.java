/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.api.extensions;

import iti.commons.jext.ExtensionPoint;

import java.util.List;
import java.util.stream.Stream;

/**
 * This interface allows adding extra class loaders from sources other
 * than java.
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@ExtensionPoint
public interface LoaderContributor extends Contributor {

    /**
     * This implementation must look for the sources in the given paths
     * and load them with the corresponding loader.
     *
     * @param discoveryPaths The paths where search the sources to load
     * @return The loaded classes
     */
    Stream<? extends Class<?>> load(List<String> discoveryPaths);
}

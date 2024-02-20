/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.Extension;


/**
 * Base interface for all Wakamiti extensions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public interface Contributor {

    /**
     * Provides information about the contributor, including provider, name, and version.
     *
     * @return A formatted string containing provider, name, and version information.
     */
    default String info() {
        Extension extensionData = extensionMetadata();
        if (extensionData != null) {
            return String.format(
                    "%s:%s:%s",
                    extensionData.provider(),
                    extensionData.name(),
                    version()
            );
        } else {
            return getClass().getCanonicalName();
        }
    }

    /**
     * Retrieves the metadata defined by the {@link Extension} annotation for the contributor.
     *
     * @return The extension metadata.
     */
    default Extension extensionMetadata() {
        return getClass().getAnnotation(Extension.class);
    }

    /**
     * Retrieves the version information of the contributor.
     *
     * @return The version of the contributor.
     */
    default String version() {
        return getClass().getPackage().getImplementationVersion();
    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.api.extensions;

import es.iti.commons.jext.Extension;

import java.util.Optional;

/** Base interface for all Wakamiti extensions */
public interface Contributor {

    default String info() {
        Extension extensionData = this.getClass().getAnnotation(Extension.class);
        if (extensionData != null) {
            String info = String.format("%s:%s",
                    extensionData.provider(),
                    extensionData.name());
            return Optional.ofNullable(version())
                    .map(version -> String.format("%s:%s", info, version.replaceAll("\\.\\d+$", "")))
                    .orElse(info);
        } else {
            return getClass().getCanonicalName();
        }
    }


    default Extension extensionMetadata() {
        return this.getClass().getAnnotation(Extension.class);
    }

    default String version() {
        return getClass().getPackage().getImplementationVersion();
    }

}
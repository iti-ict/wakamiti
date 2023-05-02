/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;

import iti.commons.jext.Extension;


/** Base interface for all Kukumo extensions */
public interface Contributor {

    default String info() {
        Extension extensionData = this.getClass().getAnnotation(Extension.class);
        if (extensionData != null) {
            return String.format(
                    "%s:%s:%s",
                    extensionData.provider(),
                    extensionData.name(),
                    extensionData.version()
            );
        } else {
            return getClass().getCanonicalName();
        }
    }


    default Extension extensionMetadata() {
        return this.getClass().getAnnotation(Extension.class);
    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


@ExtensionPoint(version = "2.5")
public interface MyExtensionPointV2_5 {

    default String value() {
        return getClass().getSimpleName();
    }

}
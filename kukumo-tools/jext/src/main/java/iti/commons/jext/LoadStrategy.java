/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.jext;


/** The different strategies that can be used each time an extension is * requested using the {@link ExtensionManager}. */
public enum LoadStrategy {

    /** Keep a single instance */
    SINGLETON,

    /** Creates a new instance each time */
    FRESH,

    /** The behaviour is decided by the underline implementation */
    UNDEFINED
}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.commons.jext.test;


import es.iti.commons.jext.Extension;

@Extension(provider = "test", name = "Singleton", version = "1.0")
public class MyExtensionSingleton implements ExtensionPointSingleton {

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.jext;


/** This interface allows third-party contributors to implement custom * mechanisms to retrieve extension instances, instead of using the Java * {@link java.util.ServiceLoader} approach. * <p> * This is specially suited for IoC injection frameworks that may manage * object instances in a wide range of different ways. * </p> */
public interface ExtensionLoader {

    <T> Iterable<T> load(Class<T> type, ClassLoader loader);
}
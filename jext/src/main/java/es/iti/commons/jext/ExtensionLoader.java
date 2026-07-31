/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


/**
 * This interface allows third-party contributors to implement custom
 * mechanisms to retrieve extension instances, instead of using the Java
 * {@link java.util.ServiceLoader} approach.
 * <p>
 * This is specially suited for IoC injection frameworks that may manage
 * object instances in a wide range of different ways.
 * </p>
 */
public interface ExtensionLoader {

    /**
     * Discovers the available implementations of an extension point.
     * <p>
     * Implementations may instantiate providers directly or return objects
     * managed by an external container. The returned {@link Iterable} may be
     * evaluated lazily, so callers must not assume that discovery or
     * instantiation has completed when this method returns.
     * </p>
     *
     * @param type   the extension point contract that every returned instance
     *               must implement
     * @param loader the class loader that defines the discovery scope; custom
     *               loaders may use it to locate provider metadata or classes
     * @param <T>    the extension point type
     * @return the discovered implementations, or an empty iterable when no
     * compatible provider is available
     */
    <T> Iterable<T> load(
            Class<T> type,
            ClassLoader loader
    );

}

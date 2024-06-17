/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.imconfig;


/**
 * A convenient interface for objects that can apply a configuration to themselves.
 * <p>
 * The main difference with {@link Configurer} is that, while a `Configurer` is a third
 * object that applies a configuration to others, a `Configurable` applies the configuration
 * to itself.
 */
public interface Configurable {

    /**
     * Apply the given configuration
     */
    void configure(Configuration configuration);
}

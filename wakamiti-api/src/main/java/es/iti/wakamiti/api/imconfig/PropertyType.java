/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


/**
 * This class determines what are the accepted values for a specific
 * property.
 *
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
public interface PropertyType {

    /**
     * The type name (for describing purposes). Do not use this as an id, the same name
     * can be shared among several instances with different behaviors.
     * @return The describing name of the type
     */
    String name();



    /**
     * @return A description of the expected values
     */
    String hint();


    /**
     * Check if the current type accepts the given value
     * @param value The input value
     * @return whether the given value is valid
     */
    boolean accepts(String value);


}
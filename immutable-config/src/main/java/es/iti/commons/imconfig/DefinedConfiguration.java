/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.imconfig;


import java.util.*;


public interface DefinedConfiguration extends Configuration {


    /**
     * Check whether the current value for the given property is valid according its definition.
     * If the property definition is multivalued, it will return a different validation for each value
     * @param key The property key
     * @return The validation message, or empty if the value(s) is(are) valid
     */
    List<String> validations(String key);


    /**
     * Retrieve the property definition for a given property
     */
    Optional<PropertyDefinition> getDefinition(String key);



}

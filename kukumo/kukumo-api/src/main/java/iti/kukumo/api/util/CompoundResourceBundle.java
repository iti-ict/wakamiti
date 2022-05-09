/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.util;


import java.util.*;



public class CompoundResourceBundle extends ResourceBundle {

    private final List<ResourceBundle> resourceBundles;


    public CompoundResourceBundle(List<ResourceBundle> resourceBundles) {
        this.resourceBundles = new ArrayList<>(resourceBundles);
    }


    @Override
    protected Object handleGetObject(String key) {
        for (ResourceBundle resourceBundle : resourceBundles) {
            Object value = resourceBundle.getObject(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }


    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<>();
        for (ResourceBundle resourceBundle : resourceBundles) {
            keys.addAll(resourceBundle.keySet());
        }
        return Collections.enumeration(keys);
    }

}
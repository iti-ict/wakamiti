/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import java.util.*;


/**
 * A compound resource bundle that combines multiple resource bundles.
 *
 * <p>This class extends {@code ResourceBundle} and allows the aggregation of multiple
 * resource bundles. When looking up a resource, it iterates through its internal list
 * of resource bundles to find the first occurrence of the specified key.</p>
 *
 * <p>Instances of this class are created by providing a list of resource bundles to
 * be aggregated.</p>
 *
 * <p>The implementation overrides the {@link #handleGetObject(String)} method to
 * search for the specified key in the list of resource bundles. It also overrides
 * the {@link #getKeys()} method to collect all keys from the aggregated resource
 * bundles without duplicates.</p>
 *
 * <p>Instances of this class are typically used to combine resource bundles from
 * different sources, providing a unified interface for resource key lookup.</p>
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class CompoundResourceBundle extends ResourceBundle {

    private final List<ResourceBundle> resourceBundles;

    public CompoundResourceBundle(List<ResourceBundle> resourceBundles) {
        this.resourceBundles = new ArrayList<>(resourceBundles);
    }

    /**
     * Looks up the value of the specified key in the aggregated resource bundles.
     *
     * <p>This method iterates through the list of resource bundles and returns the
     * value for the first occurrence of the specified key. If the key is not found
     * in any of the resource bundles, it returns {@code null}.</p>
     *
     * @param key The key for which to retrieve the value.
     * @return The value associated with the specified key, or {@code null} if not found.
     */
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

    /**
     * Retrieves an enumeration of all keys from the aggregated resource bundles.
     *
     * <p>This method collects all keys from the aggregated resource bundles without
     * duplicates and returns an enumeration of those keys.</p>
     *
     * @return An enumeration of all keys from the aggregated resource bundles.
     */
    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<>();
        for (ResourceBundle resourceBundle : resourceBundles) {
            keys.addAll(resourceBundle.keySet());
        }
        return Collections.enumeration(keys);
    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Registry for Wakamiti data types, allowing retrieval and
 * querying of data types.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiDataTypeRegistry {

    private final Map<String, WakamitiDataType<?>> types;

    public WakamitiDataTypeRegistry(Map<String, WakamitiDataType<?>> types) {
        this.types = types;
    }

    /**
     * Gets the Wakamiti data type associated with the specified
     * name.
     *
     * @param name The name of the data type to retrieve.
     * @return The Wakamiti data type corresponding to the
     * specified name.
     */
    public WakamitiDataType<?> getType(String name) {
        return types.get(name);
    }

    /**
     * Gets a collection of all registered Wakamiti data types.
     *
     * @return A collection containing all registered Wakamiti data
     * types.
     */
    public Collection<WakamitiDataType<?>> getTypes() {
        return types.values();
    }

    /**
     * Finds and return a stream of Wakamiti data types associated
     * with the specified Java type.
     *
     * @param javaType The Java type for which to find associated
     *                 Wakamiti data types.
     * @return A stream of Wakamiti data types associated with the
     * specified Java type.
     */
    public Stream<WakamitiDataType<?>> findTypesForJavaType(Class<?> javaType) {
        return this.types.values().stream()
                .filter(type -> type.getJavaType().equals(javaType));
    }

    /**
     * Gets a stream of all registered Wakamiti data type names.
     *
     * @return A stream of names of all registered Wakamiti data
     * types.
     */
    public Stream<String> allTypeNames() {
        return this.types.keySet().stream();
    }

}
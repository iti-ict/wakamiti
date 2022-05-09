/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;


public class KukumoDataTypeRegistry {

    private final Map<String, KukumoDataType<?>> types;


    public KukumoDataTypeRegistry(Map<String, KukumoDataType<?>> types) {
        this.types = types;
    }


    public KukumoDataType<?> getType(String name) {
        return types.get(name);
    }


    public Collection<KukumoDataType<?>> getTypes() {
        return types.values();
    }


    public Stream<KukumoDataType<?>> findTypesForJavaType(Class<?> javaType) {
        return this.types.values().stream()
            .filter(type -> type.getJavaType().equals(javaType));
    }


    public Stream<String> allTypeNames() {
        return this.types.keySet().stream();
    }

}
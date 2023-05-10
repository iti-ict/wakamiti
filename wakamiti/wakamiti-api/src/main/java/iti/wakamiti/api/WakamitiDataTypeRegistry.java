/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api;


import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;


public class WakamitiDataTypeRegistry {

    private final Map<String, WakamitiDataType<?>> types;


    public WakamitiDataTypeRegistry(Map<String, WakamitiDataType<?>> types) {
        this.types = types;
    }


    public WakamitiDataType<?> getType(String name) {
        return types.get(name);
    }


    public Collection<WakamitiDataType<?>> getTypes() {
        return types.values();
    }


    public Stream<WakamitiDataType<?>> findTypesForJavaType(Class<?> javaType) {
        return this.types.values().stream()
            .filter(type -> type.getJavaType().equals(javaType));
    }


    public Stream<String> allTypeNames() {
        return this.types.keySet().stream();
    }

}
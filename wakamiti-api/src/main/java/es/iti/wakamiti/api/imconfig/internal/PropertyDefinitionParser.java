/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.internal;


import es.iti.wakamiti.api.imconfig.*;
import es.iti.wakamiti.api.imconfig.types.internal.PropertyTypeFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class PropertyDefinitionParser {

    private final Yaml yaml = new Yaml();
    private final PropertyTypeFactory typeFactory = new PropertyTypeFactory();


    @SuppressWarnings("unchecked")
    Collection<PropertyDefinition> read (Reader reader) {
        Map<String,Map<String,Object>> map = yaml.loadAs(reader, HashMap.class);
        return map.entrySet().stream().map(this::parseDefinition).collect(Collectors.toList());
    }


    @SuppressWarnings("unchecked")
    Collection<PropertyDefinition> read (InputStream inputStream) {
        Map<String,Map<String,Object>> map = yaml.loadAs(inputStream, HashMap.class);
        return map.entrySet().stream().map(this::parseDefinition).collect(Collectors.toList());
    }



    @SuppressWarnings("unchecked")
    private PropertyDefinition parseDefinition(Entry<String, Map<String, Object>> entry) {
        try {
            var definition = entry.getValue();
            return PropertyDefinition.builder()
                .property(entry.getKey())
                .description((String) definition.get("description"))
                .required((Boolean)definition.get("required"))
                .defaultValue(toString(definition.get("defaultValue")))
                .propertyType(typeFactory.create(
                    (String)definition.get("type"),
                    (Map<String, Object>) definition.get("constraints")
                ))
                .build();
        } catch (RuntimeException e) {
            throw new ConfigurationException(
                "Bad configuration of property '"+entry.getKey()+"' : "+e.getMessage(), e
            );
        }
    }


    private String toString(Object object) {
        return object == null ? null : object.toString();
    }


}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class ConfigurationFactoryDefinitionsTest {


    private final ConfigurationFactory factory = ConfigurationFactory.instance();
    private final Path definitionPath = Path.of("src", "test", "resources", "definition.yaml");


    @Test
    public void testBuildEmptyConfigurationWithDefinitionFromURI() {
        var conf = factory.accordingDefinitionsFromURI(definitionPath.toUri());
        assertConfiguration(conf);
    }

    @Test
    public void testBuildEmptyConfigurationWithDefinitionFromPath() {
        var conf = factory.accordingDefinitionsFromPath(definitionPath);
        assertConfiguration(conf);
    }


    @Test
    public void testAttachDefinitionFromURI() {
        var conf = factory.empty().accordingDefinitionsFromURI(definitionPath.toUri());
        assertConfiguration(conf);
    }


    @Test
    public void testAttachDefinitionFromPath() {
        var conf = factory.empty().accordingDefinitionsFromPath(definitionPath);
        assertConfiguration(conf);
    }


    @Test
    public void testConfigurationValidation() {
        var conf = factory
            .fromPairs("defined.property.min-max-number", "6")
            .accordingDefinitionsFromPath(definitionPath);
        assertThat(conf.validations("defined.property.min-max-number"))
        .contains("Invalid value '6', expected: Integer number between 2 and 3");
    }


    private void assertConfiguration(Configuration conf) {
        assertThat(conf.getDefinitions()).hasSize(6);
        assertThat(conf.getDefinition("defined.property.required")).isNotEmpty();
        assertThat(conf.getDefinition("defined.property.with-default-value")).isNotEmpty();
        assertThat(conf.getDefinition("defined.property.regex-text")).isNotEmpty();
        assertThat(conf.getDefinition("defined.property.min-max-number")).isNotEmpty();
        assertThat(conf.getDefinition("defined.property.enumeration")).isNotEmpty();
        assertThat(conf.getDefinition("defined.property.boolean")).isNotEmpty();
        assertThat(conf.getDefinition("undefined.property")).isEmpty();
        assertThat(conf.get("defined.property.regex-text", String.class)).isEmpty();
        assertThat(conf.get("defined.property.with-default-value", Integer.class)).hasValue(5);
    }

}

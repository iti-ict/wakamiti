/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;


import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("java:S1872")
public class GroovyLoaderContributorTest {

    private final GroovyLoaderContributor loader = new GroovyLoaderContributor();

    @Test
    public void testLoadWhenThereAreFiles() throws URISyntaxException {
        URI steps = Objects.requireNonNull(this.getClass().getClassLoader().getResource("steps")).toURI();
        Class<?>[] result = loader.load(List.of(new File(steps).getAbsolutePath())).toArray(Class[]::new);
        assertThat(result)
                .isNotEmpty()
                .hasSize(2)
                .allMatch(it -> it.getPackage().getName().equals("steps"))
                .anyMatch(it -> it.getSimpleName().equals("CustomSteps"))
                .anyMatch(it -> it.getSimpleName().equals("CustomSteps2"));
    }

    @Test
    public void testLoadWhenNotFiles() throws URISyntaxException {
        URI steps = Objects.requireNonNull(this.getClass().getClassLoader().getResource("empty")).toURI();
        Class<?>[] result = loader.load(List.of(new File(steps).getAbsolutePath())).toArray(Class[]::new);
        assertThat(result).isEmpty();
    }

    @Test
    public void testLoadWhenEmpty() {
        Class<?>[] result = loader.load(List.of()).toArray(Class[]::new);
        assertThat(result).isEmpty();
    }

    @Test
    public void testLoadWhenErrorFile() throws URISyntaxException {
        URI steps = Objects.requireNonNull(this.getClass().getClassLoader().getResource("error")).toURI();
        Class<?>[] result = loader.load(List.of(new File(steps).getAbsolutePath())).toArray(Class[]::new);
        assertThat(result).isEmpty();
    }

}

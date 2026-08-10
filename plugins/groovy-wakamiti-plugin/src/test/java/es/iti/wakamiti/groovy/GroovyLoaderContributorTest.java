/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.StepContributor;


public class GroovyLoaderContributorTest {

    private final GroovyLoaderContributor loader = new GroovyLoaderContributor();

    @Test
    public void testLoadWhenThereAreFiles() throws URISyntaxException {
        URI steps = Objects.requireNonNull(this.getClass().getClassLoader().getResource("steps")).toURI();
        Class<?>[] result = loader.load(List.of(new File(steps).getAbsolutePath())).toArray(Class[]::new);
        assertThat(result)
                .isNotEmpty()
                .hasSize(2)
                .anyMatch(StepContributor.class::isAssignableFrom);
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
    public void testLoadWhenErrorFileAndFailOnErrorEnabled() throws URISyntaxException {
        URI steps = Objects.requireNonNull(this.getClass().getClassLoader().getResource("error")).toURI();
        assertThatThrownBy(() -> loader.load(List.of(new File(steps).getAbsolutePath())))
                .isInstanceOf(WakamitiException.class);
    }

    @Test
    public void testLoadWhenErrorFileAndFailOnErrorDisabled() throws URISyntaxException {
        loader.setFailOnError(false);
        URI steps = Objects.requireNonNull(this.getClass().getClassLoader().getResource("error")).toURI();
        Class<?>[] result = loader.load(List.of(new File(steps).getAbsolutePath())).toArray(Class[]::new);
        assertThat(result).isEmpty();
    }

}

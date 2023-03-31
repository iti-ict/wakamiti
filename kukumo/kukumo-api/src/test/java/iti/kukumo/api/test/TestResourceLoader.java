/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.test;


import iti.kukumo.api.*;
import iti.kukumo.api.extensions.*;
import iti.kukumo.api.util.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TestResourceLoader {

    @Test
    public void testDiscoverFromClasspath() throws IOException {

        Predicate<String> txtFilter = filename -> filename.endsWith(".txt");

        List<Resource> discoveredResources = new ResourceLoader()
            .discoverResources("classpath:discovery", txtFilter, IOUtils::toString)
            .stream().sorted(Comparator.comparing(Resource::absolutePath))
            .collect(Collectors.toList());

        assertEquals(3,discoveredResources.size());

        assertFile(discoveredResources.get(0),"file1.txt");
        assertFile(discoveredResources.get(1),"file2.txt");
        assertFile(discoveredResources.get(2),"subfolder/file4.txt");


        for (Resource discoveredResource : discoveredResources) {
            System.out.println(
                discoveredResource.relativePath() + " || " + discoveredResource.absolutePath()
            );
            System.out.println("------------------");
            String content = (String) discoveredResource.content();
            System.out.println(content);

            System.out.println("------------------");
        }

    }
    private void assertFile(Resource<?> resource, String relativePath) {
        assertEquals(resource.relativePath(), new File(relativePath).getPath());
        assertTrue(resource.absolutePath().endsWith(new File("discovery/" + relativePath).getPath()));

    }


    @Test
    public void testPremain() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        ClasspathAgent.premain("", instrumentation);

        assertNotNull(ClasspathAgent.instrumentation);
    }

    @Test
    public void agentmain() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        ClasspathAgent.agentmain("", instrumentation);

        assertNotNull(ClasspathAgent.instrumentation);
    }

    @Test
    public void testAppendJarFileWhenInstrumentationIsNotNull() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        ClasspathAgent.instrumentation = instrumentation;

        JarFile jarFile = mock(JarFile.class);

        ClasspathAgent.appendJarFile(jarFile);
    }


    @Test
    public void testResource() {
        String absolutePath = "/path/to/resource";
        String relativePath = "resource";
        String content = "Content";
        Resource<String> resource = new Resource<>(absolutePath, relativePath, content);

        assertEquals(relativePath, resource.relativePath());
        assertEquals(absolutePath, resource.absolutePath());
        assertEquals(content, resource.content());
    }

    @Test
    public void testAllContributors() {
        KukumoContributors kukumoContributors = new KukumoContributors();
        Map<Class<?>, List<Contributor>> allContributors = kukumoContributors.allContributors();
        assertNotNull(allContributors);
        assertTrue(allContributors.containsKey(ConfigContributor.class));
        assertTrue(allContributors.containsKey(DataTypeContributor.class));
        assertTrue(allContributors.containsKey(EventObserver.class));
        assertTrue(allContributors.containsKey(PlanBuilder.class));
        assertTrue(allContributors.containsKey(PlanTransformer.class));
        assertTrue(allContributors.containsKey(Reporter.class));
        assertTrue(allContributors.containsKey(ResourceType.class));
        assertTrue(allContributors.containsKey(StepContributor.class));
    }


}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ResourceLoaderTest {

    @Test
    public void testDiscoverFromClasspath() {

        Predicate<String> txtFilter = filename -> filename.endsWith(".txt");

        List<Resource<?>> discoveredResources = new ResourceLoader()
                .discoverResources("classpath:discovery", txtFilter, IOUtils::toString)
                .stream().sorted(Comparator.comparing(Resource::absolutePath))
                .collect(Collectors.toList());

        assertEquals(3, discoveredResources.size());

        assertFile(discoveredResources.get(0), "file1.txt");
        assertFile(discoveredResources.get(1), "file2.txt");
        assertFile(discoveredResources.get(2), "subfolder/file4.txt");

        assertEquals(discoveredResources.get(0).content().toString(), "Content of File 1");
        assertEquals(discoveredResources.get(1).content().toString(), "Content of File 2");
        assertEquals(discoveredResources.get(2).content().toString(), "Content of File 4");
    }

    private void assertFile(Resource<?> resource, String relativePath) {
        assertEquals(resource.relativePath(), new File(relativePath).getPath());
        assertTrue(resource.absolutePath().endsWith(new File("discovery/" + relativePath).getPath()));
    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.test;


import iti.kukumo.api.Resource;
import iti.kukumo.api.util.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;


public class TestResourceLoader {

    @Test
    public void testDiscoverFromClasspath() throws IOException {

        Predicate<String> txtFilter = filename -> filename.endsWith(".txt");

        List<Resource<?>> discoveredResources = new ResourceLoader()
            .discoverResources("classpath:discovery", txtFilter, IOUtils::toString);
        for (Resource<?> discoveredResource : discoveredResources) {
            System.out.println(
                discoveredResource.relativePath() + " || " + discoveredResource.absolutePath()
            );
            System.out.println("------------------");
            String content = (String) discoveredResource.content();
            System.out.println(content);
            System.out.println("------------------");
        }

    }

}
package iti.kukumo.test.util;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.Resource;

/**
 * @author ITI
 *         Created by ITI on 8/01/19
 */
public class TestResourceLoader {


    @Test
    public void testDiscoverFromClasspath() throws IOException {

        Predicate<String> txtFilter = filename -> filename.endsWith(".txt");


        List<Resource<?>> discoveredResources = Kukumo.getResourceLoader()
                .discoverResources("classpath:discovery", txtFilter, IOUtils::toString);
        for (Resource<?> discoveredResource : discoveredResources) {
            System.out.println(discoveredResource.relativePath()+" || "+discoveredResource.absolutePath());
            System.out.println("------------------");
            String content = (String) discoveredResource.content();
            System.out.println(content);
            System.out.println("------------------");
        }

    }




}

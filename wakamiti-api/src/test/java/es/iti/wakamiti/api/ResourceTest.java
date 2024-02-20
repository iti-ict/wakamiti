/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ResourceTest {

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

}

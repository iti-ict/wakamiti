/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class TestExtensionManager {

    private final ExtensionManager extensionManager = new ExtensionManager();


    @Test
    public void testGetExtension() {
        // MyExtensionV2_5 has greater priority than MyExtensionV2_6
        assertThat(
                extensionManager.getExtension(MyExtensionPointV2_5.class)
        ).containsInstanceOf(MyExtensionV2_5.class);
    }


    @Test
    public void testGetExtensions() {
        // MyExtensionV2_5 has greater priority than MyExtensionV2_6
        List<MyExtensionPointV2_5> extensions = extensionManager
                .getExtensions(MyExtensionPointV2_5.class).collect(Collectors.toList());
        assertThat(extensions).hasSize(2);
        assertThat(extensions.get(0)).isInstanceOf(MyExtensionV2_5.class);
        assertThat(extensions.get(1)).isInstanceOf(MyExtensionV2_6.class);
    }


    @Test
    public void testGetExtensionsSatisfying() {
        List<MyExtensionPointV2_5> extensions = extensionManager.getExtensionsThatSatisfy(
                MyExtensionPointV2_5.class,
                extension -> extension.value().endsWith("_6")
        ).collect(Collectors.toList());
        assertThat(extensions).hasSize(1);
        assertThat(extensions.get(0)).isInstanceOf(MyExtensionV2_6.class);
    }


    @Test
    public void testGetExtensionsSatisfyingMetadata() {
        List<MyExtensionPointV2_5> extensions = extensionManager.getExtensionsThatSatisfyMetadata(
                MyExtensionPointV2_5.class,
                extension -> extension.extensionPointVersion().equals("2.6")
        ).collect(Collectors.toList());
        assertThat(extensions).hasSize(1);
        assertThat(extensions.get(0)).isInstanceOf(MyExtensionV2_6.class);
    }


    @Test
    public void testExtensionPointSingleton() {
        ExtensionPointSingleton extension1 = extensionManager
                .getExtension(ExtensionPointSingleton.class).get();
        ExtensionPointSingleton extension2 = extensionManager
                .getExtension(ExtensionPointSingleton.class).get();
        ExtensionPointSingleton extension3 = extensionManager
                .getExtension(ExtensionPointSingleton.class).get();
        assertThat(extension1).isSameAs(extension2);
        assertThat(extension2).isSameAs(extension3);
        assertThat(extension3).isSameAs(extension1);
    }


    @Test
    public void testExtensionPointFresh() {
        ExtensionPointFresh extension1 = extensionManager.getExtension(ExtensionPointFresh.class)
                .get();
        ExtensionPointFresh extension2 = extensionManager.getExtension(ExtensionPointFresh.class)
                .get();
        ExtensionPointFresh extension3 = extensionManager.getExtension(ExtensionPointFresh.class)
                .get();
        assertThat(extension1).isNotSameAs(extension2);
        assertThat(extension2).isNotSameAs(extension3);
        assertThat(extension3).isNotSameAs(extension1);
    }
}
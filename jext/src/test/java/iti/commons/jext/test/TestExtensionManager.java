package iti.commons.jext.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import iti.commons.jext.ExtensionManager;

/**
 * @author ITI
 *         Created by ITI on 26/02/19
 */
public class TestExtensionManager {

    private ExtensionManager extensionManager = new ExtensionManager(Thread.currentThread().getContextClassLoader());

    
    @Test
    public void testFindExtensions() {
        List<MyExtensionPointV2_5> extensions = extensionManager.findExtensions(MyExtensionPointV2_5.class);
        Assertions.assertThat(extensions).hasSize(3);
        Assertions.assertThat(extensions).hasOnlyElementsOfTypes(
            MyExtensionV2_0.class,
            MyExtensionV2_5.class,
            MyExtensionV2_6.class
        );
    }
    
    @Test
    public void testLoadExtensions() {
        MyExtensionPointV2_5 loadedFirst = extensionManager
                .loadExtensionsFromNames(MyExtensionPointV2_5.class,Arrays.asList("A2_5"),x->{}).get(0);
        MyExtensionPointV2_5 loadedSecond = extensionManager
                .loadExtensionsFromNames(MyExtensionPointV2_5.class,Arrays.asList("A2_5"),x->{}).get(0);
        Assertions.assertThat(loadedFirst).isNotSameAs(loadedSecond);
    }
    
    
    @Test
    public void testFindFirstExtension() {
        Optional<MyExtensionPointV2_5> foundAny = extensionManager.findFirstExtension(MyExtensionPointV2_5.class);
        Assertions.assertThat(foundAny).isNotEmpty();
    }
    
    
    @Test
    public void testBlackList() {
        extensionManager.addBlackListEntry("*", "A2_6", "*");
        List<MyExtensionPointV2_5> extensions = extensionManager.findExtensions(MyExtensionPointV2_5.class);
        Assertions.assertThat(extensions).hasOnlyElementsOfTypes(MyExtensionV2_0.class,MyExtensionV2_5.class);
    }
    
    @Test
    public void testWhiteList() {
        extensionManager.addWhiteListEntry("*", "A2_6", "*");
        List<MyExtensionPointV2_5> extensions = extensionManager.findExtensions(MyExtensionPointV2_5.class);
        Assertions.assertThat(extensions).hasOnlyElementsOfTypes(MyExtensionV2_6.class);
    }

    
    
    
}

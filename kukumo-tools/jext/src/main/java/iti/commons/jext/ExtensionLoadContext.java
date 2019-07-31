package iti.commons.jext;

import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Wither(AccessLevel.PRIVATE)
@Getter @Accessors(fluent = true)
class ExtensionLoadContext<T> {

    
    static <T> ExtensionLoadContext<T> all(Class<T> extensionPoint) {
        return new ExtensionLoadContext<>(extensionPoint,dataOf(extensionPoint),selectAll());
    }
    
    static <T> ExtensionLoadContext<T> satisfying(
        Class<T> extensionPoint, 
        Predicate<T> condition
    ) {
        return new ExtensionLoadContext<>(extensionPoint,dataOf(extensionPoint),condition);
    }
    
    
    static <T> ExtensionLoadContext<T> satisfyingData(
        Class<T> extensionPoint, 
        Predicate<Extension> condition
    ) {
       return new ExtensionLoadContext<>(extensionPoint,
            dataOf(extensionPoint),
            conditionFromAnnotation(condition)
        );
    }
        
    
    
    private final Class<T> extensionPoint;
    private final ExtensionPoint extensionPointData;
    private final Predicate<T> condition; 
    
    private ClassLoader classLoader;
    private ExtensionLoader extensionLoader;
    private boolean externallyManaged;

    
    public ExtensionLoadContext<T> withInternalLoader(
        ClassLoader classLoader, 
        ExtensionLoader extensionLoader
    ) {
        return this
            .withClassLoader(classLoader)
            .withExtensionLoader(extensionLoader);
    }
    
    
    public ExtensionLoadContext<T> withExternalLoader(
        ClassLoader classLoader, 
        ExtensionLoader extensionLoader
    ) {
        return this
            .withClassLoader(classLoader)
            .withExtensionLoader(extensionLoader)
            .withExternallyManaged(true);
    }
    
    
    
    public Iterable<T> load () {
        return extensionLoader.load(extensionPoint, classLoader);
    }

    
    private static <T> Predicate<T> selectAll() {
        return x->true;
    }
    
    private static <T> Predicate<T> conditionFromAnnotation(Predicate<Extension> condition) {
        return extension -> condition.test(extension.getClass().getAnnotation(Extension.class));
    }

    
    private static <T> ExtensionPoint dataOf(Class<T> extensionPoint) {
        ExtensionPoint extensionPointData = extensionPoint.getAnnotation(ExtensionPoint.class);
        if (extensionPointData == null) {
            throw new IllegalArgumentException(extensionPoint+
                " must be annotated with @ExtensionPoint");
        }
        return extensionPointData;
    }

    
  
    
}

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.jext;


import java.util.function.Predicate;


class ExtensionLoadContext<T> {

    public static <T> ExtensionLoadContext<T> all(Class<T> extensionPoint) {
        return new ExtensionLoadContext<>(extensionPoint, dataOf(extensionPoint), selectAll());
    }


    public static <T> ExtensionLoadContext<T> satisfying(
        Class<T> extensionPoint,
        Predicate<T> condition
    ) {
        return new ExtensionLoadContext<T>(extensionPoint, dataOf(extensionPoint), condition);
    }


    public static <T> ExtensionLoadContext<T> satisfyingData(
        Class<T> extensionPoint,
        Predicate<Extension> condition
    ) {
        return new ExtensionLoadContext<T>(
            extensionPoint,
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


    private ExtensionLoadContext(
        Class<T> extensionPoint,
        ExtensionPoint extensionPointData,
        Predicate<T> condition
    ) {
        this.extensionPoint = extensionPoint;
        this.extensionPointData = extensionPointData;
        this.condition = condition;

    }





    public ExtensionLoadContext<T> withInternalLoader(
        ClassLoader classLoader,
        ExtensionLoader extensionLoader
    ) {
        var context = new ExtensionLoadContext<T>(extensionPoint, extensionPointData, condition);
        context.classLoader = classLoader;
        context.extensionLoader = extensionLoader;
        context.externallyManaged = false;
        return context;
    }



    public ExtensionLoadContext<T> withExternalLoader(
        ClassLoader classLoader,
        ExtensionLoader extensionLoader
    ) {
        var context = new ExtensionLoadContext<T>(extensionPoint, extensionPointData, condition);
        context.classLoader = classLoader;
        context.extensionLoader = extensionLoader;
        context.externallyManaged = true;
        return context;
    }


    public Iterable<T> load() {
        return extensionLoader.load(extensionPoint, classLoader);
    }


    public Predicate<T> condition() {
        return condition;
    }


    public ExtensionPoint extensionPointData() {
        return extensionPointData;
    }


    public Class<T> extensionPoint() {
        return extensionPoint;
    }


    public boolean isExternallyManaged() {
        return externallyManaged;
    }



    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("[Extensions of type ").append(extensionPoint);
        if (externallyManaged) {
            string.append(" (externally managed) ");
        }
        if (extensionLoader != null) {
            string.append(" loaded by ").append(extensionLoader);
        }
        if (classLoader != null) {
            string.append(" using class loader ").append(classLoader);
        }
        return string.append("]").toString();
    }


    private static <T> Predicate<T> selectAll() {
        return x -> true;
    }


    private static <T> Predicate<T> conditionFromAnnotation(Predicate<Extension> condition) {
        return extension -> condition.test(extension.getClass().getAnnotation(Extension.class));
    }


    private static <T> ExtensionPoint dataOf(Class<T> extensionPoint) {
        ExtensionPoint extensionPointData = extensionPoint.getAnnotation(ExtensionPoint.class);
        if (extensionPointData == null) {
            throw new IllegalArgumentException(
                extensionPoint + " must be annotated with @ExtensionPoint"
            );
        }
        return extensionPointData;
    }

}

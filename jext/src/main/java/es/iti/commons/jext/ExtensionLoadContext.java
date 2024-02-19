/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


import java.util.function.Predicate;


/**
 * This class provides a context for loading extensions of a specific type.
 * It allows specifying the conditions for selecting the extensions to be loaded,
 * as well as the class loader and the extension loader to be used for loading
 * the extensions.
 *
 * @param <T> the type of the extensions to be loaded
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class ExtensionLoadContext<T> {

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

    /**
     * Creates an extension load context for loading all extensions of a
     * specific type.
     *
     * @param extensionPoint the class of the extension point
     */
    public static <T> ExtensionLoadContext<T> all(Class<T> extensionPoint) {
        return new ExtensionLoadContext<>(extensionPoint, dataOf(extensionPoint), selectAll());
    }

    /**
     * Creates an extension load context for loading all extensions of a
     * specific type that satisfy a given condition based on their
     * extension data.
     *
     * @param extensionPoint the class of the extension point
     * @param condition the condition used to select the extensions based
     *                  on their extension data
     * @return an extension load context for loading all extensions of the specified type
     */
    public static <T> ExtensionLoadContext<T> satisfying(
            Class<T> extensionPoint,
            Predicate<T> condition
    ) {
        return new ExtensionLoadContext<>(extensionPoint, dataOf(extensionPoint), condition);
    }

    /**
     * Creates an extension load context for loading all extensions of a
     * specific type that satisfy a given condition based on their
     * extension data.
     *
     * @param extensionPoint the class of the extension point
     * @param condition the condition used to select the extensions based
     *                  on their extension data
     * @return an extension load context for loading all extensions of the specified type
     */
    public static <T> ExtensionLoadContext<T> satisfyingData(
            Class<T> extensionPoint,
            Predicate<Extension> condition
    ) {
        return new ExtensionLoadContext<>(
                extensionPoint,
                dataOf(extensionPoint),
                conditionFromAnnotation(condition)
        );
    }

    private static <T> Predicate<T> selectAll() {
        return x -> true;
    }

    private static <T> Predicate<T> conditionFromAnnotation(Predicate<Extension> condition) {
        return extension -> condition.test(extension.getClass().getAnnotation(Extension.class));
    }

    /**
     * This method returns the {@link ExtensionPoint} annotation data for the
     * specified extension point class.
     *
     * @param extensionPoint the extension point class
     * @return the extension point annotation data
     * @throws IllegalArgumentException if the specified extension point class
     * is not annotated with {@link ExtensionPoint}
     */
    private static <T> ExtensionPoint dataOf(Class<T> extensionPoint) {
        ExtensionPoint extensionPointData = extensionPoint.getAnnotation(ExtensionPoint.class);
        if (extensionPointData == null) {
            throw new IllegalArgumentException(
                    extensionPoint + " must be annotated with @ExtensionPoint"
            );
        }
        return extensionPointData;
    }

    /**
     * Creates a new internal {@code ExtensionLoadContext} object with specified
     * ClassLoader and ExtensionLoader.
     *
     * @param classLoader the class loader to use for loading the extensions
     * @param extensionLoader the extension loader to use for loading the extensions
     * @return an extension load context for loading all internal extensions of the
     * specified type
     */
    public ExtensionLoadContext<T> withInternalLoader(
            ClassLoader classLoader,
            ExtensionLoader extensionLoader
    ) {
        var context = new ExtensionLoadContext<>(extensionPoint, extensionPointData, condition);
        context.classLoader = classLoader;
        context.extensionLoader = extensionLoader;
        context.externallyManaged = false;
        return context;
    }

    /**
     * Creates a new external {@code ExtensionLoadContext} object with specified
     * ClassLoader and ExtensionLoader.
     *
     * @param classLoader the class loader to use for loading the extensions
     * @param extensionLoader the extension loader to use for loading the extensions
     * @return an extension load context for loading all internal extensions of the
     * specified type
     */
    public ExtensionLoadContext<T> withExternalLoader(
            ClassLoader classLoader,
            ExtensionLoader extensionLoader
    ) {
        var context = new ExtensionLoadContext<>(extensionPoint, extensionPointData, condition);
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

    /**
     * Returns a string representation of the object from the values of its fields.
     *
     * @return a string representation of the object.
     */
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

}
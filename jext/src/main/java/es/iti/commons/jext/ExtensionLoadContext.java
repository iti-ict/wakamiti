/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


import java.util.function.Predicate;


/**
 * Immutable descriptor for one extension-discovery request.
 * <p>
 * A context encapsulates the extension point metadata, selection predicate,
 * loader strategy and class loader to use during discovery.
 * </p>
 *
 * @param <T> extension point type
 */
public final class ExtensionLoadContext<T> {

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
     * @param <T>            the type of extensions to load
     * @return a context that selects all extensions of the specified type
     */
    public static <T> ExtensionLoadContext<T> all(
            Class<T> extensionPoint
    ) {
        return new ExtensionLoadContext<>(extensionPoint, dataOf(extensionPoint), selectAll());
    }

    /**
     * Creates a context that selects extensions whose
     * {@link Extension} metadata matches the supplied condition.
     *
     * @param extensionPoint extension point contract
     * @param condition      predicate evaluated against extension annotations
     * @param <T>            extension point type
     * @return discovery context for metadata-filtered extensions
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
     * @param condition      the condition used to select the extensions based
     *                       on their extension data
     * @param <T>            the type of extensions to load
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

    private static <T> Predicate<T> conditionFromAnnotation(
            Predicate<Extension> condition
    ) {
        return extension -> condition.test(extension.getClass().getAnnotation(Extension.class));
    }

    /**
     * This method returns the {@link ExtensionPoint} annotation data for the
     * specified extension point class.
     *
     * @param extensionPoint the extension point class
     * @return the extension point annotation data
     * @throws IllegalArgumentException if the specified extension point class
     *                                  is not annotated with {@link ExtensionPoint}
     */
    private static <T> ExtensionPoint dataOf(
            Class<T> extensionPoint
    ) {
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
     * @param classLoader     the class loader to use for loading the extensions
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
     * Creates a context configured to discover externally managed extensions.
     *
     * @param classLoader     class loader used for discovery
     * @param extensionLoader loader implementation used for discovery
     * @return context for loading external extensions of the same point
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

    /**
     * Discovers extension instances using the loader and class loader configured
     * for this context.
     * <p>
     * Discovery is delegated to the selected {@link ExtensionLoader}; the
     * selection {@linkplain #condition() condition} is intentionally not applied
     * here because the extension manager evaluates it together with version,
     * priority, and replacement rules.
     * </p>
     *
     * @return the extensions discovered for {@link #extensionPoint()}; the
     * returned iterable may perform discovery lazily, depending on the loader
     */
    public Iterable<T> load() {
        return extensionLoader.load(extensionPoint, classLoader);
    }

    /**
     * Returns the predicate used to decide whether a discovered extension is a
     * candidate for this load operation.
     *
     * @return the non-null selection predicate supplied when the context was
     * created, or a predicate that accepts every extension for {@link #all(Class)}
     */
    public Predicate<T> condition() {
        return condition;
    }

    /**
     * Returns the metadata declared by the extension point type.
     *
     * @return the {@link ExtensionPoint} annotation present on
     * {@link #extensionPoint()}
     */
    public ExtensionPoint extensionPointData() {
        return extensionPointData;
    }

    /**
     * Returns the service contract whose implementations are to be discovered.
     *
     * @return the extension point type passed to {@link #all(Class)},
     * {@link #satisfying(Class, Predicate)}, or
     * {@link #satisfyingData(Class, Predicate)}
     */
    public Class<T> extensionPoint() {
        return extensionPoint;
    }

    /**
     * Indicates whether the extension instances are owned by an external
     * container rather than instantiated through Java's service-provider
     * mechanism.
     *
     * @return {@code true} when this context was configured with
     * {@link #withExternalLoader(ClassLoader, ExtensionLoader)}; {@code false}
     * when it uses an internal loader
     */
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

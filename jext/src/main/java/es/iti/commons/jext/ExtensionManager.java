/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Component that provides operations to retrieve instances of
 * classes annotated with {@link Extension}.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class ExtensionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private final ClassLoader[] classLoaders;
    private final ExtensionLoader builtInExtensionLoader = new InternalExtensionLoader();
    private final List<ExtensionLoader> extensionLoaders = extensionLoaders();

    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Set<Class<?>>> invalidExtensions = new HashMap<>();
    private final Map<Class<?>, Set<Class<?>>> validExtensions = new HashMap<>();
    private final Map<Object, Extension> extensionMetadata = new HashMap<>();
    private final Map<Class<?>, List<Object>> cachedValidExtensionInstances = new HashMap<>();

    /**
     * Creates a new extension manager using the default class loader of the
     * current thread.
     */
    public ExtensionManager() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a new extension manager restricted to a specific set of class
     * loaders.
     *
     * @param loaders The class loaders used for loading extension classes.
     */
    public ExtensionManager(ClassLoader... loaders) {
        this.classLoaders = loaders;
    }

    private static String id(Extension extension) {
        return extension.provider() + ":" + extension.name() + ":" + extension.version();
    }

    private static List<ExtensionLoader> extensionLoaders() {
        List<ExtensionLoader> loaders = new ArrayList<>();
        ServiceLoader.load(ExtensionLoader.class).forEach(loaders::add);
        return loaders;
    }

    /**
     * Get the extension annotated metadata for a given extension.
     *
     * @param extension An extension instance.
     * @return The extension metadata, or {@code null} if a passed object is
     * not an extension.
     */
    public <T> Extension getExtensionMetadata(T extension) {
        return extensionMetadata.computeIfAbsent(
                extension,
                e -> e.getClass().getAnnotation(Extension.class)
        );
    }

    /**
     * Get all the extension annotated metadata for a given extension point.
     *
     * @param extensionPoint An extension point.
     * @return The extension metadata, or {@code null} if a passed object is
     * not an extension.
     */
    public <T> Stream<Extension> getExtensionMetadata(Class<T> extensionPoint) {
        return getExtensions(extensionPoint).map(this::getExtensionMetadata);
    }

    /**
     * Retrieves an instance for the given extension point if any exists.
     * In the case of existing multiple alternatives, the one with the highest
     * priority will be used.
     *
     * @param extensionPoint The extension point type.
     * @return An optional object either empty or wrapping the instance.
     */
    public <T> Optional<T> getExtension(Class<T> extensionPoint) {
        return loadFirst(ExtensionLoadContext.all(extensionPoint));
    }

    /**
     * Retrieves the instance for the given extension point that satisfies the
     * specified condition if any exists. In the case of existing multiple
     * alternatives, the one with the highest priority will be used.
     *
     * @param extensionPoint The extension point type.
     * @param condition      Only extensions satisfying this condition will be
     *                       returned.
     * @return An optional object either empty or wrapping the instance.
     */
    public <T> Optional<T> getExtensionThatSatisfy(
            Class<T> extensionPoint,
            Predicate<T> condition
    ) {
        return loadFirst(ExtensionLoadContext.satisfying(extensionPoint, condition));
    }

    /**
     * Retrieves the instance for the given extension point that satisfies the
     * specified condition if any exists. In the case of existing multiple
     * alternatives, the one with the highest priority will be used.
     *
     * @param extensionPoint The extension point type.
     * @param condition      Only extensions which their metadata satisfies this
     *                       condition will be returned.
     * @return An optional object either empty or wrapping the instance.
     */
    public <T> Optional<T> getExtensionThatSatisfyMetadata(
            Class<T> extensionPoint,
            Predicate<Extension> condition
    ) {
        return loadFirst(ExtensionLoadContext.satisfyingData(extensionPoint, condition));
    }

    /**
     * Retrieves a priority-ordered list with all extensions for the given
     * extension point.
     *
     * @param extensionPoint The extension point type.
     * @return A list with the extensions, empty if none was found.
     */
    public <T> Stream<T> getExtensions(Class<T> extensionPoint) {
        return loadAll(ExtensionLoadContext.all(extensionPoint));
    }

    /**
     * Retrieves a priority-ordered list with all then extensions for the given
     * extension point that satisfies the specified condition.
     *
     * @param extensionPoint The extension point type.
     * @param condition      Only extensions satisfying this condition will be
     *                       returned.
     * @return A list with the extensions, empty if none was found.
     */
    public <T> Stream<T> getExtensionsThatSatisfy(Class<T> extensionPoint, Predicate<T> condition) {
        return loadAll(ExtensionLoadContext.satisfying(extensionPoint, condition));
    }

    /**
     * Retrieves a priority-ordered list with all then extensions for the given
     * extension point that satisfies the specified condition.
     *
     * @param extensionPoint The extension point type.
     * @param condition      Only extensions which their metadata satisfies this
     *                       condition will be returned.
     * @return A list with the extensions, empty if none was found.
     */
    public <T> Stream<T> getExtensionsThatSatisfyMetadata(
            Class<T> extensionPoint,
            Predicate<Extension> condition
    ) {
        return loadAll(ExtensionLoadContext.satisfyingData(extensionPoint, condition));
    }

    /**
     * Retrieves a priority-ordered stream with all valid extensions for the
     * given extension point.
     *
     * @param context The context specifying the extension point and condition.
     * @return A stream of valid extensions, sorted by priority.
     */
    protected <T> Stream<T> loadAll(ExtensionLoadContext<T> context) {
        return obtainCachedValidExtensions(context).stream()
                .filter(context.condition())
                .sorted(sortByPriority())
                .map(extension -> resolveInstance(extension, context));
    }

    /**
     * Retrieves an optional instance for the given extension point if any
     * exists.
     *
     * @param context The context specifying the extension point and condition.
     * @return An optional object either empty or wrapping the instance with
     * the highest priority.
     */
    protected <T> Optional<T> loadFirst(ExtensionLoadContext<T> context) {
        return obtainCachedValidExtensions(context).stream()
                .filter(context.condition()).min(sortByPriority())
                .map(extension -> resolveInstance(extension, context));
    }

    /**
     * Resolves an instance of the extension based on the specified load strategy
     * in the given context.
     * <p>
     * This method determines the appropriate resolution strategy based on the
     * extension point's load strategy in the provided context. It returns the
     * resolved instance accordingly.
     * </p>
     *
     * @param extension The extension to be resolved.
     * @param context   The context specifying the extension point and its data.
     * @return The resolved instance of the extension based on the load strategy.
     */
    protected <T> T resolveInstance(T extension, ExtensionLoadContext<T> context) {
        T instance;
        switch (context.extensionPointData().loadStrategy()) {
            case SINGLETON:
                instance = singleton(extension);
                break;
            case FRESH:
                instance = newInstance(extension);
                break;
            default:
                instance = extension;
        }
        return instance;
    }

    /**
     * Retrieves the list of valid extensions from the cache or obtains them and
     * stores them in the cache.
     *
     * @param context The context specifying the extension point and its data.
     * @return The list of valid extensions for the specified extension point.
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> obtainCachedValidExtensions(ExtensionLoadContext<T> context) {
        List<Object> cache = cachedValidExtensionInstances.get(context.extensionPoint());
        if (cache != null) {
            LOGGER.trace("{} :: Retrieved from cache [{}]", context, cache);
            return (List<T>) cache;
        }
        List<T> extensions = obtainValidExtensions(context);
        cachedValidExtensionInstances.put(context.extensionPoint(), (List<Object>) extensions);
        return extensions;
    }

    /**
     * Retrieves the valid extensions for the specified extension point using the
     * provided extension context.
     *
     * @param context The extension context specifying the extension point and its data.
     * @return A list containing the valid extensions for the specified extension point.
     */
    protected <T> List<T> obtainValidExtensions(ExtensionLoadContext<T> context) {

        this.validExtensions.putIfAbsent(context.extensionPoint(), new HashSet<>());
        this.invalidExtensions.putIfAbsent(context.extensionPoint(), new HashSet<>());

        List<T> collectedExtensions = new ArrayList<>();
        for (ClassLoader classLoader : classLoaders) {
            collectValidExtensions(
                    context.withInternalLoader(classLoader, builtInExtensionLoader),
                    collectedExtensions
            );
            for (ExtensionLoader extensionLoader : extensionLoaders) {
                collectValidExtensions(
                        context.withExternalLoader(classLoader, extensionLoader),
                        collectedExtensions
                );
            }
        }
        filterOverriddenExtensions(collectedExtensions);
        return collectedExtensions;
    }

    /**
     * Collects valid extensions for the specified extension point within
     * the given extension context.
     *
     * @param context             The extension context specifying the
     *                            extension point and its data.
     * @param collectedExtensions The list to which valid extensions
     *                            will be added.
     * @param <T>                 The type of the extension point.
     */
    private <T> void collectValidExtensions(
            ExtensionLoadContext<T> context,
            List<T> collectedExtensions
    ) {
        Class<T> extensionPoint = context.extensionPoint();
        LOGGER.trace("{} :: Searching...", context);
        for (T extension : context.load()) {
            if (hasBeenInvalidated(extensionPoint, extension)) {
                LOGGER.trace(
                        "{} :: Found {} but ignored (it is marked as invalid)",
                        context,
                        extension
                );
                break;
            }
            if (!hasBeenValidated(extensionPoint, extension)) {
                boolean valid = validateExtension(context, extension);
                if (valid) {
                    LOGGER.trace("{} :: Found {}", context, extension);
                    collectedExtensions.add(extension);
                } else {
                    LOGGER.trace(
                            "{} :: Found {} but ignored (marked as invalid)",
                            context,
                            extension
                    );
                }
            }
        }
    }

    /**
     * Validates the compatibility and annotation of an extension within
     * the given extension context.
     *
     * @param context   The extension context specifying the extension
     *                  point and its data.
     * @param extension The extension instance to validate.
     * @param <T>       The type of the extension point.
     * @return {@code true} if the extension is valid, {@code false} otherwise.
     */
    protected <T> boolean validateExtension(ExtensionLoadContext<T> context, T extension) {
        Class<T> extensionPoint = context.extensionPoint();
        ExtensionPoint extensionPointData = context.extensionPointData();
        Extension extensionData = getExtensionMetadata(extension);

        if (extensionData == null) {
            LOGGER.warn(
                    "Class {} is not annotated with @Extension and will be ignored",
                    extension.getClass()
            );
            this.invalidExtensions.get(extensionPoint).add(extension.getClass());
            return false;
        }

        if (extensionData.externallyManaged() != context.isExternallyManaged()) {
            this.invalidExtensions.get(extensionPoint).add(extension.getClass());
            return false;
        }

        if (!areCompatible(extensionPointData, extensionData)) {
            LOGGER.warn(
                    "Extension point version of {} ({}) is not compatible with expected version {}",
                    id(extensionData),
                    extensionData.extensionPointVersion(),
                    extensionPointData.version()
            );
            this.invalidExtensions.get(extensionPoint).add(extension.getClass());
            return false;
        }

        this.validExtensions.get(extensionPoint).add(extension.getClass());
        return true;
    }

    /**
     * Filters out overridden extensions from the given list.
     * <p>
     * This method identifies overridable extensions within the provided list based on their
     * metadata and removes overridden extensions, updating the list accordingly.
     * </p>
     *
     * @param extensions The list of extensions to filter.
     * @param <T>        The type of the extension point.
     */
    private <T> void filterOverriddenExtensions(List<T> extensions) {

        List<T> overridableExtensions = extensions.stream()
                .filter(extension -> getExtensionMetadata(extension).overridable())
                .collect(Collectors.toList());

        Map<String, T> overridableExtensionClassNames = overridableExtensions.stream()
                .collect(
                        Collectors.toMap(
                                extension -> extension.getClass().getCanonicalName(),
                                Function.identity()
                        )
                );

        for (T extension : new ArrayList<>(extensions)) {
            Extension metadata = getExtensionMetadata(extension);
            T overridable = overridableExtensionClassNames.get(metadata.overrides());
            if (overridable != null) {
                extensions.remove(overridable);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(
                            "Extension {} overrides extension {}",
                            id(getExtensionMetadata(extension)),
                            id(getExtensionMetadata(overridable))
                    );
                }
            }
        }
    }

    /**
     * Checks the compatibility between the version of an extension point and an extension.
     *
     * @param extensionPointData The metadata of the extension point.
     * @param extensionData      The metadata of the extension.
     * @return {@code true} if the versions are compatible, {@code false} otherwise.
     * @throws IllegalArgumentException If there is an issue with parsing the version.
     */
    private boolean areCompatible(ExtensionPoint extensionPointData, Extension extensionData) {
        ExtensionVersion extensionPointVersion = new ExtensionVersion(extensionPointData.version());
        try {
            ExtensionVersion extensionDataPointVersion = new ExtensionVersion(
                    extensionData.extensionPointVersion()
            );
            return extensionDataPointVersion.isCompatibleWith(extensionPointVersion);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Bad extensionPointVersion in {}", id(extensionData));
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T newInstance(T extension) {
        try {
            return (T) extension.getClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            LOGGER.error(
                    "Error loading new instance of {} : {}",
                    extension.getClass(),
                    e.getMessage(),
                    e
            );
            return null;
        }
    }

    private int getExtensionPriority(Object extension) {
        return getExtensionMetadata(extension).priority();
    }

    @SuppressWarnings("unchecked")
    private <T> T singleton(T extension) {
        return (T) singletons.computeIfAbsent(extension.getClass(), x -> extension);
    }

    protected <T> boolean hasBeenValidated(Class<T> extensionPoint, T extension) {
        return validExtensions.get(extensionPoint).contains(extension.getClass());
    }

    protected <T> boolean hasBeenInvalidated(Class<T> extensionPoint, T extension) {
        return invalidExtensions.get(extensionPoint).contains(extension.getClass());
    }

    protected Comparator<Object> sortByPriority() {
        return Comparator.comparingInt(this::getExtensionPriority);
    }

}
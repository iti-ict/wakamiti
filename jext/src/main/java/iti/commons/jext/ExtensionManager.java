package iti.commons.jext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Component that provides operations in order to retrieve instances of classes annotated with
 * {@link Extension}.
 */
public class ExtensionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    /*
      Replicate the data of @Extension, due to the impossibility of adding methods to @interface
     */
    private static class ExtensionMetadata {
        private String provider;
        private String name;
        private String version;

        public ExtensionMetadata(String provider, String name, String version) {
            this.provider = provider;
            this.name = name;
            this.version = version;
        }

        public boolean matches(Extension extension) {
            return
                (this.provider.equals("*") || this.provider.equals(extension.provider())) &&
                (this.name.equals("*") || this.name.equals(extension.name())) &&
                (this.version.equals("*") || this.version.equals(extension.version()));
        }
    }


    private final ClassLoader[] loaders;
    private final List<ExtensionMetadata> whiteList = new ArrayList<>();
    private final List<ExtensionMetadata> blackList = new ArrayList<>();
    private final List<ExtensionLoader> externalLoaders = StreamSupport.stream(
            ServiceLoader.load(ExtensionLoader.class).spliterator(), false
        ).collect(Collectors.toList());

    private Map<Class<?>,Set<Class<?>>> invalidExtensions = new HashMap<>();

    public ExtensionManager() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ExtensionManager(ClassLoader... loaders) {
        this.loaders = loaders;
    }




    /**
     * Add a metadata entry for the white list. If the white list contains one or more entry,
     * the extension manager only will retrieve extensions that are registered in the white list.
     * @param provider Provider identifier (can be <pre>*</pre> to match everything)
     * @param name Name identifier (can be <pre>*</pre> to match everything)
     * @param version Version identifier (can be <pre>*</pre> to match everything)
     */
    public ExtensionManager addWhiteListEntry(String provider, String name, String version) {
        this.whiteList.add(new ExtensionMetadata(provider,name,version));
        return this;
    }


    /**
     * Add a metadata entry for the black list. If the black list contains an extension, it will be
     * never retrieved by the extension manager (even if the same entry is in the white list).
     * @param provider Provider identifier (can be <pre>*</pre> to match everything)
     * @param name Name identifier (can be <pre>*</pre> to match everything)
     * @param version Version identifier (can be <pre>*</pre> to match everything)
     */
    public ExtensionManager addBlackListEntry(String provider, String name, String version) {
        this.blackList.add(new ExtensionMetadata(provider,name,version));
        return this;
    }


    public <T> Optional<T> get(Class<T> extensionPoint) {
        return findFirst(extensionPoint, x->true);
    }



    public <T> List<T> findExtensions(Class<T> extensionPoint) {
        return find(extensionPoint, x->true);
    }




    public <T> List<T> findExtensionsThatSatisfy(Class<T> extensionPoint, Predicate<T> condition) {
        return find(extensionPoint, condition);
    }


    public <T> List<T> findExtensionsThatSatisfyAnnotation(Class<T> extensionPoint, Predicate<Extension> condition) {
        return find(extensionPoint, conditionFromAnnotation(condition));
    }


    public <T> List<T> findExtensionsFromNames(Class<T> extensionPoint, List<String> names) {
        return find(extensionPoint, conditionFromNames(names));
    }



    public <T> Optional<T> findFirstExtension(Class<T> extensionPoint) {
        return findFirst(extensionPoint, x->true);
    }


    public <T> Optional<T> findFirstExtensionThatSatisfies(Class<T> extensionPoint, Predicate<T> condition) {
        return findFirst(extensionPoint, condition);
    }

    public <T> Optional<T> findFirstExtensionFromName(Class<T> extensionPoint, String name) {
        return findFirst(extensionPoint, conditionFromNames(Arrays.asList(name)));
    }




    public <T> List<T> loadExtensions(Class<T> extensionPoint, Consumer<T> initializer) {
        return load(extensionPoint, initializer, x->true);
    }


    public <T> List<T> loadExtensionsThatSatisfy(Class<T> extensionPoint, Predicate<T> condition, Consumer<T> initializer) {
        return load(extensionPoint, initializer, condition);
    }



    public <T> List<T> loadExtensionsThatSatisfyAnnotation(Class<T> extensionPoint, Predicate<Extension> condition, Consumer<T> initializer) {
        return load(extensionPoint, initializer, conditionFromAnnotation(condition));
    }



    public <T> List<T> loadExtensionsFromNames(Class<T> extensionPoint, List<String> names, Consumer<T> initializer) {
        return load(extensionPoint, initializer, conditionFromNames(names));
    }


    public <T> Optional<T> loadFirstExtension(Class<T> extensionPoint, Consumer<T> initializer) {
        return loadFirst(extensionPoint, initializer, x->true);
    }



    public <T> Optional<T> loadFirstThatSatisfies(Class<T> extensionPoint,  Predicate<T> condition, Consumer<T> initializer) {
        return loadFirst(extensionPoint, initializer, condition);
    }






    protected <T> Optional<T> findFirst(Class<T> extensionPoint, Predicate<T> condition) {
        List<T> extensions = find(extensionPoint, condition);
        if (extensions.isEmpty()) {
            LOGGER.warn("No extension for extension point {} found", extensionPoint.getCanonicalName());
            return Optional.empty();
        }
        if (extensions.size() > 1 && LOGGER.isWarnEnabled()) {
            String classpathOrder = extensions.stream()
                    .map(extension -> " - "+extension.getClass())
                    .collect(Collectors.joining("\n"));
            LOGGER.warn("More than one extension for extension point {} found\nClasspath order is: \n{}",
                    extensionPoint, classpathOrder);
        }
        return Optional.of(extensions.get(0));
    }




    protected <T> List<T> find(Class<T> extensionPoint, Predicate<T> condition) {
        ExtensionPoint extensionPointData = extensionPoint.getAnnotation(ExtensionPoint.class);
        if (extensionPointData == null) {
            throw new IllegalArgumentException(extensionPoint+" must be annotated with @ExtensionPoint");
        }
        List<T> validExtensions = new ArrayList<>();
        this.invalidExtensions.computeIfAbsent(extensionPoint, x->new HashSet<>());

        for (ClassLoader loader : loaders) {
            collectValidExtensions(extensionPoint,condition,extensionPointData,validExtensions,loader,ServiceLoader::load,false);
        }

        for (ExtensionLoader externalResolver : externalLoaders) {
            for (ClassLoader loader : loaders) {
                collectValidExtensions(extensionPoint,condition,extensionPointData,validExtensions,loader,externalResolver,true);
            }
        }

        return filterOverriddenExtensions(validExtensions);
    }




    private <T> List<T> filterOverriddenExtensions(List<T> validExtensions) {
        List<String> overriddenExtensions = validExtensions.stream()
                .map(this::getExtensionMetadata)
                .map(Extension::overrides)
                .filter(s->!s.isEmpty())
                .collect(Collectors.toList());
        Predicate<T> isNotOverridden = extension -> !overriddenExtensions.contains(extension.getClass().getCanonicalName());
        return validExtensions.stream().filter(isNotOverridden).collect(Collectors.toList());
    }


    private <T> Extension getExtensionMetadata(T extension) {
        return extension.getClass().getAnnotation(Extension.class);
    }




    private <T> void collectValidExtensions(
            Class<T> extensionPoint,
            Predicate<T> condition,
            ExtensionPoint extensionPointData,
            List<T> validExtensions,
            ClassLoader loader,
            ExtensionLoader extensionLoader,
            boolean externallyManaged
    ) {
        for (T extension : extensionLoader.load(extensionPoint, loader)) {
            boolean skip = false;
            if (this.invalidExtensions.get(extensionPoint).contains(extension.getClass())) {
                skip = true;
            }
            Extension extensionData = extension.getClass().getAnnotation(Extension.class);
            if (!skip && extensionData.externallyManaged() != externallyManaged) {
                skip = true;
            }
            if (!skip && extensionData == null) {
                LOGGER.warn("Class {} is not annotated with @Extension and will be ignored",extension.getClass());
                this.invalidExtensions.get(extensionPoint).add(extension.getClass());
                skip = true;
            }
            if (!skip && !areCompatible(extensionPointData,extensionData)) {
                LOGGER.warn("Extension point version of {} is not compatible with expected version {}",
                        id(extensionData), extensionPointData.version());
                this.invalidExtensions.get(extensionPoint).add(extension.getClass());
                skip = true;
            }
            if (!skip && !condition.test(extension)) {
                skip = true;
            }
            if (!skip && !testBlackWhiteList(extensionData)) {
                skip = true;
            }
            if (!skip) {
                validExtensions.add(extension);
            }
        }
    }





    protected <T> List<T> load(Class<T> extensionPoint, Consumer<T> initializer, Predicate<T> condition) {
        List<T> loadedExtensions = new ArrayList<>();
        for (T extension : findExtensionsThatSatisfy(extensionPoint, condition)) {
            T initiatedExtension = init(extension,initializer);
            if (initiatedExtension != null){
                loadedExtensions.add(initiatedExtension);
            }
        }
        return loadedExtensions;
    }


    protected <T> Optional<T> loadFirst(Class<T> extensionPoint, Consumer<T> initializer, Predicate<T> condition) {
        Optional<T> extension = findFirst(extensionPoint,condition);
        return extension.map(e->init(e,initializer));
    }



    @SuppressWarnings("unchecked")
    private <T> T init(T extension, Consumer<T> initializer) {
        try {
            extension = (T) extension.getClass().getConstructor().newInstance();
            initializer.accept(extension);
            return extension;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Error loading new instance of {} : {}", extension.getClass(), e.getMessage(), e);
            return null;
        }
    }



    private boolean areCompatible(ExtensionPoint extensionPoint, Extension extension) {
        // assuming format major.minor...., compare the major segment
        String extensionPointVersion = new StringTokenizer(extensionPoint.version(),".").nextToken();
        String extensionVersion = new StringTokenizer(extension.extensionPointVersion(),".").nextToken();
        return extensionPointVersion.equals(extensionVersion);
    }



    private boolean testBlackWhiteList(Extension extension) {
        Predicate<ExtensionMetadata> matcher = extensionMetadata -> extensionMetadata.matches(extension);
        if (!blackList.isEmpty() && blackList.stream().anyMatch(matcher)) {
            return false;
        }
        if (!whiteList.isEmpty()) {
            return whiteList.stream().anyMatch(matcher);
        }
        return true;
    }



    private static String id(Extension extension) {
        return extension.provider()+":"+extension.name()+":"+extension.version();
    }



    private static <T> Predicate<T> conditionFromAnnotation(Predicate<Extension> condition) {
        return extension -> condition.test(extension.getClass().getAnnotation(Extension.class));
    }


    private static <T> Predicate<T> conditionFromNames(List<String> names) {
        return extension -> names.contains(extension.getClass().getAnnotation(Extension.class).name());
    }






}

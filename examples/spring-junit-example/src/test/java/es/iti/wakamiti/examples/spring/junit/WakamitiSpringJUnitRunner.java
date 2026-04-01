package es.iti.wakamiti.examples.spring.junit;

import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runner variant that bootstraps Spring before Wakamiti initialization.
 */
public class WakamitiSpringJUnitRunner extends WakamitiJUnitRunner {

    private static final Map<Class<?>, ConfigurableApplicationContext> SPRING_CONTEXTS = new ConcurrentHashMap<>();
    private final Class<?> configurationClass;

    public WakamitiSpringJUnitRunner(Class<?> configurationClass) throws InitializationError {
        super(bootstrapSpring(configurationClass));
        this.configurationClass = configurationClass;
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                initWakamiti();
                statement.evaluate();
            }
        };
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        Statement wrapped = super.withAfterClasses(statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    wrapped.evaluate();
                } finally {
                    closeSpringContext(configurationClass);
                }
            }
        };
    }

    private static Class<?> bootstrapSpring(Class<?> configurationClass) throws InitializationError {
        try {
            SpringBootstrapConfig springBootstrapConfig = resolveSpringBootstrapConfig(configurationClass);
            SPRING_CONTEXTS.computeIfAbsent(
                    configurationClass,
                    ignored -> new SpringApplicationBuilder()
                            .sources(springBootstrapConfig.sources())
                            .profiles(springBootstrapConfig.profiles())
                            .properties(springBootstrapConfig.properties())
                            .run(springBootstrapConfig.args())
            );
            return configurationClass;
        } catch (Exception ex) {
            throw new InitializationError(ex);
        }
    }

    private static void closeSpringContext(Class<?> configurationClass) {
        ConfigurableApplicationContext context = SPRING_CONTEXTS.remove(configurationClass);
        if (context != null && context.isActive()) {
            context.close();
        }
    }

    private static SpringBootstrapConfig resolveSpringBootstrapConfig(Class<?> configurationClass) throws InitializationError {
        SpringBootTest springBootTest = configurationClass.getAnnotation(SpringBootTest.class);
        if (springBootTest == null) {
            throw new InitializationError(
                    "Missing @" + SpringBootTest.class.getSimpleName() + " in " + configurationClass.getName()
            );
        }

        if (springBootTest.classes().length == 0) {
            throw new InitializationError(
                    "@" + SpringBootTest.class.getSimpleName() + " must define 'classes' with at least one source class"
            );
        }

        ActiveProfiles activeProfiles = configurationClass.getAnnotation(ActiveProfiles.class);
        String[] profiles = new String[0];
        if (activeProfiles != null) {
            profiles = activeProfiles.profiles().length > 0
                    ? activeProfiles.profiles()
                    : activeProfiles.value();
        }

        List<String> properties = new ArrayList<>(Arrays.asList(springBootTest.properties()));
        TestPropertySource testPropertySource = configurationClass.getAnnotation(TestPropertySource.class);
        if (testPropertySource != null) {
            properties.addAll(Arrays.asList(testPropertySource.properties()));
        }

        return new SpringBootstrapConfig(
                springBootTest.classes(),
                profiles,
                properties.toArray(new String[0]),
                springBootTest.args()
        );
    }

    private static final class SpringBootstrapConfig {
        private final Class<?>[] sources;
        private final String[] profiles;
        private final String[] properties;
        private final String[] args;

        private SpringBootstrapConfig(Class<?>[] sources, String[] profiles, String[] properties, String[] args) {
            this.sources = sources;
            this.profiles = profiles;
            this.properties = properties;
            this.args = args;
        }

        private Class<?>[] sources() {
            return sources;
        }

        private String[] profiles() {
            return profiles;
        }

        private String[] properties() {
            return properties;
        }

        private String[] args() {
            return args;
        }
    }
}

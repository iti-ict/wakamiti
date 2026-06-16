# jExt Spring

`jext-spring` bridges `jext` with the Spring application context. It contributes a `SpringExtensionLoader` through `META-INF/services`, so `ExtensionManager` can resolve externally managed extensions from Spring beans.

## When to use it

Use this module when:

- your extension implementations are Spring beans
- you want `ExtensionManager` to discover them without manual wiring
- lifecycle and dependency injection should remain under Spring control

## Maven dependency

```xml
<dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>jext-spring</artifactId>
    <version>2.3.0</version>
</dependency>
```

## Required application bean

Register `ApplicationContextProvider` in the Spring context so the loader can access the `ApplicationContext`:

```java
@Configuration
public class JextSpringConfiguration {

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }
}
```

## Example

```java
@ExtensionPoint
public interface MessageFormatter {
    String format(String value);
}

@Component
@Extension(
    provider = "example",
    name = "springBean",
    version = "1.0.0",
    externallyManaged = true
)
public class SpringMessageFormatter implements MessageFormatter {
    @Override
    public String format(String value) {
        return value;
    }
}
```

With `jext-spring` on the classpath and `ApplicationContextProvider` initialized, a plain `ExtensionManager` can retrieve that bean through the `SpringExtensionLoader`.

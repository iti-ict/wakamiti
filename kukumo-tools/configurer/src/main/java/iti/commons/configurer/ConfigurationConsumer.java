/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.configurer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class helps to apply a specific configuration to the given object
 */
public class ConfigurationConsumer<T> {

    public static <T> ConfigurationConsumer<T> of(Configuration configuration, T subject) {
        return new ConfigurationConsumer<>(configuration,subject);
    }

    private ConfigurationConsumer(Configuration configuration, T subject) {
        /*avoid public instantiation*/
        this.subject = subject;
        this.configuration = configuration;
    }

    private final T subject;
    private final Configuration configuration;



    public <P> ConfigurationConsumer<T> ifPresent(
            String property,
            Class<P> propertyClass,
            BiConsumer<T,? super P> consumer
    ) {
        configuration.get(property,propertyClass).ifPresent(toConsumer(consumer));
        return this;
    }


    public <P> ConfigurationConsumer<T> orDefault(
            String property,
            Class<P> propertyClass,
            P defaultValue,
            BiConsumer<T,? super P> consumer
    ) {
        consumer.accept(subject,configuration.get(property,propertyClass).orElse(defaultValue));
        return this;
    }


    public <P> ConfigurationConsumer<T> orGetDefault(
            String property,
            Class<P> propertyClass,
            Supplier<? extends P> defaultValue,
            BiConsumer<T,P> consumer
    ) {
        consumer.accept(subject,configuration.get(property,propertyClass).orElseGet(defaultValue));
        return this;
    }



    private <P> Consumer<P> toConsumer(BiConsumer<T,P> biconsumer) {
        return p -> biconsumer.accept(subject, p);
    }


}

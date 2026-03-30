/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation allows classes to be used as a data source for a configuration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface AnnotatedConfiguration {

    /** Pairs of [key,value] that defines the configuration */
    Property[] value() default {};

    /**
     * Optional path/URI to an external configuration resource.
     * <p>
     * Supported formats are the same than in {@link ConfigurationFactory},
     * such as {@code .yaml}, {@code .yml}, {@code .json}, {@code .xml}
     * and {@code .properties}.
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code classpath:test-conf.yaml}</li>
     *     <li>{@code src/test/resources/wakamiti.yaml}</li>
     *     <li>{@code file:/opt/wakamiti/wakamiti.yaml}</li>
     * </ul>
     */
    String path() default "";

    /**
     * Optional prefix to apply over the external file loaded via {@link #path()}.
     * <p>
     * If set, only the inner section is imported (equivalent to {@link Configuration#inner(String)}).
     */
    String pathPrefix() default "";

}

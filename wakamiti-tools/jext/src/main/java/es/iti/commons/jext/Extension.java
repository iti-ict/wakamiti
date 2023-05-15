/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.commons.jext;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ServiceLoader;


/** This annotation allows to mark a class as an extension managed by the * {@link ExtensionManager}. * <p> * Notice that any class not annotated with {@link Extension} will not be * managed in spite of implementing or extending the {@link ExtensionPoint} * class. * </p> *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Extension {

    static int NORMAL_PRORITY = 5;


    /** The provider (organization, package, group, etc.) of the extension */
    String provider() default "none";


    /** The name of the extension */
    String name();


    /**
     * The version of the extension in form of
     * {@code <majorVersion>.<minorVersion>}
     */
    String version() default "1.0";


    /**
     * The class name of the extension point that is extended.
     * <p>
     * If this field is not provided and the extension class implements directly
     * the extension point class, it will automatically infer the value as the
     * qualified name of the extension point class. Notice that, if the extension
     * point class uses generic parameters, the inference mechanism will not
     * work, so clients must provide the name of the class directly in those
     * cases.
     * </p>
     */
    String extensionPoint() default "";


    /**
     * The minimum version of the extension point that is extended in form of
     * {@code <majorVersion>.<minorVersion>} .
     * <p>
     * If an incompatible version is used (that is, the major part of the version
     * is different), the extension manager will emit a warning and prevent the
     * extension from loading.
     * </p>
     */
    String extensionPointVersion() default "1.0";


    /**
     * Extensions marked as externally managed will not resolved using the
     * {@link ServiceLoader} mechanism. Instead, custom {@link ExtensionLoader}
     * will be used to retrieve the instance of the extension.
     */
    boolean externallyManaged() default false;


    /**
     * Priority used when extensions collide, the highest value have priority
     * over others.
     */
    int priority() default NORMAL_PRORITY;


    boolean overridable() default true;


    /**
     * The qualified class name of another extension that should be replaced by
     * this extension in case both of them are valid alternatives. It only has
     * effect if the {@link #overridable()} property of the extension to override
     * is set to <code>true</code>.
     */
    String overrides() default "";


}
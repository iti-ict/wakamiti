package iti.commons.jext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * This annotation allows to mark an interface or abstract class as an extension point managed by 
 * the {@link ExtensionManager}.
 * </p>
 * <p>
 * In order to ensure compatibility between the extension point and its extensions, 
 * it is important to maintain correctly the {@link #version()} property. If you are intended 
 * to break backwards compatibility keeping the same package and type name, increment the 
 * major part of the version in order to avoid runtime errors. Otherwise, increment the minor 
 * part of the version in order to state the previous methods are still valid. 
 * </p>
 * @since 1.0.0 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtensionPoint {
    
     /** The version of the extension point in form of {@code <majorVersion>.<minorVersion>} */
    String version() default "1.0";
    
    /** The load strategy used when an extension is requested */
    LoadStrategy loadStrategy() default LoadStrategy.UNDEFINED;
    
}

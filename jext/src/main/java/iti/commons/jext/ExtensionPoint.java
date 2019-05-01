package iti.commons.jext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation allows to mark an interface as an extension point managed by the {@link ExtensionManager}.
 * @since 1.0.0 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtensionPoint {
    
     /** The version of the extension point in form of {@code <majorVersion>.<minorVersion>} */
    String version() default "1.0";
}

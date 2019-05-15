package iti.commons.jext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ServiceLoader;

/**
 * This annotation allows to mark a class as an extension managed by the {@link ExtensionManager}.<br/>
 * <br/>
 * Notice that any class not annotated with {@link Extension} will not be managed in spite of implementing
 * or extending the {@link ExtensionPoint} class. 
 * @since 1.0.0 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Extension {
    
    /** The provider (organization, package, group, etc.) of the extension */
    String provider();
    
    /** The name of the extension */
    String name() default "";
    
    /** The version of the extension in form of {@code <majorVersion>.<minorVersion>} */
    String version() default "1.0";
    
    /** 
     * The class name of the extension point that is extended.<br/><br/>
     * If this field is not provided and the extension class implements directly the extension point class, it will 
     * automatically infer the value as the qualified name of the extension point class. Notice that, if the extension
     * point class uses generic parameters, the inference mechanism will not work, so clients must provide 
     * the name of the class directly in those cases.
     */
    String extensionPoint() default "";
    
    /**
     * The version of the extension point that is extended
     * in form of {@code <majorVersion>.<minorVersion>} 
     * .<br/><br/>
     * This value must be exactly the same of the extension point class loaded. If an incompatible version is used 
     * (that is, the major part of the version is different),
     * the extension manager will emit a warning and prevent the extension from loading.
     */
    String extensionPointVersion() default "1.0";


    /**
     * The class name of another extension for the same extension point. Whenever there is an alternative
     * between this extension and the overriden one, the extension manager will pick this extension.
     */
    String overrides() default "";


    /**
     * Extensions marked as externally managed will not resolved using the {@link ServiceLoader} mechanism.
     * Instead, custom {@link ExtensionLoader} will be used to retrieve the instance of the extension.
     */
    boolean externallyManaged() default false;
}

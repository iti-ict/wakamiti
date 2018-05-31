/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.redefining;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})

public @interface RedefiningOptions {


    /**
     * @return the tag(s) used to declare original scenarios
     */
    String[] sourceTags() default {};


    /**
     * @return the tag(s) used to declare redefined scenarios
     */
    String[] targetTags() default {};


    /**
     * @return return the regexp pattern
     */
    String idTagPattern() default "";


}

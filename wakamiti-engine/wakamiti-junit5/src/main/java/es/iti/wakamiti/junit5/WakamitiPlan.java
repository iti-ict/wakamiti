/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a class as a Wakamiti test plan executed on the JUnit Platform.
 *
 * <p>Any concrete class annotated with this annotation is discovered and
 * executed by the {@link WakamitiTestEngine}. The configuration of the plan is
 * provided through the {@code @AnnotatedConfiguration} annotation (or any other
 * Wakamiti configuration annotation) placed on the same class.</p>
 *
 * <p>Plan classes must not declare JUnit Jupiter {@code @Test},
 * {@code @TestFactory} or {@code @TestTemplate} methods, since Wakamiti
 * manages its own lifecycle and execution flow. Static {@code @BeforeAll} and
 * {@code @AfterAll} methods are honoured and invoked, respectively, before and
 * after the plan execution.</p>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WakamitiPlan {

}

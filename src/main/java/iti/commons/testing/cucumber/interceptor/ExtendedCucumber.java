/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.cucumber.interceptor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;

public class ExtendedCucumber extends ParentRunner<FeatureRunner> {


    private final JUnitReporter jUnitReporter;
    private final List<FeatureRunner> children = new ArrayList<>();
    private final Runtime runtime;

    private final List<CucumberInterceptor> interceptors;


    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws java.io.IOException                         if there is a problem
     * @throws org.junit.runners.model.InitializationError if there is another problem
     */
    public ExtendedCucumber(Class<?> clazz) throws InitializationError, IOException {
        super(clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        runtime = createRuntime(resourceLoader, classLoader, runtimeOptions);

        final JUnitOptions junitOptions = new JUnitOptions(runtimeOptions.getJunitOptions());
        interceptors = createInterceptors(clazz, runtimeOptions);

        final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);

        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict(), junitOptions);
        addChildren(cucumberFeatures);
    }






    /**
     * Create the Runtime. Can be overridden to customize the runtime or backend.
     *
     * @param resourceLoader used to load resources
     * @param classLoader    used to load classes
     * @param runtimeOptions configuration
     * @return a new runtime
     */
    protected Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader,
                                    RuntimeOptions runtimeOptions) {
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        return new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
    }

    @Override
    public List<FeatureRunner> getChildren() {
        return children;
    }


    @Override
    protected Description describeChild(FeatureRunner child) {
        Description description = null;
        for (CucumberInterceptor interceptor : interceptors) {
            description = interceptor.describeChild(child);
            if (description != null) {
                return description;
            }
        }
        return child.getDescription();
    }



    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
        interceptors.forEach(interceptor -> interceptor.beforeRunChild(child,notifier));
        child.run(notifier);
        interceptors.forEach(interceptor -> interceptor.afterRunChild(child,notifier));
    }





    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        jUnitReporter.done();
        jUnitReporter.close();
        runtime.printSummary();
    }



    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        interceptors.forEach(interceptor -> interceptor.beforeAddChildren(cucumberFeatures));
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            FeatureRunner runner = null;
            for (CucumberInterceptor interceptor : interceptors) {
                runner = interceptor.provideFeatureRunner(cucumberFeature, runtime, jUnitReporter);
                if (runner != null) {
                    break;
                }
            }
            if (runner != null) {
                children.add(runner);
            }
        }
        interceptors.forEach(interceptor -> interceptor.afterAddChildren(cucumberFeatures));
    }





    @SuppressWarnings("unchecked")
    protected List<CucumberInterceptor> createInterceptors (Class<?> clazz, RuntimeOptions runtimeOptions) {

        List<CucumberInterceptor> list = new ArrayList<>();
        List<InterceptedBy> interceptorProviders = getAnnotations(clazz, InterceptedBy.class);
        for (InterceptedBy interceptorProvider : interceptorProviders) {
            try {

                Class<? extends CucumberInterceptor> interceptorProviderClass = interceptorProvider.value();
                // get the options type
                Type[] parametrized = parameters(interceptorProviderClass);
                if (parametrized.length == 0) {
                    throw new CucumberException("Type "+interceptorProviderClass.getSimpleName()+" must be parametrized");
                }
                Class<? extends Annotation> optionsClass = (Class<? extends Annotation>) parametrized[0];
                List<? extends Annotation> optionsList = getAnnotations(clazz, optionsClass);

                Constructor<? extends CucumberInterceptor> interceptorProviderConstructor =
                        interceptorProviderClass.getConstructor(RuntimeOptions.class,Annotation.class);
                if (interceptorProviderConstructor == null) {
                    throw new CucumberException("Type "+interceptorProviderClass.getSimpleName()+" must have a constructor with the signature (RuntimeOptions,Annotation)");
                }
                for (Annotation options : optionsList) {
                    list.add( interceptorProviderConstructor.newInstance(runtimeOptions, options) );
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new CucumberException(e);
            }
        }
        return list;
    }




    protected Type[] parameters(Class<?> actualClass) {
        Type genericSuperclass = actualClass.getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            return ((ParameterizedType)genericSuperclass).getActualTypeArguments();
        }
        for (Type genericInterface : actualClass.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType) {
                return ((ParameterizedType)genericInterface).getActualTypeArguments();
            }
        }
        return new Type[0];
    }


    protected <A extends Annotation> List<A> getAnnotations(Class<?> annotatedClass, Class<A> annotationClass) {
        A[] annotations = annotatedClass.getAnnotationsByType(annotationClass);
        List<A> list = Arrays.asList(annotations);
        if (annotatedClass.getSuperclass() != null) {
            list.addAll(getAnnotations(annotatedClass.getSuperclass(), annotationClass));
        }
        return list;
    }


}

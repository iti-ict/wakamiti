/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public abstract class AbstractLocaleSteps<T> implements CucumberSteps {

    protected final ClassLoader classLoader;
    protected final Locale locale;
    protected final String localeDefinitionFile;
    protected final T helper;
    protected final MatchParser matchParser;

    private Map<String,String> stepDefinitions;


    public AbstractLocaleSteps(T helper, String localeDefinitionFile) {
        this(helper, Thread.currentThread().getContextClassLoader(), Locale.getDefault(), localeDefinitionFile);
    }


    public AbstractLocaleSteps(T helper, ClassLoader classLoader, Locale locale, String localeDefinitionFile) {
        this.helper = helper;
        this.stepDefinitions = new HashMap<>();
        this.classLoader = classLoader;
        this.locale = locale;
        this.localeDefinitionFile = localeDefinitionFile;
        this.matchParser = new MatchParser(locale);
        mapLocaleSteps();
        registerSteps();
    }


    protected void map(String key, String localeDefinition) {
        stepDefinitions.put(key,localeDefinition);
    }

    protected String resolve(String key) {
        String stepDefinition = stepDefinitions.get(key);
        if (stepDefinition == null) {
            throw new TestingException("Step definition for key "+key+" not defined");
        }
        return "^"+stepDefinition+"$";
    }

    protected void mapLocaleSteps() {
        ResourceBundle localeDefinitionResource = ResourceBundle.getBundle(localeDefinitionFile, locale, classLoader);
        Enumeration<String> stepDefinitionKeys = localeDefinitionResource.getKeys();
        while (stepDefinitionKeys.hasMoreElements()) {
            String key = stepDefinitionKeys.nextElement();
            map(key,localeDefinitionResource.getString(key));
        }
    }
}

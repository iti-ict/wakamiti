package iti.commons.jext.spring;

import java.util.Collections;

import iti.commons.jext.ExtensionLoader;

/**
 * @author ITI
 * Created by ITI on 15/05/19
 */
public class SpringExtensionLoader implements ExtensionLoader  {

    @Override
    public <T> Iterable<T> load(Class<T> type, ClassLoader loader) {
    	if (ApplicationContextProvider.hasContext()) {
    		return ApplicationContextProvider.applicationContext().getBeansOfType(type).values();
    	} else {
    		return Collections.emptyList();
    	}
    }
}

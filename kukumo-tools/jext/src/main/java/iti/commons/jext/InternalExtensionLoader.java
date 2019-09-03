package iti.commons.jext;

import java.util.ServiceLoader;

/**
 * @author ITI
 * Created by ITI on 2/9/19
 */
public class InternalExtensionLoader implements ExtensionLoader {

    @Override
    public <T> Iterable<T> load(Class<T> type, ClassLoader loader) {
        return ServiceLoader.load(type,loader);
    }

    @Override
    public String toString() {
        return "Built-in extension loader";
    }
}

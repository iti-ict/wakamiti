/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.jext;


import java.util.ServiceLoader;



public class InternalExtensionLoader implements ExtensionLoader {

    @Override
    public <T> Iterable<T> load(Class<T> type, ClassLoader loader) {
        return ServiceLoader.load(type, loader);
    }


    @Override
    public String toString() {
        return "Built-in extension loader";
    }
}

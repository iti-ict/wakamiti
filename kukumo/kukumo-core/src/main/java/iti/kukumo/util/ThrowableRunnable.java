/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.util;


@FunctionalInterface
public interface ThrowableRunnable {

    void run(Object... arguments) throws Exception;

}

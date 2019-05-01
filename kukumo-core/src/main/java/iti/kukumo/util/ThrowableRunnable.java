package iti.kukumo.util;

@FunctionalInterface
public interface ThrowableRunnable {

    void run(Object... arguments) throws Exception;

}

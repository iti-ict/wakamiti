/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;



public class ClasspathAgent {

    public static Instrumentation instrumentation;


    public static void premain(String args, Instrumentation instrumentation) {
        ClasspathAgent.instrumentation = instrumentation;
    }


    public static void agentmain(String args, Instrumentation instrumentation) {
        ClasspathAgent.instrumentation = instrumentation;
    }


    public static void appendJarFile(JarFile file) throws IOException {
        if (instrumentation != null) {
            instrumentation.appendToSystemClassLoaderSearch(file);
        }
    }
}

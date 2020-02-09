/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.maven.fetcher;


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


    public static void appendJarFile(JarFile file) {
        if (instrumentation == null) {
            throw new IllegalStateException(
            "ClasspathAgent was not instrumentialized.\n"+
            "You must include the following in the MANIFEST.MF file of the jar with the main class:\n"+
            "Launcher-Agent-Class: iti.commons.maven.fetcher.ClasspathAgent\n"+
            "Agent-Class: iti.commons.maven.fetcher.ClasspathAgent\n"+
            "Premain-Class: iti.commons.maven.fetcher.ClasspathAgent\n"
            );
        }
        instrumentation.appendToSystemClassLoaderSearch(file);
    }
}

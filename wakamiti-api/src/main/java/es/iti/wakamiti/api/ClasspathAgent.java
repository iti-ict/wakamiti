/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;


/**
 * ClasspathAgent is a Java agent that can be used to add JAR
 * files to the system classpath at runtime.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class ClasspathAgent {

    private ClasspathAgent() {

    }

    /**
     * The Instrumentation instance used for adding JAR files
     * to the classpath.
     */
    private static Instrumentation instrumentation;

    /**
     * Premain method called when the agent is started with the
     * JVM.
     *
     * @param args            The agent arguments.
     * @param instrumentation The Instrumentation instance.
     */
    public static void premain(String args, Instrumentation instrumentation) {
        ClasspathAgent.instrumentation = instrumentation;
    }

    /**
     * Agentmain method called when the agent is started
     * dynamically.
     *
     * @param args            The agent arguments.
     * @param instrumentation The Instrumentation instance.
     */
    public static void agentmain(String args, Instrumentation instrumentation) {
        ClasspathAgent.instrumentation = instrumentation;
    }

    /**
     * Append a JAR file to the system class loader search
     * path.
     *
     * @param file The JAR file to be appended to the classpath.
     * @throws IllegalStateException If the ClasspathAgent was not
     *                               initialized.
     */
    public static void appendJarFile(JarFile file) {
        if (instrumentation == null) {
            throw new IllegalStateException(
                    "ClasspathAgent was not instrumentialized.\n" +
                            "You must include the following in the MANIFEST.MF file of the jar with the main class:\n" +
                            "Launcher-Agent-Class: iti.commons.maven.fetcher.ClasspathAgent\n" +
                            "Agent-Class: iti.commons.maven.fetcher.ClasspathAgent\n" +
                            "Premain-Class: iti.commons.maven.fetcher.ClasspathAgent\n"
            );
        }
        instrumentation.appendToSystemClassLoaderSearch(file);
    }
}
package iti.commons.distribution.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;

public class JarBuilder {

    private final File outputFile;
    private final Manifest manifest;
    private final List<File> directoryContentToAdd = new ArrayList<>();
    private final List<File> filesToAdd = new ArrayList<>();

    private Logger logger = Logger.NONE;


    public JarBuilder(File outputFile) {
        this.outputFile = outputFile;
        this.manifest = new Manifest();
    }

    public JarBuilder addManifestAttribute(String key, String value) {
        manifest.getMainAttributes().putValue(key, value);
        return this;
    }


    public JarBuilder addDirectoryContent(File directory) {
        directoryContentToAdd.add(directory);
        return this;
    }


    public JarBuilder addFile(File file) {
        filesToAdd.add(file);
        return this;
    }


    public JarBuilder setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public void build() throws IOException {
        logger.debug("Creating JAR file "+outputFile+" ...");
        try (var stream = new JarOutputStream(new FileOutputStream(outputFile), manifest)) {
            for (File directory : directoryContentToAdd) {
                for (File nestedFile : directory.listFiles()) {
                    addFileOrDirectory("", nestedFile, stream);
                }
            }
            for (File file : filesToAdd) {
                addFileOrDirectory("", file, stream);
            }
            logger.debug("JAR file succesfully created.");
        }
    }



    private void addFileOrDirectory(String jarPath, File source, JarOutputStream target) throws IOException {
        String jarSubPath = (jarPath.isEmpty() ? source.getName() : jarPath + "/" + source.getName());
        if (source.isDirectory()) {
            for (File nestedFile : source.listFiles()) {
                addFileOrDirectory(jarSubPath, nestedFile, target);
            }
        } else {
            addFile(jarPath, source, target);
        }
    }

    private void addFile(String jarPath, File source, JarOutputStream target) throws IOException {
        String jarSubPath = (jarPath.isEmpty() ? source.getName() : jarPath + "/" + source.getName());
        logger.debug("Jar entry added: "+jarSubPath);
        JarEntry entry = new JarEntry(jarSubPath);
        entry.setTime(source.lastModified());
        target.putNextEntry(entry);
        try (var input = new FileInputStream(source)) {
            IOUtils.copy(input, target);
        }
        target.closeEntry();
    }




}

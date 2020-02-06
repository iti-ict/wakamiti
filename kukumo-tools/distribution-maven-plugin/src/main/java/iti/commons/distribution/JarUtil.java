package iti.commons.distribution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class JarUtil {

    private final Logger logger;

    public JarUtil(Logger logger) {
        this.logger = logger;
    }


    public void extractJar(File jarFile, File targetFolder, Predicate<String> filter) throws IOException {
        try (var jar = new java.util.jar.JarFile(jarFile)) {
            var enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                var jarEntry = enumEntries.nextElement();
                var targetFile = new File(targetFolder + File.separator + jarEntry.getName());
                if (jarEntry.isDirectory() || !filter.test(jarEntry.getName())) {
                    continue;
                }
                Files.createDirectories(targetFile.toPath().getParent());
                logger.finer("Extracting "+targetFile+" ...");
                try (var inputStream = jar.getInputStream(jarEntry);
                     var outputStream = new FileOutputStream(targetFile)) {
                    IOUtils.copy(inputStream, outputStream);
                }
            }
        }
    }



    public void extractJarClasses(File jarFile, File targetFolder) throws IOException {
        extractJar(jarFile,targetFolder,filename->filename.endsWith(".class"));
    }

    public void extractJarAll(File jarFile, File targetFolder) throws IOException {
        extractJar(jarFile,targetFolder,x->true);
    }


    public static File selfJarFile() throws IOException {
        try {
            var location = Distributor.class.getProtectionDomain().getCodeSource().getLocation();
            return Paths.get(location.toURI().getPath()).toFile();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}

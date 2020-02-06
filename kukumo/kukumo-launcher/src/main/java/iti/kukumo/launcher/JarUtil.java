package iti.kukumo.launcher;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarUtil {

    public static Path jarFolder() throws URISyntaxException {
        return Paths.get(
            KukumoLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
        ).getParent();
    }
}

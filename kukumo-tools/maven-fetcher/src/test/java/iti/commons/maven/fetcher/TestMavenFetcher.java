/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.maven.fetcher;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestMavenFetcher {

    private static FileVisitor<Path> deleteFileTree = new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {
            Files.delete(arg0);
            return FileVisitResult.CONTINUE;
        }


        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };


    @Test
    public void testFetcher() throws DependencyCollectionException, ArtifactResolutionException, IOException {
        Path localRepo = Paths.get("target/mvn-repo");
        if (localRepo.toFile().exists()) {
            Files.walkFileTree(localRepo, deleteFileTree);
        }
        new MavenFetcher()
            .localRepositoryPath(localRepo)
            .addRemoteRepository("central", "http://repo1.maven.org/maven2")
            .addRemoteRepository("kukumo", "https://raw.github.com/luiinge/maven-repo/master")
            .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
            .fetchArtifacts(
                new MavenFetchRequest(
                    Arrays.asList("junit:junit:4.12", "iti.kukumo:kukumo-gherkin:0.1.0")
                )
                    .scopes("compile")
            );
    }

}

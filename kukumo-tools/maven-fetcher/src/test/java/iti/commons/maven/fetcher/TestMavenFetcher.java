/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.maven.fetcher;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

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
    public void testFetcher() throws IOException, MavenFetchException {
        Path localRepo = Paths.get("target/mvn-repo");
        if (localRepo.toFile().exists()) {
            Files.walkFileTree(localRepo, deleteFileTree);
        }
        MavenFetchResult results = new MavenFetcher()
            .localRepositoryPath(localRepo)
            .addRemoteRepository("central", "https://repo1.maven.org/maven2")
            .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
            .fetchArtifacts(
                new MavenFetchRequest(
                    Arrays.asList("junit:junit:4.12")
                ).scopes("compile")
            );
        results.allArtifacts().forEach(artifact -> assertTrue(artifact.path().toFile().exists()));
    }

}

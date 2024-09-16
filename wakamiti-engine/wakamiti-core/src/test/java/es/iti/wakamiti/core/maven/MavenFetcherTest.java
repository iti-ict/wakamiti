/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven;


import es.iti.wakamiti.api.util.WakamitiLogger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;


public class MavenFetcherTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(MavenFetcherTest.class);

    private final String mockRepo = Path.of("src","test","resources","mock_maven_repo")
            .toAbsolutePath()
            .toUri()
            .toString();
    private String m2 = Path.of(System.getenv("USERPROFILE"), ".m2", "repository")
            .toAbsolutePath().toUri().toString();
    private Path localRepo;

    @Before
    public void prepareLocalRepo() throws IOException {
        localRepo = Files.createTempDirectory("test");
    }

    @After
    public void cleanLocalRepo() throws IOException {
        Files.walkFileTree(localRepo, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(path);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    public void fetchArtifactWithDependencies() {
        var result = new MavenFetcher()
                .localRepositoryPath(localRepo.toString())
                .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .fetchArtifacts(
                        new MavenFetchRequest("org.apache.maven:maven-artifact:3.9.1").scopes("compile")
                );
        LOGGER.debug("Result: {}", result);
        assertThat(result.artifacts()).containsExactly(
                new FetchedArtifact("org.apache.maven:maven-artifact:3.9.1",
                        new FetchedArtifact("org.codehaus.plexus:plexus-utils:3.5.1"),
                        new FetchedArtifact("org.apache.commons:commons-lang3:3.8.1")
                )
        );
    }

    @Test
    public void fetchArtifactWithExcludedDependencies() {
        var result = new MavenFetcher()
                .localRepositoryPath(localRepo.toString())
                .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .fetchArtifacts(
                        new MavenFetchRequest("org.apache.maven:maven-artifact:3.9.1")
                                .scopes("compile")
                                .excludingArtifacts("org.codehaus.plexus:plexus-utils")
                );
        LOGGER.debug("Result: {}", result);
        assertThat(result.artifacts()).containsExactly(
                new FetchedArtifact("org.apache.maven:maven-artifact:3.9.1",
                        new FetchedArtifact("org.apache.commons:commons-lang3:3.8.1")
                )
        );
    }

    @Test
    public void fetchLatestVersionIfVersionNotSpecified() {
        var result = new MavenFetcher()
                .localRepositoryPath(localRepo.toString())
                .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .fetchArtifacts(
                        new MavenFetchRequest("org.apache.maven:maven-artifact").scopes("compile")
                );
        LOGGER.debug("Result: {}", result);
        assertThat(result.artifacts())
                .anyMatch(it -> it.groupId().equals("org.apache.maven") && it.artifactId().equals("maven-artifact"));
    }

    @Test
    public void fetchArtifactWithRequiredProfile() {
        var result = new MavenFetcher()
                .localRepositoryPath(localRepo.toString())
                .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .fetchArtifacts(
                        new MavenFetchRequest("com.google.guava:guava:32.0.1-jre").scopes("compile", "runtime")
                );
        LOGGER.debug("Result: {}", result);
        assertThat(result.allArtifacts()).size().isGreaterThan(1);
        assertThat(result.allArtifacts()).anyMatch(it -> it.artifactId().equals("failureaccess"));
    }

    @Test
    public void fetchArtifactWithInternalExcludedDependencies() {
        var result = new MavenFetcher()
                .localRepositoryPath(localRepo.toString())
                .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .fetchArtifacts(
                        new MavenFetchRequest("org.apache.maven:maven-aether-provider:3.3.9").scopes("compile", "runtime")
                );
        LOGGER.debug("Result: {}", result);
        assertThat(result.allArtifacts()).noneMatch(it -> it.artifactId().equals("aopalliance"));
    }

    @Test
    public void doNotUserDefaultRemoteRepository() {
        Properties withoutDefaultRepo = new Properties();
        withoutDefaultRepo.setProperty(MavenFetcherProperties.USE_DEFAULT_REMOTE_REPOSITORY,"false");
        var fetcher1 = new MavenFetcher().config(withoutDefaultRepo);
        assertThat(fetcher1.remoteRepositories()).isEmpty();

        Properties withDefaultRepo = new Properties();
        withoutDefaultRepo.setProperty(MavenFetcherProperties.USE_DEFAULT_REMOTE_REPOSITORY,"true");
        var fetcher2 = new MavenFetcher().config(withDefaultRepo);
        assertThat(fetcher2.remoteRepositories()).containsExactly(
                "maven-central (https://repo.maven.apache.org/maven2, default, releases+snapshots)"
        );

        var fetcher3 = new MavenFetcher();
        assertThat(fetcher3.remoteRepositories()).containsExactly(
                "maven-central (https://repo.maven.apache.org/maven2, default, releases+snapshots)"
        );
    }

    @Test
    public void repositoryFormats() {
        assertThat(new MavenFetcher().config(properties(
                MavenFetcherProperties.REMOTE_REPOSITORIES,
                "maven-central=https://repo1.maven.org/maven2"
        ))).isNotNull();
        assertThat(new MavenFetcher().config(properties(
                MavenFetcherProperties.REMOTE_REPOSITORIES,
                "maven-central=https://repo1.maven.org/maven2 [user123_@domain:mypass#123@!.]])]"
        ))).isNotNull();
    }

    @Test
    public void malformedPropertiesThrowError() {
        Assertions.assertThatCode(() -> {
            Properties properties = new Properties();
            properties.setProperty(MavenFetcherProperties.REMOTE_REPOSITORIES,"mock:file://repository");
            new MavenFetcher().config(properties);
        }).hasMessage("Invalid value for property 'remoteRepositories' : Invalid repository value 'mock:file://repository' .\n"+
                "Expected formats are 'id=url' and 'id=url [user:pwd]'");
    }

    @Test
    public void attemptToFetchANonExistingArtifact() {
        var result = new MavenFetcher()
                .localRepositoryPath(localRepo.toString())
                .clearRemoteRepositories()
                .addRemoteRepository(new Repository("mock", mockRepo).priority(0))
                .logger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .fetchArtifacts(
                        new MavenFetchRequest("a:b:1.0").scopes("compile")
                );
        assertThat(result.allArtifacts()).isEmpty();
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors().findAny().map(Exception::getMessage).orElseThrow())
                .isEqualTo("Could not fetch artifact b-1.0.jar");

    }


    private Properties properties(String... pairs) {
        Properties properties = new Properties();
        for (int i = 0; i < pairs.length-1; i+=2) {
            properties.setProperty(pairs[i],pairs[i+1]);
        }
        return properties;
    }

}

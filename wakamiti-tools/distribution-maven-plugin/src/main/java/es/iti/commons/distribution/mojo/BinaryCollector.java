/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.distribution.mojo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import maven.fetcher.MavenFetchException;
import maven.fetcher.MavenFetchRequest;
import maven.fetcher.MavenFetcher;
import org.apache.commons.io.FileUtils;

import es.iti.commons.distribution.JarUtil;


public class BinaryCollector {

    private Logger logger = Logger.NONE;

    private final File binaryFolder;
    private String localRepository = "";
    private String remoteRepositories = "";


    public BinaryCollector(File binaryFolder) {
        this.binaryFolder = binaryFolder;
    }

    public BinaryCollector localRepository(String localRepository) {
        this.localRepository = localRepository;
        return this;
    }


    public BinaryCollector remoteRepositories(String remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
        return this;
    }


    public BinaryCollector logger(Logger logger) {
        this.logger = logger;
        return this;
    }



    public void collectBinaries(List<String> artifacts)
    throws MavenFetchException, IOException, URISyntaxException {

        JarUtil jarUtil = new JarUtil(java.util.logging.Logger.getAnonymousLogger());

            if (binaryFolder.exists()) {
                FileUtils.deleteDirectory(binaryFolder);
            }
            Files.createDirectories(binaryFolder.toPath());

            MavenFetcher fetcher = new MavenFetcher().localRepositoryPath(localRepository);
            for (String remoteRepository : remoteRepositories.split(",")) {
                String[] remoteRepositoryParts = remoteRepository.split("::");
                if (remoteRepositoryParts.length == 2) {
                    fetcher.addRemoteRepository(remoteRepositoryParts[0],remoteRepositoryParts[1]);
                } else {
                    fetcher.addRemoteRepository(remoteRepositoryParts[0],remoteRepositoryParts[2]);
                }
            }

            var fetchedArtifacts = fetcher
                .fetchArtifacts(new MavenFetchRequest(artifacts))
                .allArtifacts()
                .collect(Collectors.toList());

            for (var artifact : fetchedArtifacts) {
                logger.debug("Extracting binaries of "+artifact.path());
                try {
                    jarUtil.extractJarClasses(artifact.path().toFile(),binaryFolder);
                } catch (IOException e) {
                    logger.error("Problem extracting "+artifact.path()+": "+e.getMessage(),e);
                }
            }

            File selfJarFile = JarUtil.selfJarFile();
            try {
                jarUtil.extractJarClasses(selfJarFile,binaryFolder);
            } catch (IOException e) {
                logger.error("Problem extracting "+selfJarFile+": "+e.getMessage(),e);
            }

    }


















}
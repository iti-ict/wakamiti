package iti.commons.distribution.mojo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import iti.commons.distribution.JarUtil;
import iti.commons.maven.fetcher.MavenFetchException;
import iti.commons.maven.fetcher.MavenFetchRequest;
import iti.commons.maven.fetcher.MavenFetcher;

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

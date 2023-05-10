/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.distribution.mojo;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import maven.fetcher.MavenFetchException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.yaml.snakeyaml.Yaml;

import iti.commons.distribution.DistributionSet;


@Mojo(defaultPhase = LifecyclePhase.PACKAGE, name = "generate-installer")
public class DistributionMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project.build.directory}/staging")
    private File stagingDirectory;

    @Parameter(required = true)
    private File distributionDefinition;

    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}-installer.jar")
    private File output;

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File buildDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File buildOutputDirectory;

    @Parameter(defaultValue = "${settings.localRepository}")
    private String localRepository;

    @Parameter(defaultValue = "central::default::https://repo.maven.apache.org/maven2")
    private String remoteRepositories;


    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            getLog().info("Preparing distribution definition file "+distributionDefinition+"...");
            prepareDistributionDefinition();

            getLog().info("Collecting binary dependencies for installer ...");

            File binaryFolder = new File(buildDirectory,"dependency-classes");
            new BinaryCollector(binaryFolder)
            .localRepository(localRepository)
            .remoteRepositories(remoteRepositories)
            .logger(Logger.of(getLog()))
            .collectBinaries(List.of(
                "org.apache.commons:commons-lang3:3.9",
                "commons-cli:commons-cli:1.4",
                "commons-io:commons-io:2.6",
                "org.yaml:snakeyaml:1.21"
            ));

            getLog().info("Creating executable jar "+output+" ...");

            new JarBuilder(output)
                .addManifestAttribute("Manifest-Version", "1.0")
                .addManifestAttribute("Main-Class","iti.commons.distribution.Distributor")
                .addDirectoryContent(stagingDirectory)
                .addDirectoryContent(binaryFolder)
                .setLogger(Logger.of(getLog()))
                .build();


        } catch (IOException | MavenFetchException | URISyntaxException e) {
            throw new MojoExecutionException("Error ocurred: "+e.getMessage(), e);
        }
    }







    private void prepareDistributionDefinition() throws IOException {
        try (var fileStream = new FileInputStream(distributionDefinition)) {
            new Yaml().loadAs(fileStream, DistributionSet.class);
        }
        Files.copy(
            distributionDefinition.toPath(),
            new File(stagingDirectory,distributionDefinition.getName()).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
    }




}
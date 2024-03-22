/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher;


import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.WakamitiFetcher;
import imconfig.Configuration;
import net.harawata.appdirs.AppDirsFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Custom WakamitiFetcher for the WakamitiLauncher application.
 *
 * <p>This class extends WakamitiFetcher and is tailored for the WakamitiLauncher application.
 * It provides methods to fetch and update the classpath based on the specified modules.</p>
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiLauncherFetcher extends WakamitiFetcher {

    private final CliArguments arguments;

    /**
     * Constructs a WakamitiLauncherFetcher instance.
     *
     * @param arguments The command-line arguments provided to the application.
     * @throws URISyntaxException If there is an issue with URI syntax.
     */
    public WakamitiLauncherFetcher(CliArguments arguments) throws URISyntaxException {
        super(WakamitiLauncher.logger(), mavenFetcherConfiguration(arguments));
        this.arguments = arguments;
    }

    /**
     * Retrieves the MavenFetcher configuration based on the provided command-line arguments.
     *
     * @param arguments The command-line arguments provided to the application.
     * @return The configuration for the MavenFetcher.
     * @throws URISyntaxException If there is an issue with URI syntax.
     */
    private static Configuration mavenFetcherConfiguration(CliArguments arguments)
            throws URISyntaxException {
        Path mavenRepo = Paths.get(AppDirsFactory.getInstance()
                .getUserDataDir("wakamiti", "repository", "iti"));
        return Wakamiti.defaultConfiguration()
                .append(arguments.mavenFetcherConfiguration()
                        .appendProperty("localRepository", mavenRepo.toString()))
                .append(arguments.wakamitiConfiguration());
    }

    /**
     * Fetches and updates the classpath based on the specified modules.
     *
     * @return A list of Paths representing the fetched modules.
     * @throws URISyntaxException If there is an issue with URI syntax.
     */
    public List<Path> fetchAndUpdateClasspath() throws URISyntaxException {
        return super.fetch(modulesToFetch(), arguments.mustClean());
    }

    /**
     * Retrieves the list of modules to fetch based on the command-line arguments and configuration.
     *
     * @return A list of module strings.
     * @throws URISyntaxException If there is an issue with URI syntax.
     */
    private List<String> modulesToFetch() throws URISyntaxException {
        Configuration conf = arguments.wakamitiConfiguration();
        List<String> modules = new ArrayList<>();
        modules.addAll(arguments.modules());
        modules.addAll(conf.getList("launcher.modules", String.class));
        return modules;
    }

}
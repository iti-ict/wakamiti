/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import imconfig.Configuration;
import iti.kukumo.core.Kukumo;
import iti.kukumo.core.KukumoFetcher;
import net.harawata.appdirs.AppDirsFactory;



public class KukumoLauncherFetcher extends KukumoFetcher {

    private final CliArguments arguments;


    public KukumoLauncherFetcher(CliArguments arguments) throws URISyntaxException {
        super(
            KukumoLauncher.logger(),
            mavenFetcherConfiguration(arguments)
        );
        this.arguments = arguments;
    }


    private static Configuration mavenFetcherConfiguration(CliArguments arguments)
    throws URISyntaxException {
        Path mavenRepo = Paths.get(
            AppDirsFactory.getInstance().getUserDataDir("kukumo", "repository", "iti")
        );
        return Kukumo.defaultConfiguration().append(
                arguments.mavenFetcherConfiguration()
                        .appendProperty("localRepository", mavenRepo.toString())
        ).append(arguments.kukumoConfiguration());
    }



    public List<Path> fetchAndUpdateClasspath() throws URISyntaxException {
        List<Path> modules = super.fetch(modulesToFetch(), arguments.mustClean());
        super.loadGroovyClasses();
        return modules;
    }




    private List<String> modulesToFetch() throws URISyntaxException {
        Configuration conf = arguments.kukumoConfiguration();
        List<String> modules = new ArrayList<>();
        modules.addAll(arguments.modules());
        modules.addAll(conf.getList("launcher.modules", String.class));
        return modules;
    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.launcher;


import imconfig.Configuration;
import iti.wakamiti.core.Wakamiti;
import iti.wakamiti.core.WakamitiFetcher;
import net.harawata.appdirs.AppDirsFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class WakamitiLauncherFetcher extends WakamitiFetcher {

    private final CliArguments arguments;


    public WakamitiLauncherFetcher(CliArguments arguments) throws URISyntaxException {
        super(
            WakamitiLauncher.logger(),
            mavenFetcherConfiguration(arguments)
        );
        this.arguments = arguments;
    }


    private static Configuration mavenFetcherConfiguration(CliArguments arguments)
    throws URISyntaxException {
        Path mavenRepo = Paths.get(
            AppDirsFactory.getInstance().getUserDataDir("wakamiti", "repository", "iti")
        );
        return Wakamiti.defaultConfiguration().append(
                arguments.mavenFetcherConfiguration()
                        .appendProperty("localRepository", mavenRepo.toString())
        ).append(arguments.wakamitiConfiguration())
        ;
    }



    public List<Path> fetchAndUpdateClasspath() throws URISyntaxException {
        return super.fetch(modulesToFetch(), arguments.mustClean());
    }




    private List<String> modulesToFetch() throws URISyntaxException {
        Configuration conf = arguments.wakamitiConfiguration();
        List<String> modules = new ArrayList<>();
        modules.addAll(arguments.modules());
        modules.addAll(conf.getList("launcher.modules", String.class));
        return modules;
    }

}
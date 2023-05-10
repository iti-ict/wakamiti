/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.launcher;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import imconfig.Configuration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class CliArguments {

    private static final String DEFAULT_CONF_FILE = "wakamiti.yaml";
    private static final String ARG_HELP = "h";
    private static final String ARG_DEBUG = "d";
    private static final String ARG_CLEAN = "c";
    private static final String ARG_FILE = "f";
    private static final String ARG_MODULES = "m";
    private static final String ARG_WAKAMITI_PROPERTY = "K";
    private static final String ARG_MAVEN_PROPERTY = "M";
    public static final String  ARG_LIST = "l";

    private final Options cliOptions;
    private CommandLine cliCommand;


    public CliArguments() {
        this.cliOptions = new Options();
        cliOptions.addOption(ARG_HELP, "help", false, "Show this help screen");
        cliOptions.addOption(ARG_DEBUG, "debug", false, "Show debug information");
        cliOptions.addOption(ARG_CLEAN, "clean", false, "Force any module to be re-gathered");
        cliOptions.addOption(ARG_FILE, "file", true, "Configuration file to use (./wakamiti.yaml by default)");
        cliOptions.addOption(ARG_MODULES, "modules", true, "Comma-separated modules, in format group:artifact:version");
        cliOptions.addOption(
            Option.builder(ARG_WAKAMITI_PROPERTY)
            .argName("wakamitiProperty=value")
            .numberOfArgs(2)
            .valueSeparator('=')
            .desc("Set a Wakamiti-specific property")
            .build()
        );
        cliOptions.addOption(
            Option.builder(ARG_MAVEN_PROPERTY)
            .argName("mavenFetcherProperty=value")
            .numberOfArgs(2)
            .valueSeparator('=')
            .desc("Set a MavenFetcher-specific property")
            .build()
        );
        cliOptions.addOption(ARG_LIST,"list", false, "Show all available modules");
    }


    public CliArguments parse(String[] args) throws ParseException {
        CommandLineParser cliParser = new DefaultParser();
        this.cliCommand = cliParser.parse(cliOptions, args, false);
        return this;
    }


    public void printUsage() {
        new HelpFormatter().printHelp("wakamiti [options]", cliOptions);
    }


    public Configuration wakamitiConfiguration() throws URISyntaxException {
        Properties properties = cliCommand.getOptionProperties(ARG_WAKAMITI_PROPERTY);
        String confFile = cliCommand.getOptionValue(ARG_FILE, DEFAULT_CONF_FILE);
        return buildConfiguration(confFile,properties,"wakamiti");
    }


    public Configuration mavenFetcherConfiguration() throws URISyntaxException {
        Properties properties = cliCommand.getOptionProperties(ARG_MAVEN_PROPERTY);
        String confFile = cliCommand.getOptionValue(ARG_FILE, DEFAULT_CONF_FILE);
        return buildConfiguration(confFile,properties,"mavenFetcher");
    }

    public boolean mustClean() {
        return cliCommand.hasOption(ARG_CLEAN);
    }

    public boolean isDebugActive() {
        return cliCommand.hasOption(ARG_DEBUG);
    }

    public boolean isHelpActive() {
        return cliCommand.hasOption(ARG_HELP);
    }

    public boolean isSshowContributionsEnabled() {
        return cliCommand.hasOption(ARG_LIST);
    }

    public List<String> modules() {
        return cliCommand.hasOption(ARG_MODULES) ?
            Arrays.asList(cliCommand.getOptionValue(ARG_MODULES, "").split(",")) :
            List.of();
    }

    private Configuration buildConfiguration(
        String confFileName,
        Properties arguments,
        String qualifier
    ) throws URISyntaxException {
        Path launcherProperties = JarUtil.jarFolder().resolve("launcher.properties");
        Path projectProperties = Paths.get(confFileName);
        Configuration launcherConf = (Files.exists(launcherProperties))
            ? Configuration.factory().fromPath(launcherProperties).inner(qualifier)
            : Configuration.factory().empty();
        Configuration projectConf = (Files.exists(projectProperties))
            ? Configuration.factory().fromPath(projectProperties).inner(qualifier)
            : Configuration.factory().empty();
        Configuration argumentConf = Configuration.factory().fromProperties(arguments);
        return launcherConf.append(projectConf).append(argumentConf);
    }


    @Override
    public String toString() {
        return Stream.of(cliCommand.getOptions())
            .map(opt->opt.getOpt()+"="+opt.getValuesList())
            .collect(Collectors.joining(", ","arguments: {","}"));
    }






}
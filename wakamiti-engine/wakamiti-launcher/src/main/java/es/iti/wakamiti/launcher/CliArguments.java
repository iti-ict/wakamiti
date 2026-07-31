/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher;


import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;


/**
 * Class representing command-line arguments for the WakamitiLauncher.
 *
 * <p>This class provides methods to parse and access command-line arguments related to the WakamitiLauncher.
 * It uses Apache Commons CLI library for parsing command-line options and provides methods to retrieve
 * Wakamiti-specific and MavenFetcher-specific configurations.</p>
 */
public class CliArguments {

    /** Short option used to request the list of available Wakamiti modules. */
    public static final String ARG_LIST = "l";
    /** Short option used to select the ISO 639-1 execution language. */
    public static final String ARG_LANGUAGE = "L";

    private static final String DEFAULT_CONF_FILE = "wakamiti.yaml";
    private static final String ARG_HELP = "h";
    private static final String ARG_DEBUG = "d";
    private static final String ARG_CLEAN = "c";
    private static final String ARG_FILE = "f";
    private static final String ARG_MODULES = "m";
    private static final String ARG_NO_EXECUTION = "n";
    private static final String ARG_WAKAMITI_PROPERTY = "K";
    private static final String ARG_MAVEN_PROPERTY = "M";
    private final Options cliOptions;
    private CommandLine cliCommand;

    /**
     * Creates the command-line model and registers every launcher option.
     * <p>
     * Call {@link #parse(String...)} before reading option-dependent values.
     */
    public CliArguments() {
        this.cliOptions = new Options();
        cliOptions.addOption(ARG_HELP, "help", false, "Show this help screen");
        cliOptions.addOption(ARG_DEBUG, "debug", false, "Show debug information");
        cliOptions.addOption(ARG_CLEAN, "clean", false, "Force any module to be re-gathered");
        cliOptions.addOption(ARG_FILE, "file", true, "Configuration file to use (./wakamiti.yaml by default)");
        cliOptions.addOption(ARG_MODULES, "modules", true, "Comma-separated modules, in format group:artifact:version");
        cliOptions.addOption(ARG_NO_EXECUTION, "dry-run", false, "Generates report without execution");
        cliOptions.addOption(ARG_LANGUAGE, "language", true, "ISO 639-1 language code");

        cliOptions.addOption(
                Option.builder(ARG_WAKAMITI_PROPERTY)
                        .argName("wakamitiProperty=value")
                        .numberOfArgs(2)
                        .valueSeparator('=')
                        .desc("Set a Wakamiti-specific property")
                        .get()
        );
        cliOptions.addOption(
                Option.builder(ARG_MAVEN_PROPERTY)
                        .argName("mavenFetcherProperty=value")
                        .numberOfArgs(2)
                        .valueSeparator('=')
                        .desc("Set a MavenFetcher-specific property")
                        .get()
        );
        cliOptions.addOption(ARG_LIST, "list", false, "Show all available modules");
    }

    /**
     * Parses the given command-line arguments.
     *
     * @param args The command-line arguments to parse.
     * @return The CliArguments instance for method chaining.
     * @throws ParseException If a parsing exception occurs.
     */
    public CliArguments parse(
            String... args
    ) throws ParseException {
        CommandLineParser cliParser = new DefaultParser();
        this.cliCommand = cliParser.parse(cliOptions, args, false);
        return this;
    }

    /**
     * Prints the usage information for the command-line options.
     */
    public void printUsage() {
        new HelpFormatter().printHelp("wakamiti [options]", cliOptions);
    }

    /**
     * Builds launcher-side configuration for the {@code wakamiti} namespace.
     *
     * @return effective Wakamiti configuration
     * @throws URISyntaxException when launcher location cannot be resolved
     */
    public Configuration wakamitiConfiguration() throws URISyntaxException {
        Properties properties = cliCommand.getOptionProperties(ARG_WAKAMITI_PROPERTY);
        properties.setProperty(WakamitiConfiguration.DRY_RUN, String.valueOf(isNoExecution()));
        String confFile = cliCommand.getOptionValue(ARG_FILE, DEFAULT_CONF_FILE);
        return buildConfiguration(confFile, properties, "wakamiti");
    }

    /**
     * Builds launcher-side configuration for the {@code mavenFetcher}
     * namespace.
     *
     * @return effective Maven fetcher configuration
     * @throws URISyntaxException when launcher location cannot be resolved
     */
    public Configuration mavenFetcherConfiguration() throws URISyntaxException {
        Properties properties = cliCommand.getOptionProperties(ARG_MAVEN_PROPERTY);
        String confFile = cliCommand.getOptionValue(ARG_FILE, DEFAULT_CONF_FILE);
        return buildConfiguration(confFile, properties, "mavenFetcher");
    }

    /**
     * Checks if the clean option is specified in the command-line arguments.
     *
     * @return {@code true} if the clean option is specified, {@code false} otherwise.
     */
    public boolean mustClean() {
        return cliCommand.hasOption(ARG_CLEAN);
    }

    /**
     * Checks if the debug option is specified in the command-line arguments.
     *
     * @return {@code true} if the debug option is specified, {@code false} otherwise.
     */
    public boolean isDebugActive() {
        return cliCommand.hasOption(ARG_DEBUG);
    }

    /**
     * Checks if the help option is specified in the command-line arguments.
     *
     * @return {@code true} if the help option is specified, {@code false} otherwise.
     */
    public boolean isHelpActive() {
        return cliCommand.hasOption(ARG_HELP);
    }

    /**
     * Checks if the showContributions option is specified in the command-line arguments.
     *
     * @return {@code true} if the showContributions option is specified, {@code false} otherwise.
     */
    public boolean isShowContributionsEnabled() {
        return cliCommand.hasOption(ARG_LIST);
    }

    /**
     * Checks if the noExecution option is specified in the command-line arguments.
     *
     * @return {@code true} if the noExecution option is specified, {@code false} otherwise.
     */
    public boolean isNoExecution() {
        return cliCommand.hasOption(ARG_NO_EXECUTION);
    }

    /**
     * Retrieves the list of modules specified in the command-line arguments.
     *
     * @return The list of modules, or an empty list if not specified.
     */
    public List<String> modules() {
        return cliCommand.hasOption(ARG_MODULES)
                ? Arrays.asList(cliCommand.getOptionValue(ARG_MODULES, "").split(","))
                : List.of();
    }

    /**
     * Returns the parsed value of an option.
     *
     * @param key short or long option name, such as {@link #ARG_LANGUAGE}
     * @return option value, or an empty string when the option was not supplied
     */
    public String getValue(
            String key
    ) {
        return cliCommand.getOptionValue(key, "");
    }

    /**
     * Merges configuration sources for one launcher qualifier.
     * <p>
     * Merge precedence is:
     * launcher.properties < project file < command-line properties.
     * Missing files are ignored.
     * </p>
     *
     * @param confFileName project configuration file path
     * @param arguments    command-line properties for the qualifier
     * @param qualifier    top-level configuration namespace to extract
     * @return merged configuration for the qualifier
     * @throws URISyntaxException when launcher folder cannot be resolved
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Stream.of(cliCommand.getOptions())
                .map(opt -> opt.getOpt() + "=" + opt.getValuesList())
                .collect(Collectors.joining(", ", "arguments: {", "}"));
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher;


import es.iti.wakamiti.api.imconfig.Configuration;
import org.apache.commons.cli.*;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Class representing command-line arguments for the WakamitiLauncher.
 *
 * <p>This class provides methods to parse and access command-line arguments related to the WakamitiLauncher.
 * It uses Apache Commons CLI library for parsing command-line options and provides methods to retrieve
 * Wakamiti-specific and MavenFetcher-specific configurations.</p>
 */
public class CliArguments {

    public static final String ARG_LIST = "l";
    public static final String ARG_API_DOCS = "D";
    public static final String ARG_AI_TOKEN = "t";
    public static final String ARG_FEATURE_GENERATION_PATH = "p";

    private static final String DEFAULT_CONF_FILE = "wakamiti.yaml";
    private static final String ARG_HELP = "h";
    private static final String ARG_DEBUG = "d";
    private static final String ARG_CLEAN = "c";
    private static final String ARG_FILE = "f";
    private static final String ARG_MODULES = "m";
    private static final String ARG_WAKAMITI_PROPERTY = "K";
    private static final String ARG_MAVEN_PROPERTY = "M";
    private static final String ARG_AI = "a";
    private final Options cliOptions;
    private CommandLine cliCommand;

    public CliArguments() {
        this.cliOptions = new Options();
        cliOptions.addOption(ARG_HELP, "help", false, "Show this help screen");
        cliOptions.addOption(ARG_DEBUG, "debug", false, "Show debug information");
        cliOptions.addOption(ARG_CLEAN, "clean", false, "Force any module to be re-gathered");
        cliOptions.addOption(ARG_FILE, "file", true, "Configuration file to use (./wakamiti.yaml by default)");
        cliOptions.addOption(ARG_MODULES, "modules", true, "Comma-separated modules, in format group:artifact:version");

        cliOptions.addOption(ARG_AI, "ai", false, "Activate feature generator mode");
        cliOptions.addOption(ARG_API_DOCS, "apiDocs", false, "Api docs url or json file");
        cliOptions.addOption(ARG_AI_TOKEN, "token", false, "Token for chat-gpt");
        cliOptions.addOption(ARG_FEATURE_GENERATION_PATH, "path", false, "Feature Generator path");

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
        cliOptions.addOption(ARG_LIST, "list", false, "Show all available modules");
    }

    /**
     * Parses the given command-line arguments.
     *
     * @param args The command-line arguments to parse.
     * @return The CliArguments instance for method chaining.
     * @throws ParseException If a parsing exception occurs.
     */
    public CliArguments parse(String... args) throws ParseException {
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
     * Retrieves the Wakamiti-specific configuration based on the parsed command-line arguments.
     *
     * @return The Wakamiti-specific configuration.
     * @throws URISyntaxException If a URI syntax exception occurs.
     */
    public Configuration wakamitiConfiguration() throws URISyntaxException {
        Properties properties = cliCommand.getOptionProperties(ARG_WAKAMITI_PROPERTY);
        String confFile = cliCommand.getOptionValue(ARG_FILE, DEFAULT_CONF_FILE);
        return buildConfiguration(confFile, properties, "wakamiti");
    }

    /**
     * Retrieves the MavenFetcher-specific configuration based on the parsed command-line arguments.
     *
     * @return The MavenFetcher-specific configuration.
     * @throws URISyntaxException If a URI syntax exception occurs.
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
     * Checks if the feature generator options are specified in the command-line arguments.
     *
     * @return {@code true} if the feature generator options are specified, {@code false} otherwise.
     */
    public boolean isFeatureGeneratorEnabled() {
        return cliCommand.hasOption(ARG_AI)
                && cliCommand.hasOption(ARG_API_DOCS)
                && cliCommand.hasOption(ARG_AI_TOKEN)
                && cliCommand.hasOption(ARG_FEATURE_GENERATION_PATH);
    }

    /**
     * Retrieves the list of modules specified in the command-line arguments.
     *
     * @return The list of modules, or an empty list if not specified.
     */
    public List<String> modules() {
        return cliCommand.hasOption(ARG_MODULES) ?
                Arrays.asList(cliCommand.getOptionValue(ARG_MODULES, "").split(",")) :
                List.of();
    }

    public String getValue(String key) {
        return cliCommand.getOptionValue(key, "");
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
package iti.kukumo.launcher;

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

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;

public class CliArguments {

    private static final String DEFAULT_CONF_FILE = "kukumo.yaml";
    private static final String ARG_HELP = "h";
    private static final String ARG_DEBUG = "d";
    private static final String ARG_CLEAN = "c";
    private static final String ARG_FILE = "f";
    private static final String ARG_MODULES = "m";
    private static final String ARG_KUKUMO_PROPERTY = "K";
    private static final String ARG_MAVEN_PROPERTY = "M";

    private final Options cliOptions;
    private final ConfigurationBuilder configurationBuilder;
    private CommandLine cliCommand;


    public CliArguments() {
        this.cliOptions = new Options();
        cliOptions.addOption(ARG_HELP, "help", false, "Show this help screen");
        cliOptions.addOption(ARG_DEBUG, "debug", false, "Show debug information");
        cliOptions.addOption(ARG_CLEAN, "clean", false, "Force any module to be re-gathered");
        cliOptions.addOption(ARG_FILE, "file", true, "Configuration file to use (./kukumo.yaml by default)");
        cliOptions.addOption(ARG_MODULES, "modules", true, "Comma-separated modules, in format group:artifact:version");
        cliOptions.addOption(
            Option.builder(ARG_KUKUMO_PROPERTY)
            .argName("kukumoProperty=value")
            .numberOfArgs(2)
            .valueSeparator('=')
            .desc("Set a Kukumo-specific property")
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
        this.configurationBuilder = ConfigurationBuilder.instance();
    }


    public CliArguments parse(String[] args) throws ParseException {
        CommandLineParser cliParser = new DefaultParser();
        this.cliCommand = cliParser.parse(cliOptions, args, false);
        return this;
    }


    public void printUsage() {
        new HelpFormatter().printHelp("kukumo [options]", cliOptions);
    }


    public Configuration kukumoConfiguration() throws URISyntaxException {
        Properties properties = cliCommand.getOptionProperties(ARG_KUKUMO_PROPERTY);
        String confFile = cliCommand.getOptionValue(ARG_FILE, DEFAULT_CONF_FILE);
        return buildConfiguration(confFile,properties,"kukumo");
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
            ? configurationBuilder.buildFromPath(launcherProperties).inner(qualifier)
            : configurationBuilder.empty();
        Configuration projectConf = (Files.exists(projectProperties))
            ? configurationBuilder.buildFromPath(projectProperties).inner(qualifier)
            : configurationBuilder.empty();
        Configuration argumentConf = configurationBuilder.buildFromProperties(arguments);
        return launcherConf.append(projectConf).append(argumentConf);
    }


    @Override
    public String toString() {
        return Stream.of(cliCommand.getOptions())
            .map(opt->opt.getOpt()+"="+opt.getValue())
            .collect(Collectors.joining(", ","arguments: {","}"));
    }






}

package iti.kukumo.lsp;

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

public class CliArguments {

    private static final String ARG_HELP = "h";
    private static final String ARG_PORT = "p";
    private static final String ARG_KUKUMO_PROPERTY = "K";
    private static final String ARG_MAVEN_PROPERTY = "M";


    private final Options cliOptions;
    private CommandLine cliCommand;


    public CliArguments() {
        this.cliOptions = new Options();
        cliOptions.addOption(ARG_HELP, "help", false, "Show this help screen");
        cliOptions.addOption(ARG_PORT, "port", true, "Port to run the LSP server (any free port if not specified)");
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
    }


    public CliArguments parse(String[] args) throws ParseException {
        CommandLineParser cliParser = new DefaultParser();
        this.cliCommand = cliParser.parse(cliOptions, args, false);
        return this;
    }


    public void printUsage() {
        new HelpFormatter().printHelp("kukumo [options]", cliOptions);
    }


    public Configuration kukumoConfiguration() {
        Properties properties = cliCommand.getOptionProperties(ARG_KUKUMO_PROPERTY);
        return Configuration.fromProperties(properties);
    }


    public Configuration mavenFetcherConfiguration() {
        Properties properties = cliCommand.getOptionProperties(ARG_MAVEN_PROPERTY);
        return Configuration.fromProperties(properties);
    }


    public boolean isHelpActive() {
        return cliCommand.hasOption(ARG_HELP);
    }


    public int port() {
        return Integer.parseInt(cliCommand.getOptionValue(ARG_PORT, "0"));
    }


    @Override
    public String toString() {
        return Stream.of(cliCommand.getOptions())
            .map(opt->opt.getOpt()+"="+opt.getValue())
            .collect(Collectors.joining(", ","arguments: {","}"));
    }






}

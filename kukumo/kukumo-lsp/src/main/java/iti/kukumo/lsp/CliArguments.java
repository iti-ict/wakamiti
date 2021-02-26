package iti.kukumo.lsp;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliArguments {

    private static final String ARG_HELP = "h";
    private static final String ARG_POSITION_BASE = "b";
    private static final String ARG_TCP_SERVER = "tcp";
    private static final String ARG_PORT = "p";

    private final Options cliOptions;
    private CommandLine cliCommand;


    public CliArguments() {
        this.cliOptions = new Options();
        cliOptions.addOption(ARG_HELP, "help", false, "Show this help screen");
        cliOptions.addOption(ARG_TCP_SERVER, false, "Run a TCP using the given port (multiclient)");
        cliOptions.addOption(ARG_PORT, "port", true, "Port used when running TCP server");
        cliOptions.addOption(Option.builder(ARG_POSITION_BASE)
            .argName("position-base")
            .numberOfArgs(1)
            .required(false)
            .desc("Base of text position ranges (0 or 1). [0 by default]")
            .build()
        );
    }


    public CliArguments parse(String[] args) throws ParseException {
        CommandLineParser cliParser = new DefaultParser();
        this.cliCommand = cliParser.parse(cliOptions, args, false);
        return this;
    }


    public void printUsage() {
        new HelpFormatter().printHelp(
    		"",
    		"Open a Kukmo Language Server using the standard input and output channels.\n"+
    		"LSP clients can use this to open a process directly without requiring a whole server setting.\n\n"+
    		"Alternatively, you can run the process as a TCP server, that will provide several connections\n"+
    		"via TCP sockets.",
    		cliOptions,
    		""
		);
    }



    public boolean isHelpActive() {
        return cliCommand.hasOption(ARG_HELP);
    }


    public int positionBase() {
        return Integer.parseInt(cliCommand.getOptionValue(ARG_POSITION_BASE, "0"));
    }


    public boolean isTcpServer() {
    	return cliCommand.hasOption(ARG_TCP_SERVER);
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

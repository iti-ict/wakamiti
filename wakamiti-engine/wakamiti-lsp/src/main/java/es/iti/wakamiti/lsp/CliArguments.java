/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp;


import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Parses CLI options for the Wakamiti LSP process.
 */
public class CliArguments {

    private static final String ARG_HELP = "h";
    private static final String ARG_POSITION_BASE = "b";
    private static final String ARG_TCP_SERVER = "tcp";
    private static final String ARG_PORT = "p";
    private static final String ARG_DEBUG = "d";

    private final Options cliOptions;
    private CommandLine cliCommand;

    /**
     * Creates the supported command-line option model.
     */
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
        cliOptions.addOption(ARG_DEBUG, "debug", false, "Enable debug logs");
    }

    /**
     * Parses command-line arguments and stores the resulting option state.
     *
     * @param args raw process arguments
     * @return this parsed argument object
     * @throws ParseException when an option or value is invalid
     */
    public CliArguments parse(
            String[] args
    ) throws ParseException {
        CommandLineParser cliParser = new DefaultParser();
        this.cliCommand = cliParser.parse(cliOptions, args, false);
        return this;
    }

    /**
     * Prints usage for both standard-input/output and TCP server modes.
     */
    public void printUsage() {
        new HelpFormatter().printHelp(
                "",
                "Open a Kukmo Language Server using the standard input and output channels.\n"
                        + "LSP clients can use this to open a process directly without requiring a whole server setting.\n\n"
                        + "Alternatively, you can run the process as a TCP server, that will provide several connections\n"
                        + "via TCP sockets.",
                cliOptions,
                ""
        );
    }

    /**
     * Indicates whether help output was requested.
     *
     * @return {@code true} when {@code --help} or {@code -h} is present
     */
    public boolean isHelpActive() {
        return cliCommand.hasOption(ARG_HELP);
    }

    /**
     * Returns the coordinate base used for text ranges.
     *
     * @return {@code 0} by default, or the explicitly supplied base
     * @throws NumberFormatException when the provided value is not numeric
     */
    public int positionBase() {
        return Integer.parseInt(cliCommand.getOptionValue(ARG_POSITION_BASE, "0"));
    }

    /**
     * Indicates whether the language server should listen on TCP instead of
     * standard input/output.
     *
     * @return {@code true} in TCP mode
     */
    public boolean isTcpServer() {
        return cliCommand.hasOption(ARG_TCP_SERVER);
    }

    /**
     * Returns the requested TCP listening port.
     *
     * @return the configured port, or {@code 0} for automatic allocation
     * @throws NumberFormatException when the provided value is not numeric
     */
    public int port() {
        return Integer.parseInt(cliCommand.getOptionValue(ARG_PORT, "0"));
    }

    /**
     * Indicates whether verbose diagnostic logging is enabled.
     *
     * @return {@code true} when the debug option is present
     */
    public boolean debugEnabled() {
        return cliCommand.hasOption(ARG_DEBUG);
    }

    @Override
    public String toString() {
        return Stream.of(cliCommand.getOptions())
                .map(opt -> opt.getOpt() + "=" + opt.getValue())
                .collect(Collectors.joining(", ", "arguments: {", "}"));
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.distribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.yaml.snakeyaml.Yaml;

import iti.commons.distribution.oshandler.OsHandler;

public class Distributor {

    private static final Pattern envPattern = Pattern.compile("\\%([^\\%]*)\\%;");


    public static void main(String[] args) throws ParseException {
        Options cliOptions = new Options();
        cliOptions.addOption("h", "help", false, "Show this help screen");
        cliOptions.addOption("f", "force", false, "Force clean actions (for non-interactive execution)");
        cliOptions.addOption("v", "verbose", false, "Show additional information");
        CommandLineParser cliParser = new DefaultParser();
        CommandLine cliCommand = cliParser.parse(cliOptions, args);
        System.setProperty("java.util.logging.SimpleFormatter.format","%5$s %6$s%n");
        Logger logger = logger(cliCommand.hasOption("v"));
        new Distributor(logger).execute(cliCommand,cliOptions);
    }


    private static Logger logger(boolean verbose) {
        System.setProperty("java.util.logging.SimpleFormatter.format","%5$s %6$s%n");
        Logger logger = Logger.getAnonymousLogger();
        if (verbose) {
            ConsoleHandler handler = new ConsoleHandler();
            handler.setLevel(Level.FINE);
            logger.addHandler(handler);
            logger.setLevel(Level.FINE);
            logger.setUseParentHandlers(false);
        }
        return logger;
    }



    private final Logger logger;
    private final OsHandler osHandler;
    private boolean force;



    public Distributor(Logger logger) {
        this.logger = logger;
        this.osHandler = OsHandler.forCurrentOs(logger);
        if (this.osHandler == null) {
            logger.severe("This installer is not implemented for the current system "+OsHandler.info());
            System.exit(-1);
        }
    }


    public void execute(CommandLine cliCommand, Options cliOptions) {
        try {
            if (cliCommand.hasOption("h")) {
                new HelpFormatter().printHelp("java -jar kukumo-installer.jar <options>", cliOptions);
                System.exit(0);
            }
            var distributionSet = obtainDistributionSet();
            var platformDistribution = locateMatchPlaformDistribution(distributionSet);
            this.force = cliCommand.hasOption("f");

            if (platformDistribution.isEmpty()) {
                logger.log(Level.SEVERE,"No platform distribution matches the current system.");
                System.exit(1);
            } else {
                execute(distributionSet,platformDistribution.get());
            }
        } catch (AccessDeniedException e) {
            logger.log(Level.SEVERE,"Superuser privileges are required to perform the installation");
            System.exit(1);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE,"File not found: "+e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Unexpected error!",e);
            System.exit(1);
        }

    }



    private void execute(
        DistributionSet distributionSet,
        PlatformDistribution platformDistribution
    ) throws IOException {

        String applicationName = distributionSet.getApplicationName();
        if (applicationName == null) {
            applicationName = "software";
        }
        logger.log(Level.INFO, "Installing "+applicationName+" in system "+OsHandler.info()+"...");
        Path temporaryFolder = Files.createTempDirectory("install");
        Files.setPosixFilePermissions(temporaryFolder, Set.of(
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_WRITE
        ));
        File selfJarFile = JarUtil.selfJarFile();
        logger.fine("Extracting "+selfJarFile.getName()+" into temporary folder "+temporaryFolder+"...");
        new JarUtil(logger).extractJarAll(selfJarFile,temporaryFolder.toFile());


        if (platformDistribution.getFileSet() != null) {
            for (var fileSet : platformDistribution.getFileSet()) {
                try {
                    copyFiles(fileSet,temporaryFolder);
                } catch (IOException e) {
                    logger.log(Level.SEVERE,"Problem copying files: "+ e.toString());
                }
            }
        }

        if (platformDistribution.getEnvironmentVariables() != null) {
            osHandler.registerEnvironmentVariable(platformDistribution.getEnvironmentVariables());
        }

        FileUtils.deleteDirectory(temporaryFolder.toFile());
        logger.log(Level.INFO, "Installation completed.");
    }







    private void copyFiles(FileSet fileSet, Path sourceFolder) throws IOException {
        Path destination = Paths.get(replaceEnv(fileSet.getDestinationFolder()));
        logger.fine("Copying files to "+destination+" ...");
        if (destination.toFile().exists() && fileSet.isClean() && askConfirmationDeleteDirectory(destination)) {
            FileUtils.cleanDirectory(destination.toFile());
        } else {
            Files.createDirectories(destination);
        }
        PermissionSetter.set(destination, fileSet.getAccess());

        if (fileSet.getFiles() != null) {
            for (String file : fileSet.getFiles()) {
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+sourceFolder+"/"+file);
                Set<Path> selection = selectFiles(sourceFolder,matcher);
                for (Path selected : selection) {
                    Path target = destination.resolve(sourceFolder.relativize(selected));
                    logger.fine("Copying "+target+"...");
                    if (!target.toFile().exists()) {
                        Files.createDirectories(target.getParent());
                        PermissionSetter.set(target.getParent(), fileSet.getAccess());
                    }
                    Files.copy(selected, target, StandardCopyOption.REPLACE_EXISTING);
                    PermissionSetter.set(target, fileSet.getAccess());
                }
            }
        }
    }



    private Set<Path> selectFiles(Path file, PathMatcher matcher) throws IOException {
        return Files.find(file, Integer.MAX_VALUE, (path,att)->matcher.matches(path)).collect(Collectors.toSet());
    }


    private boolean askConfirmationDeleteDirectory(Path destination) {
        if (force) {
            return true;
        }
        try (Scanner scanner = new Scanner(System.in)) {
            for (;;) {
                System.out.println("Delete all content in directory "+destination+" (Y/n)?");
                String response = scanner.nextLine();
                if (response.isEmpty() || "y".equalsIgnoreCase(response)) {
                    return true;
                } else if ("n".equals(response)) {
                    System.out.println("Installation aborted by user.");
                    System.exit(1);
                }
            }
        }
    }




    private DistributionSet obtainDistributionSet() throws IOException {
        Yaml yaml = new Yaml();
        try (var input = ClassLoader.getSystemResourceAsStream("distribution.yaml")) {
            return yaml.loadAs(input,DistributionSet.class);
        }
    }


    private Optional<PlatformDistribution> locateMatchPlaformDistribution(DistributionSet distributionSet) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,"Searching a valid distribution for current system: "+OsHandler.info());
        }
        return distributionSet.getDistributions().stream()
        .filter(PlatformDistribution::osMatchesSystem)
        .findFirst();
    }




    private String replaceEnv(String string) {
        String result = string;
        Matcher matcher = envPattern.matcher(string);
        while (matcher.find()) {
            String property = matcher.group(1);
            result = result.replace("%"+property+"%",SystemUtils.getEnvironmentVariable(property, ""));
        }
        return result;
    }










}
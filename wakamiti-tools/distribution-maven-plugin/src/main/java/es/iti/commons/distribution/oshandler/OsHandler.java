/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.distribution.oshandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SystemUtils;



public abstract class OsHandler {

    public interface UserPrincipalOperation {
        void perform(UserPrincipal userPrincipal) throws AccessDeniedException;
    }


    public static OsHandler forCurrentOs(Logger logger) {
        if (SystemUtils.IS_OS_UNIX) {
            return new UnixHandler(logger);
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return new WindowsHandler(logger);
        } else {
            return null;
        }
    }


    protected final Logger logger;

    protected OsHandler(Logger logger) {
        this.logger = logger;
    }



    public void registerEnvironmentVariable(Map<String, String> environmentVariables) {
        try {
            logger.info("Registering environment variables...");
            environmentVariables.forEach(
                    (key,value)->logger.info(key+"="+value)
             );
            performRegisterEnvVarirable(environmentVariables);
        } catch (IOException | RuntimeException e) {
            logger.log(Level.WARNING,"Cannot register environment variables: "+e.toString());
            logger.log(Level.WARNING,"Please set the above variables manually.");
        }
    }


    protected abstract void performRegisterEnvVarirable(Map<String, String> variables) throws IOException;


    protected void execute(String... command) {
        String fullCommand = Stream.of(command).collect(Collectors.joining(" ", "'", "'"));
        try {
            logger.fine("Running command: "+fullCommand);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            logger.severe("Error running command "+fullCommand);
            throw new RuntimeException(e);
        }
    }


    public static String info() {
        return "[os="+SystemUtils.OS_NAME+",version="+SystemUtils.OS_VERSION+",architecture="+SystemUtils.OS_ARCH+"]";
    }


    protected void appendLinesToFile(File file, String... lines) throws IOException {
        List<String> allLines = Files.readAllLines(file.toPath());
        List<String> linesToWrite = Stream.of(lines).filter(Predicate.not(allLines::contains)).collect(Collectors.toList());
        if (!linesToWrite.isEmpty()) {
            logger.fine("Adding lines to "+file+" ...");
            linesToWrite.forEach(logger::fine);
            Files.write(
                Paths.get(file.toURI()),
                linesToWrite,
                StandardOpenOption.APPEND
            );
            execute("sh",file.toString());
        }
    }







}
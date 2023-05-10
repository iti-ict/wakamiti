/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.distribution.oshandler;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.SystemUtils;

public class UnixHandler extends OsHandler {


    private static final File USER_PROFILE_FILE = new File(SystemUtils.getUserHome(),".bashrc");
    private static final File GLOBAL_PROFILE_FILE = new File("/etc/profile");

    protected UnixHandler(Logger logger) {
        super(logger);
    }


    @Override
    protected void performRegisterEnvVarirable(Map<String, String> variables) throws IOException {
        String[] lines = variables.entrySet().stream()
            .map(e->"export "+e.getKey()+"="+e.getValue())
            .toArray(String[]::new);
        appendLinesToFile(GLOBAL_PROFILE_FILE, lines);
        for (String line : lines) {
            execute("sh","-c",line);
        }
    }

}
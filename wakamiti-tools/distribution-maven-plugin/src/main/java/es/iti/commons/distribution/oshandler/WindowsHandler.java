/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.distribution.oshandler;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class WindowsHandler extends OsHandler {

    protected WindowsHandler(Logger logger) {
        super(logger);
    }


    @Override
    protected void performRegisterEnvVarirable(Map<String, String> variables) throws IOException {
        variables.forEach((key,value) -> execute("setx",key,value));
    }


}
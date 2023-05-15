/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.distribution.mojo;

import org.apache.maven.plugin.logging.Log;

public interface Logger {

    void debug(String message);
    void error(String message, Exception e);


    static Logger NONE = new Logger() {

        @Override
        public void debug(String message) {
            System.out.println(message);
        }

        @Override
        public void error(String message, Exception e) {
            System.out.println(message);
            e.printStackTrace();
        }


    };



    static Logger of(Log log) {
        return new Logger() {

            @Override
            public void error(String message, Exception e) {
                log.error(message, e);
            }

            @Override
            public void debug(String message) {
                log.debug(message);
            }
        };
    }
}
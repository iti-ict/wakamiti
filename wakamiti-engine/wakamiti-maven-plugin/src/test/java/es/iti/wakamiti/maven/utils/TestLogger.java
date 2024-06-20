/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven.utils;


import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestLogger implements Log {

    private static final Logger LOGGER = LoggerFactory.getLogger("es.iti.wakamiti");

    @Override
    public boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

    @Override
    public void debug(CharSequence charSequence) {
        LOGGER.debug(charSequence.toString());
    }

    @Override
    public void debug(CharSequence charSequence, Throwable throwable) {
        LOGGER.debug(charSequence.toString(), throwable);
    }

    @Override
    public void debug(Throwable throwable) {
        LOGGER.debug("", throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return LOGGER.isInfoEnabled();
    }

    @Override
    public void info(CharSequence charSequence) {
        LOGGER.info(charSequence.toString());
    }

    @Override
    public void info(CharSequence charSequence, Throwable throwable) {
        LOGGER.info(charSequence.toString(), throwable);
    }

    @Override
    public void info(Throwable throwable) {
        LOGGER.info("", throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return LOGGER.isWarnEnabled();
    }

    @Override
    public void warn(CharSequence charSequence) {
        LOGGER.warn(charSequence.toString());
    }

    @Override
    public void warn(CharSequence charSequence, Throwable throwable) {
        LOGGER.warn(charSequence.toString(), throwable);
    }

    @Override
    public void warn(Throwable throwable) {
        LOGGER.warn("", throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return LOGGER.isErrorEnabled();
    }

    @Override
    public void error(CharSequence charSequence) {
        LOGGER.error(charSequence.toString());
    }

    @Override
    public void error(CharSequence charSequence, Throwable throwable) {
        LOGGER.error(charSequence.toString(), throwable);
    }

    @Override
    public void error(Throwable throwable) {
        LOGGER.error("", throwable);
    }
}

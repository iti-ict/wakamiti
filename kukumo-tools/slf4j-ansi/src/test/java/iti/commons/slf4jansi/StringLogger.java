/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.slf4jansi;

import org.slf4j.helpers.SubstituteLogger;

public class StringLogger extends SubstituteLogger {

    private final StringBuilder contents = new StringBuilder();

    public StringLogger() {
        super("TEST", null, true);
    }

    @Override
    public void info(String msg) {
        contents.append(msg).append("\n");
    }

    @Override
    public void info(String format, Object arg) {
        contents.append(replace(format,arg)).append("\n");
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        contents.append(replace(format,arg1,arg2)).append("\n");
    }


    @Override
    public void warn(String msg) {
        contents.append(msg).append("\n");
    }

    @Override
    public void warn(String format, Object arg) {
        contents.append(replace(format,arg)).append("\n");
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        contents.append(replace(format,arg1,arg2)).append("\n");
    }


    @Override
    public void error(String msg) {
        contents.append(msg).append("\n");
    }

    @Override
    public void error(String format, Object arg) {
        contents.append(replace(format,arg)).append("\n");
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        contents.append(replace(format,arg1,arg2)).append("\n");
    }

    private String replace(String format, Object...args) {
        for (Object arg : args) {
            format = format.replaceFirst("\\{\\}",arg.toString());
        }
        return format;
    }



    public String getContent() {
        return contents.toString();
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }
}
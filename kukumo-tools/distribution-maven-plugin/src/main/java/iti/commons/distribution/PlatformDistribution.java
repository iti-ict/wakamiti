/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.distribution;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;

public class PlatformDistribution {

    private String os;
    private Map<String,String> environmentVariables;
    private List<FileSet> fileSet;


    public boolean osMatchesSystem() {
        String property = "IS_OS_"+os.replace(" ","_").toUpperCase();
        try {
            Field field = SystemUtils.class.getField(property);
            return field.getBoolean(SystemUtils.class);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return false;
        }
    }


    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public void setFileSet(List<FileSet> fileSet) {
        this.fileSet = fileSet;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public List<FileSet> getFileSet() {
        return fileSet;
    }

    public String getOs() {
        return os;
    }
}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.commons.jext;


public class ExtensionVersion {

    private final int major;
    private final int minor;


    public ExtensionVersion(String version) {
        String[] str = version.split("\\.");
        if (str.length != 2) {
            throw new IllegalArgumentException("Not valid version number " + version);
        }
        try {
            this.major = Integer.parseInt(str[0]);
            this.minor = Integer.parseInt(str[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Not valid version number " + version + " (" + e.getMessage() + ")"
            );
        }
    }


    public int major() {
        return major;
    }


    public int minor() {
        return minor;
    }


    public boolean isCompatibleWith(ExtensionVersion otherVersion) {
        return (major == otherVersion.major && minor >= otherVersion.minor);
    }


    @Override
    public String toString() {
        return major + "." + minor;
    }

}
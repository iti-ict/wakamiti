/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


/**
 * The {@code ExtensionVersion} class represents a version number in the format
 * {@code <major>.<minor>}. It provides methods for extracting the major and minor
 * components, checking compatibility with another version, and generating a
 * string representation.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
class ExtensionVersion {

    private final int major;
    private final int minor;

    ExtensionVersion(String version) {
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

    /**
     * Checks if the current version is compatible with another version.
     *
     * @param otherVersion The other version to compare with.
     * @return {@code true} if compatible, {@code false} otherwise.
     */
    public boolean isCompatibleWith(ExtensionVersion otherVersion) {
        return (major == otherVersion.major && minor >= otherVersion.minor);
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

}
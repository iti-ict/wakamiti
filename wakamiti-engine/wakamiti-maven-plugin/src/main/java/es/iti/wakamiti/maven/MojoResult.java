/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import org.apache.maven.plugin.AbstractMojoExecutionException;

import java.util.Optional;


/**
 * Utility class to store and retrieve the result of plugin execution.
 */
public class MojoResult {

    private static AbstractMojoExecutionException error;

    private MojoResult() {
        // prevents instantiation
    }

    /**
     * Retrieves the error occurred during plugin execution.
     *
     * @return An optional containing the error, if any.
     */
    public static Optional<AbstractMojoExecutionException> getError() {
        return Optional.ofNullable(error);
    }

    /**
     * Sets the error that occurred during plugin execution.
     *
     * @param e The error that occurred during plugin execution.
     */
    public static void setError(AbstractMojoExecutionException e) {
        MojoResult.error = e;
    }
}

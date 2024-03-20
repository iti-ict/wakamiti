/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.Optional;


/**
 * Maven plugin mojo for controlling failed executions.
 */
@Mojo(name = "control", defaultPhase = LifecyclePhase.VERIFY)
public class FailedControlMojo extends AbstractMojo {


    /**
     * Executes the plugin.
     *
     * @throws MojoExecutionException If an unexpected problem occurs during execution.
     * @throws MojoFailureException   If the plugin fails during execution.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Optional<AbstractMojoExecutionException> error = MojoResult.getError();
        if (error.isPresent()) {
            AbstractMojoExecutionException ex = error.get();
            if (ex instanceof MojoExecutionException) {
                throw (MojoExecutionException) ex;
            }
            if (ex instanceof MojoFailureException) {
                throw (MojoFailureException) ex;
            }
        }
    }

}

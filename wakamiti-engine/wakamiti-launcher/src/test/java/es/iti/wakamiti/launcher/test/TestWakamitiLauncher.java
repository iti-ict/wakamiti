/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.launcher.test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import es.iti.wakamiti.launcher.CliArguments;
import imconfig.Configuration;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;


public class TestWakamitiLauncher {

    @Test
    public void testArguments() throws Exception {
        String args =
            "-modules es.iti.wakamiti:wakamiti.core:0.1.0,rest " +
            "-f /var/lib/wakamiti.conf " +
            "-Krest.host=localhost " +
            "-MremoteRepositories=http://maven.com ";
        CliArguments result = new CliArguments().parse(args.split(" "));
        assertThat(result.modules()).containsExactly( "es.iti.wakamiti:wakamiti.core:0.1.0", "rest");
        assertThat(result.wakamitiConfiguration().asMap()).contains(entry("rest.host", "localhost"));
        assertThat(result.mavenFetcherConfiguration().asMap())
            .contains(entry("remoteRepositories", "http://maven.com"));
    }

    @Test
    public void testConfigurationFile() throws ParseException, URISyntaxException {
        Configuration configuration = new CliArguments().parse("-f", "src/test/resources/wakamiti.yaml")
                .wakamitiConfiguration();
        assertThat(configuration.get("something", String.class)).isPresent();
        assertThat(configuration.get("something", String.class))
                .contains("this_is_a_large_value_with_special_characters");
    }

}
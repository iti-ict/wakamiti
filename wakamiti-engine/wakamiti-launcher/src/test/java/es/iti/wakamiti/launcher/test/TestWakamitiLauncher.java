/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher.test;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.launcher.CliArguments;
import es.iti.wakamiti.launcher.WakamitiLauncher;
import es.iti.wakamiti.launcher.WakamitiLauncherFetcher;
import es.iti.wakamiti.launcher.WakamitiRunner;
import org.apache.commons.cli.ParseException;
import org.assertj.core.util.Files;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;


public class TestWakamitiLauncher {

    @Test
    public void testArguments() throws Exception {
        String args =
                "-modules es.iti.wakamiti:wakamiti.core:0.1.0,rest " +
                        "-f /var/lib/wakamiti.conf " +
                        "-Krest.host=localhost " +
                        "-MremoteRepositories=http://maven.com ";
        CliArguments result = new CliArguments().parse(args.split(" "));
        assertThat(result.modules()).containsExactly("es.iti.wakamiti:wakamiti.core:0.1.0", "rest");
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

    @Test
    public void testLogger() {
        String args = "--no-execution -KresourceTypes=gherkin -Klog.path=target -Klog.level=info";
        try (MockedConstruction<WakamitiLauncherFetcher> mockPaymentService =
                     Mockito.mockConstruction(WakamitiLauncherFetcher.class, (mock, context) ->
                             when(mock.fetchAndUpdateClasspath()).thenReturn(new ArrayList<>()))
        ) {
            try (MockedConstruction<WakamitiRunner> mockRunner =
                         Mockito.mockConstruction(WakamitiRunner.class, (mock, context) ->
                                 when(mock.run(anyBoolean())).thenReturn(true))) {
                WakamitiLauncher.main(args.split(" "));
            }
        }

        Logger logger = WakamitiLogger.forClass(this.getClass());
        logger.info("Test");
        assertThat(new File("target").listFiles())
                .anyMatch(file -> file.getName().matches("wakamiti-\\d+\\.log")
                        && Files.linesOf(file, Charset.defaultCharset()).stream().anyMatch(l -> l.contains("Test")));
    }

}
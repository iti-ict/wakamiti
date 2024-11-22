/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;


import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.internal.Util;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class TestUtil {

    private static final Logger LOGGER = WakamitiLogger.forClass(TestUtil.class);

    @Test
    public void testZonedDateTimeToZonedDateTime() {
        String date = "2024-11-11T12:00:00.000+08:00";
        ZoneId zone = ZoneId.of("Europe/Berlin");
        String result = Util.toZoneId(date, zone);
        LOGGER.debug("Result: {}", result);
        assertThat(result).isEqualTo("2024-11-11T05:00:00");
    }

    @Test
    public void testDateTimeToZonedDateTime() {
        int hour = 5;
        int sum = 8;
        int diff = ZoneOffset.ofHours(8).getTotalSeconds()/100/60;
        String date = String.format("2024-11-11T%02d:00:00.000", hour);
        ZoneId zone = ZoneId.of(String.format("UTC+%02d", sum));
        String result = Util.toZoneId(date, zone);
        LOGGER.debug("Result: {}", result);
        assertThat(result).isEqualTo(String.format("2024-11-11T%02d:00:00", diff+sum));
    }

    @Test
    public void testFindFiles() throws IOException, URISyntaxException {
        Set<Path> paths = Util.findFiles(Path.of(resource(".")), "**/wakamiti.html");
        LOGGER.debug("Results: {}", paths);
        assertThat(paths).hasSize(1)
                .anyMatch(p -> p.getFileName().toString().endsWith("wakamiti.html"));
    }

    private URI resource(String path) throws URISyntaxException {
        return ClassLoader.getSystemClassLoader().getResource(path).toURI();
    }
}

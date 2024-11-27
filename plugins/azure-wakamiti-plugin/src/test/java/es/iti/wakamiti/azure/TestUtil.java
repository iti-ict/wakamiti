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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.TimeZone;

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
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String date = "2024-11-11T05:00:00.000";
        ZoneId zone = ZoneId.of("UTC+08:00");
        String result = Util.toZoneId(date, zone);
        LOGGER.debug("Result: {}", result);
        assertThat(result).isEqualTo("2024-11-11T13:00:00");
    }

    @Test
    public void testFindFiles() {
        boolean result = Util.match(Path.of("other/wakamiti.html"), "**/*.html");
        LOGGER.debug("Results: {}", result);
        assertThat(result).isTrue();
    }

}

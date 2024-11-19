package es.iti.wakamiti.azure;

import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.internal.Util;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.ZoneId;
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
        String date = "2024-11-11T05:00:00.000";
        ZoneId zone = ZoneId.of("UTC+8");
        String result = Util.toZoneId(date, zone);
        LOGGER.debug("Result: {}", result);
        assertThat(result).isEqualTo("2024-11-11T12:00:00");
    }

    @Test
    public void testFindFiles() throws IOException, URISyntaxException {
        Set<Path> paths = Util.findFiles(Path.of(resource(".")), "**/wakamiti.html");
        LOGGER.debug("Results: {}", paths);
        assertThat(paths).hasSize(1)
                .anyMatch(p -> p.getFileName().toString().endsWith("wakamiti.html"));
    }

    @Test
    public void testParseNameWithoutId() {
        Pair<String, String> parsed = Util.parseNameAndId("Wakamiti test plan");
        Assert.assertTrue(parsed.key().equals("Wakamiti test plan"));
        Assert.assertTrue(parsed.value() == null);
    }

    @Test
    public void testParseNameWithId() {
        Pair<String, String> parsed = Util.parseNameAndId("[423423] Wakamiti test plan");
        Assert.assertTrue(parsed.key().equals("Wakamiti test plan"));
        Assert.assertTrue(parsed.value().equals("423423"));
    }

    private URI resource(String path) throws URISyntaxException {
        return ClassLoader.getSystemClassLoader().getResource(path).toURI();
    }
}

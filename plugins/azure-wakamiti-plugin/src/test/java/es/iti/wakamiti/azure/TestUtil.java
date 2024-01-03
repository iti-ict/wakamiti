package es.iti.wakamiti.azure;

import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.internal.Util;
import org.junit.Assert;
import org.junit.Test;

public class TestUtil {


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
}

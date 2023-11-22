package es.iti.wakamiti.azure;

import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.internal.Util;
import org.junit.Assert;
import org.junit.Test;

public class TestUtil {


    @Test
    public void testParseNameAndId() {
        Pair<String, String> parsed = Util.parseNameAndId("Wakamiti test plan");
        Assert.assertTrue(parsed.key().equals("Wakamiti test plan"));
        Assert.assertTrue(parsed.value() == null);
    }
}

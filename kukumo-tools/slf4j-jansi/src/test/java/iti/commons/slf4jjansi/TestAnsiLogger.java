package iti.commons.slf4jjansi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;

public class TestAnsiLogger {


    @Test
    public void testEnabled() {
        System.setProperty(AnsiLogger.ANSI_ENABLED, "true");
        StringLogger string = new StringLogger();
        Logger logger = AnsiLogger.of(string);
        logger.info("@|red red|@ regular @|blue blue|@");
        assertFalse(string.getContent().equals("red regular blue"));
        System.out.println(string.getContent());
    }

    @Test
    public void testDisabled() {
        System.setProperty(AnsiLogger.ANSI_ENABLED, "false");
        StringLogger string = new StringLogger();
        Logger logger = AnsiLogger.of(string);
        logger.info("@|red red|@ regular @|blue blue|@");
        assertTrue(string.getContent().equals("red regular blue"));
        System.out.println(string.getContent());
    }


}
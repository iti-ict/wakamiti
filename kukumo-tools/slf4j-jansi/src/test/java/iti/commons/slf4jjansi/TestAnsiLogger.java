package iti.commons.slf4jjansi;

import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestAnsiLogger {


    @Test
    public void testEnabled() {
        AnsiLogger.setAnsiEnabled(true);
        StringLogger string = new StringLogger();
        Logger logger = AnsiLogger.of(string);
        logger.info("@|red red|@ regular @|blue blue|@");
        assertFalse(string.getContent().equals("red regular blue"));
        System.out.println(string.getContent());
    }

    @Test
    public void testDisabled() {
        AnsiLogger.setAnsiEnabled(false);
        StringLogger string = new StringLogger();
        Logger logger = AnsiLogger.of(string);
        logger.info("@|red red|@ regular @|blue blue|@");
        assertTrue(string.getContent().equals("red regular blue"));
        System.out.println(string.getContent());
    }


}
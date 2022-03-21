/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.slf4jansi;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;

public class TestAnsiLogger {




    @Test
    public void testEnabled() {
        Properties newStyles = new Properties();
        newStyles.put("custom","strike");
        AnsiLogger.setAnsiEnabled(true);
        AnsiLogger.setStyles(newStyles);
        StringLogger string = new StringLogger();
        Logger logger = AnsiLogger.of(string);
        logger.info("this is a nice URI: {uri} , and this: {id} a beautiful id", "http://world.org", 54423);
        logger.warn("this is a nice URI: {uri} , and this: {id} a beautiful id", "http://world.org", 54423);
        logger.error("this is a nice URI: {uri} , and this: {id} a beautiful id", "http://world.org", 54423);
        logger.info("{!important} this is a nice URI: {uri} , and this: {id} a beautiful id", "http://world.org", 54423);
        logger.error("{!important} this is a nice URI: {uri} , and this: {id} a beautiful id", "http://world.org", 54423);
        System.out.println(string.getContent());
        assertTrue(string.getContent().equals(
           "this is a nice URI: [34;4mhttp://world.org[m , and this: [36;1m54423[m a beautiful id\n"+
            "[33;1mthis is a nice URI: [m[34;4mhttp://world.org[m[33;1m , and this: [m[36;1m54423[m[33;1m a beautiful id[m\n"+
            "[31;1mthis is a nice URI: [m[34;4mhttp://world.org[m[31;1m , and this: [m[36;1m54423[m[31;1m a beautiful id[m\n"+
            "[35;1mthis is a nice URI: [m[34;4mhttp://world.org[m[35;1m , and this: [m[36;1m54423[m[35;1m a beautiful id[m\n"+
            "[35;1mthis is a nice URI: [m[34;4mhttp://world.org[m[35;1m , and this: [m[36;1m54423[m[35;1m a beautiful id[m\n"
        ));
    }

    @Test
    public void testDisabled() {
        AnsiLogger.setAnsiEnabled(false);
        StringLogger string = new StringLogger();
        Logger logger = AnsiLogger.of(string);
        logger.info("{error} regular {uri}","red","blue");
        System.out.println(string.getContent());
        assertTrue(string.getContent().equals("red regular blue\n"));
    }


    @Test
    public void testNull() {
        AnsiLogger.setAnsiEnabled(true);
        StringLogger string = new StringLogger();
        Logger logger = AnsiLogger.of(string);
        logger.info(null);
        System.out.println(string.getContent());
        assertTrue(string.getContent().equals("null\n"));
    }


}
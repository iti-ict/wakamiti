/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;


public class RestAssuredLogger {

    private final Logger logger;
    private PrintStream printStream;


    public RestAssuredLogger(Logger logger) {
        this.logger = logger;
    }


    public PrintStream getPrintStream() {
        if (printStream == null) {
            printStream = new PrintStream(new ByteArrayOutputStream(), true) { // true: autoflush must be set!

                @Override
                public void println(String x) {
                    logger.debug(x);
                }
            };
        }

        return printStream;
    }

}

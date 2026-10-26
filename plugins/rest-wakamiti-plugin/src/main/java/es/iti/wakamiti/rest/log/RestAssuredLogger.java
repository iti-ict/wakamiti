/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest.log;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


/**
 * Creates print streams that route Rest-Assured log output through the REST
 * plugin logger.
 * <p>
 * Each stream buffers the written bytes until it is flushed or closed, then
 * emits the resulting message through SLF4J.
 */
public final class RestAssuredLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("es.iti.wakamiti.rest");

    private RestAssuredLogger() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns a UTF-8 print stream that forwards its buffered output to the
     * REST plugin logger.
     * <p>
     * The returned stream flushes automatically. On each flush or close, it
     * logs the remaining message at  {@link Level#DEBUG} when debug logging is
     * enabled, or at {@link Level#INFO} otherwise.
     *
     * @return a new logger-backed print stream
     */
    public static PrintStream getPrintStream() {
        return new PrintStream(new LoggerOutputStream(), true, StandardCharsets.UTF_8);
    }

    /**
     * Buffers bytes written by a print stream and emits complete buffered
     * messages through the REST plugin logger.
     */
    private static final class LoggerOutputStream extends OutputStream {

        private final Level level;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        private LoggerOutputStream() {
            this.level = LOGGER.isDebugEnabled() ? Level.DEBUG : Level.INFO;
        }

        @Override
        public synchronized void write(
                int value
        ) {
            buffer.write(value);
        }

        @Override
        public synchronized void write(
                byte[] bytes,
                int offset,
                int length
        ) {
            if (offset < 0 || length < 0 || offset > bytes.length - length) {
                throw new IndexOutOfBoundsException();
            }
            for (int index = offset; index < offset + length; index++) {
                write(bytes[index]);
            }
        }

        @Override
        public synchronized void flush() {
            emitBuffer();
        }

        @Override
        public synchronized void close() {
            emitBuffer();
        }

        /**
         * Logs the buffered UTF-8 message, excluding a trailing line break,
         * and clears the buffer. Empty messages are ignored.
         */
        private void emitBuffer() {
            String message = buffer.toString(StandardCharsets.UTF_8).replaceAll("\\r?\\n$", "");
            buffer.reset();
            if (!message.isEmpty()) {
                switch (level) {
                    case ERROR:
                        LOGGER.error(message);
                        break;
                    case WARN:
                        LOGGER.warn(message);
                        break;
                    case INFO:
                        LOGGER.info(message);
                        break;
                    case DEBUG:
                        LOGGER.debug(message);
                        break;
                    case TRACE:
                        LOGGER.trace(message);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported log level: " + level);
                }
            }
        }

    }

}

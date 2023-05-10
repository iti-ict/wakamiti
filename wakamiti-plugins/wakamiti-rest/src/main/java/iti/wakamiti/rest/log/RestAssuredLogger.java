/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.rest.log;


import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class RestAssuredLogger implements Filter {

    private static final Logger logger = LoggerFactory.getLogger("iti.wakamiti.rest");
    private static final String HEADER_NAME_AND_VALUE_SEPARATOR = ": ";
    private static PrintStream printStream;

    public static PrintStream getPrintStream() {
        if (printStream == null) {
            printStream = new PrintStream(new ByteArrayOutputStream(), true) { // true: autoflush must be set!

                @Override
                public void println(String x) {
                    logger.info(x);
                }
            };
        }
        return printStream;
    }

    @Override
    public Response filter(
            FilterableRequestSpecification freqs,
            FilterableResponseSpecification freps,
            FilterContext ctx) {
        Response response = ctx.next(freqs, freps);

        final StringBuilder builder = new StringBuilder();
        builder.append(response.statusLine());

        Headers headers = response.headers();
        if (headers.exist()) {
            builder.append(System.lineSeparator());
            builder.append(toString(headers));
        }

        if (ContentType.fromContentType(response.contentType()) != null) {
            String responseBodyToAppend = new Prettifier().getPrettifiedBodyIfPossible(response, response.body());

            if (!isBlank(responseBodyToAppend)) {
                builder.append(System.lineSeparator()).append(System.lineSeparator());
            }
            builder.append(responseBodyToAppend);
        } else {
            builder.append(System.lineSeparator()).append(System.lineSeparator());
            builder.append("[Not readable body] (")
                    .append(response.body().asString().length())
                    .append(")");
        }

        getPrintStream().println(builder);

        return response;
    }

    private String toString(Headers headers) {
        final StringBuilder builder = new StringBuilder();
        for (Header header : headers) {
            builder.append(header.getName())
                    .append(HEADER_NAME_AND_VALUE_SEPARATOR)
                    .append(header.getValue())
                    .append(System.lineSeparator());
        }
        builder.delete(builder.length() - System.lineSeparator().length(), builder.length());
        return builder.toString();
    }
}
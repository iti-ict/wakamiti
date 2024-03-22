/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.event;


import java.time.Instant;


/**
 * The {@code Event} class represents an event that occurs during the execution of a system.
 * Events may have different types and carry associated data. Each event is timestamped with
 * the moment it occurred.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class Event {

    public static final String PLAN_CREATED = "PLAN_CREATED";
    public static final String PLAN_RUN_STARTED = "PLAN_RUN_STARTED";
    public static final String PLAN_RUN_FINISHED = "PLAN_RUN_FINISHED";
    public static final String NODE_RUN_STARTED = "NODE_RUN_STARTED";
    public static final String NODE_RUN_FINISHED = "NODE_RUN_FINISHED";
    public static final String BEFORE_RUN_BACKEND_STEP = "BEFORE_RUN_BACKEND_STEP";
    public static final String AFTER_RUN_BACKEND_STEP = "AFTER_RUN_BACKEND_STEP";
    public static final String BEFORE_WRITE_OUTPUT_FILES = "BEFORE_WRITE_OUTPUT_FILES";
    public static final String AFTER_WRITE_OUTPUT_FILES = "AFTER_WRITE_OUTPUT_FILES";
    public static final String STANDARD_OUTPUT_FILE_WRITTEN = "STANDARD_OUTPUT_FILE_WRITTEN";
    public static final String TEST_CASE_OUTPUT_FILE_WRITTEN = "TEST_CASE_OUTPUT_FILE_WRITTEN";
    public static final String REPORT_OUTPUT_FILE_WRITTEN = "REPORT_OUTPUT_FILE_WRITTEN";

    private final String type;
    private final Object data;
    private final Instant instant;


    public Event(String type, Instant instant, Object data) {
        this.type = type;
        this.data = data;
        this.instant = instant;
    }


    public String type() {
        return type;
    }


    public Object data() {
        return data;
    }


    public Instant instant() {
        return instant;
    }

}
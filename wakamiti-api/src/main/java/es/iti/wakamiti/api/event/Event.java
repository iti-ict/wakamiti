/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
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
 */
public class Event {

    /**
     * Event emitted after a test plan has been assembled and validated.
     * Its payload is a snapshot of the resulting plan.
     */
    public static final String PLAN_CREATED = "PLAN_CREATED";
    /**
     * Event emitted immediately before execution of a test plan starts.
     * Its payload is the plan snapshot at the beginning of the run.
     */
    public static final String PLAN_RUN_STARTED = "PLAN_RUN_STARTED";
    /**
     * Event emitted after every runnable node in a test plan has completed.
     * Its payload is the final plan snapshot, including execution results.
     */
    public static final String PLAN_RUN_FINISHED = "PLAN_RUN_FINISHED";
    /**
     * Event emitted when an individual plan node enters the running state.
     * Its payload is a snapshot of that node before execution.
     */
    public static final String NODE_RUN_STARTED = "NODE_RUN_STARTED";
    /**
     * Event emitted when an individual plan node reaches its finished state.
     * Its payload is a snapshot containing the node result and duration.
     */
    public static final String NODE_RUN_FINISHED = "NODE_RUN_FINISHED";
    /**
     * Event emitted immediately before a backend step implementation is
     * invoked.
     */
    public static final String BEFORE_RUN_BACKEND_STEP = "BEFORE_RUN_BACKEND_STEP";
    /**
     * Event emitted after a backend step invocation finishes, including
     * exceptional completion.
     */
    public static final String AFTER_RUN_BACKEND_STEP = "AFTER_RUN_BACKEND_STEP";
    /**
     * Event delimiting the beginning of an output-generation batch. Observers
     * can use it to acquire resources shared by subsequent file events.
     */
    public static final String BEFORE_WRITE_OUTPUT_FILES = "BEFORE_WRITE_OUTPUT_FILES";
    /**
     * Event delimiting the end of an output-generation batch. Observers can use
     * it to flush and release resources acquired for that batch.
     */
    public static final String AFTER_WRITE_OUTPUT_FILES = "AFTER_WRITE_OUTPUT_FILES";
    /**
     * Event emitted after Wakamiti writes its standard result file. The payload
     * is the {@link java.nio.file.Path} of the generated file.
     */
    public static final String STANDARD_OUTPUT_FILE_WRITTEN = "STANDARD_OUTPUT_FILE_WRITTEN";
    /**
     * Event emitted for each per-test-case output file written. The payload is
     * the {@link java.nio.file.Path} of that file.
     */
    public static final String TEST_CASE_OUTPUT_FILE_WRITTEN = "TEST_CASE_OUTPUT_FILE_WRITTEN";
    /**
     * Event emitted when a reporter produces an output artifact. The payload is
     * the {@link java.nio.file.Path} of the generated report file.
     */
    public static final String REPORT_OUTPUT_FILE_WRITTEN = "REPORT_OUTPUT_FILE_WRITTEN";

    private final String type;
    private final Object data;
    private final Instant instant;

    /**
     * Creates an immutable event envelope.
     *
     * @param type    the event identifier, normally one of the constants
     *                declared by this class
     * @param instant the exact instant at which the event occurred
     * @param data    event-specific payload; may be {@code null} for lifecycle
     *                markers that carry no additional information
     */
    public Event(
            String type,
            Instant instant,
            Object data
    ) {
        this.type = type;
        this.data = data;
        this.instant = instant;
    }

    /**
     * Returns the identifier used by observers to select supported events.
     *
     * @return the event type supplied at construction time
     */
    public String type() {
        return type;
    }

    /**
     * Returns the event-specific payload.
     *
     * @return the payload, whose expected type depends on {@link #type()}, or
     * {@code null} when the event is only a lifecycle marker
     */
    public Object data() {
        return data;
    }

    /**
     * Returns when the event was published.
     *
     * @return the event timestamp
     */
    public Instant instant() {
        return instant;
    }

}

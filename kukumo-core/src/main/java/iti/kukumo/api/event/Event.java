package iti.kukumo.api.event;

import java.time.Instant;

/**
 * @author ITI
 *         Created by ITI on 2/01/19
 */
public class Event<T> {

    public static final String PLAN_CREATED = "PLAN_CREATED";
    public static final String PLAN_RUN_STARTED = "PLAN_RUN_STARTED";
    public static final String PLAN_RUN_FINISHED = "PLAN_RUN_FINISHED";
    public static final String NODE_RUN_STARTED = "NODE_RUN_STARTED";
    public static final String NODE_RUN_FINISHED = "NODE_RUN_FINISHED";

    private final String type;
    private final Instant instant;
    private final T data;


    public Event(String type, Instant instant, T data) {
        this.type = type;
        this.instant = instant;
        this.data = data;
    }


    public String type() {
        return type;
    }

    public Instant instant() {
        return instant;
    }

    public T data() {
        return data;
    }
}

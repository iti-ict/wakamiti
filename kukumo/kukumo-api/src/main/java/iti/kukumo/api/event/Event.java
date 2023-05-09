/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.event;


import iti.kukumo.api.plan.PlanNodeSnapshot;

import java.time.Instant;



public class Event {

    public static final String PLAN_CREATED = "PLAN_CREATED";
    public static final String PLAN_RUN_STARTED = "PLAN_RUN_STARTED";
    public static final String PLAN_RUN_FINISHED = "PLAN_RUN_FINISHED";
    public static final String NODE_RUN_STARTED = "NODE_RUN_STARTED";
    public static final String NODE_RUN_FINISHED = "NODE_RUN_FINISHED";
    public static final String BEFORE_RUN_BACKEND_STEP = "BEFORE_RUN_BACKEND_STEP";
    public static final String AFTER_RUN_BACKEND_STEP = "AFTER_RUN_BACKEND_STEP";

    private final String type;
    private final Object data;


    public Event(String type, Instant instant, PlanNodeSnapshot data) {
        this.type = type;
        this.data = data;
    }


    public String type() {
        return type;
    }


    public Object data() {
        return data;
    }
}
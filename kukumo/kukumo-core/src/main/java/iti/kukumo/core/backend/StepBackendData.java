/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import iti.kukumo.api.datatypes.Argument;
import iti.kukumo.api.plan.PlanNode;


public class StepBackendData {

    private final PlanNode step;
    private final Locale stepLocale;
    private final Locale dataLocale;
    private final RunnableStep runnableStep;
    private final Matcher stepMatcher;
    private final Map<String, Argument> invokingArguments;
    private final Exception exception;


    public StepBackendData(
        PlanNode step,
        Locale stepLocale,
        Locale dataLocale,
        RunnableStep runnableStep,
        Matcher stepMatcher,
        Map<String, Argument> invokingArguments
    ) {
        this.step = step;
        this.stepLocale = stepLocale;
        this.dataLocale = dataLocale;
        this.runnableStep = runnableStep;
        this.stepMatcher = stepMatcher;
        this.invokingArguments = invokingArguments;
        this.exception = null;
    }


    public StepBackendData(PlanNode step, Exception exception) {
        this.step = step;
        this.stepLocale = null;
        this.dataLocale = null;
        this.runnableStep = null;
        this.stepMatcher = null;
        this.invokingArguments = null;
        this.exception = exception;
    }


    public PlanNode step() {
        return step;
    }


    public Locale stepLocale() {
        return stepLocale;
    }


    public Locale dataLocale() {
        return dataLocale;
    }


    public RunnableStep runnableStep() {
        return runnableStep;
    }


    public Matcher stepMatcher() {
        return stepMatcher;
    }


    public Map<String, Argument> invokingArguments() {
        return invokingArguments;
    }


    public Exception exception() {
        return exception;
    }

}
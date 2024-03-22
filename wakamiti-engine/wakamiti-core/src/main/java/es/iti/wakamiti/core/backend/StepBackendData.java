/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.Argument;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * Holds data related to the execution of a step, including the step itself, locales,
 * runnable step instance, step matcher, invoking arguments, exception (if any), and classifier.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class StepBackendData {

    private final PlanNode step;
    private final Locale stepLocale;
    private final Locale dataLocale;
    private final RunnableStep runnableStep;
    private final Matcher stepMatcher;
    private final Map<String, Argument> invokingArguments;
    private final Exception exception;
    private final String classifier;

    /**
     * Constructor for StepBackendData when the step execution is successful.
     *
     * @param step              The plan node representing the step.
     * @param stepLocale        The locale of the step.
     * @param dataLocale        The locale for data-related operations.
     * @param runnableStep      The runnable step instance.
     * @param stepMatcher       The matcher for the step.
     * @param invokingArguments The arguments used for invoking the step.
     * @param classifier        The step classifier.
     */
    public StepBackendData(
            PlanNode step,
            Locale stepLocale,
            Locale dataLocale,
            RunnableStep runnableStep,
            Matcher stepMatcher,
            Map<String, Argument> invokingArguments,
            String classifier
    ) {
        this.step = step;
        this.stepLocale = stepLocale;
        this.dataLocale = dataLocale;
        this.runnableStep = runnableStep;
        this.stepMatcher = stepMatcher;
        this.invokingArguments = invokingArguments;
        this.exception = null;
        this.classifier = classifier;
    }

    /**
     * Constructor for StepBackendData when an exception occurs during step execution.
     *
     * @param step      The plan node representing the step.
     * @param exception The exception thrown during step execution.
     */
    public StepBackendData(PlanNode step, Exception exception) {
        this.step = step;
        this.stepLocale = null;
        this.dataLocale = null;
        this.runnableStep = null;
        this.stepMatcher = null;
        this.invokingArguments = null;
        this.exception = exception;
        this.classifier = null;
    }

    /**
     * Get the plan node representing the step.
     *
     * @return The plan node representing the step.
     */
    public PlanNode step() {
        return step;
    }

    /**
     * Get the locale of the step.
     *
     * @return The locale of the step.
     */
    public Locale stepLocale() {
        return stepLocale;
    }

    /**
     * Get the locale for data-related operations.
     *
     * @return The locale for data-related operations.
     */
    public Locale dataLocale() {
        return dataLocale;
    }

    /**
     * Get the runnable step instance.
     *
     * @return The runnable step instance.
     */
    public RunnableStep runnableStep() {
        return runnableStep;
    }

    /**
     * Get the matcher for the step.
     *
     * @return The matcher for the step.
     */
    public Matcher stepMatcher() {
        return stepMatcher;
    }

    /**
     * Get the invoking arguments used for executing the step.
     *
     * @return The invoking arguments used for executing the step.
     */
    public Map<String, Argument> invokingArguments() {
        return invokingArguments;
    }

    /**
     * Get the exception thrown during step execution (if any).
     *
     * @return The exception thrown during step execution, or null if no exception occurred.
     */
    public Exception exception() {
        return exception;
    }

    /**
     * Get the classifier of the step.
     *
     * @return The classifier of the step.
     */
    public String classifier() {
        return classifier;
    }

}
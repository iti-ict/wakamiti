/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.spring.junit;


import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.springframework.test.context.TestContextManager;

import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;


/**
 * Executes Wakamiti Spring JUnit operations.
 */
public class WakamitiSpringJUnitRunner extends Runner {

    private static final Logger LOGGER = Wakamiti.LOGGER;
    private final WakamitiJUnitRunner wakamitiJUnitRunner;
    private final TestContextManager testContextManager;

    /**
     * Creates a JUnit 4 runner with a prepared Spring test context.
     * <p>
     * Spring prepares this runner instance before the delegate
     * {@link WakamitiJUnitRunner} is created, allowing Wakamiti extensions to
     * discover initialized Spring state during their own bootstrap. Preparation
     * failures are logged so the delegate can still report initialization
     * problems through JUnit.
     *
     * @param configurationClass JUnit/Spring configuration class
     * @throws InitializationError if the delegated Wakamiti runner cannot be
     *                            initialized
     */
    public WakamitiSpringJUnitRunner(
            Class<?> configurationClass
    ) throws InitializationError {
        /** IMPORTANT: TestContext must be prepared before accessing Wakamiti */
        this.testContextManager = createTestContextManager(configurationClass);
        try {
            this.testContextManager.prepareTestInstance(this);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }
        this.wakamitiJUnitRunner = new WakamitiJUnitRunner(configurationClass);
    }

    /**
     * Factory method for the Spring test-context manager.
     * <p>
     * Subclasses may override to customize context manager creation.
     * </p>
     *
     * @param clazz test configuration class
     * @return context manager used by this runner
     */
    protected TestContextManager createTestContextManager(
            Class<?> clazz
    ) {
        return new TestContextManager(clazz);
    }

    /**
     * Exposes the context manager created for this runner instance.
     *
     * @return current test context manager
     */
    protected final TestContextManager getTestContextManager() {
        return this.testContextManager;
    }

    @Override
    public Description getDescription() {
        return wakamitiJUnitRunner.getDescription();
    }

    @Override
    public void run(
            RunNotifier notifier
    ) {
        wakamitiJUnitRunner.run(notifier);
    }

}

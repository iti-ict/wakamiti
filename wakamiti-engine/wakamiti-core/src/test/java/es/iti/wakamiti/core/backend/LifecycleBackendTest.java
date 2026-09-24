/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.annotations.Level;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.util.ThrowableRunnable;


public class LifecycleBackendTest {

    @Test
    public void tearDownExecutesAllOperationsAndAggregatesFailures() {
        List<String> events = new ArrayList<>();
        WakamitiException firstFailure = new WakamitiException("first failure");
        WakamitiException secondFailure = new WakamitiException("second failure");
        LifecycleBackend backend = new LifecycleBackend(
                Configuration.factory().empty(),
                null,
                Map.of(),
                Map.of(Level.PLAN, List.<ThrowableRunnable>of(
                        args -> {
                            events.add("first");
                            throw firstFailure;
                        },
                        args -> {
                            events.add("second");
                            throw secondFailure;
                        },
                        args -> events.add("third")
                )),
                List.of()
        );

        Throwable thrown = catchThrowable(() -> backend.tearDown(Level.PLAN));

        assertThat(events).containsExactly("first", "second", "third");
        assertThat(thrown).isSameAs(firstFailure);
        assertThat(thrown.getSuppressed()).containsExactly(secondFailure);
    }

    @Test
    public void resolvesLastMatchingContributorFromBackendScope() {
        TestStepContributor first = new TestStepContributor();
        TestStepContributor last = new TestStepContributor();
        LifecycleBackend backend = backend(List.of(first, last), Map.of(), Map.of());

        assertThat(backend.getContributor(TestStepContributor.class)).isSameAs(last);
    }

    @Test
    public void lifecycleOperationExposesAndClearsBackendContext() {
        TestStepContributor contributor = new TestStepContributor();
        List<Backend> observedBackends = new ArrayList<>();
        Map<Level, List<ThrowableRunnable>> operations = Map.of(
                Level.FEATURE,
                List.of(args -> observedBackends.add(WakamitiStepRunContext.current().backend()))
        );
        LifecycleBackend backend = backend(List.of(contributor), operations, Map.of());

        backend.setUp(Level.FEATURE);

        assertThat(observedBackends).containsExactly(backend);
        assertThat(WakamitiStepRunContext.current()).isNull();
    }

    private LifecycleBackend backend(
            List<StepContributor> contributors,
            Map<Level, List<ThrowableRunnable>> setUpOperations,
            Map<Level, List<ThrowableRunnable>> tearDownOperations
    ) {
        return new LifecycleBackend(
                Configuration.factory().empty(),
                null,
                contributors,
                setUpOperations,
                tearDownOperations,
                List.of(),
                Locale.ENGLISH
        );
    }

    private static final class TestStepContributor implements StepContributor {

    }

}

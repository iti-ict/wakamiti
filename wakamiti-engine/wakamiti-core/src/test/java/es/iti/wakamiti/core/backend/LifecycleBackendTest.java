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
import java.util.Map;

import org.junit.Test;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.Level;
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

}

/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.Contributor;
import es.iti.wakamiti.api.extensions.DataTypeContributor;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.extensions.PlanBuilder;
import es.iti.wakamiti.api.extensions.PlanTransformer;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.extensions.ResourceType;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.util.Pair;


public class WakamitiContributorsTest {

    @Test
    public void testAllContributors() {
        WakamitiContributors wakamitiContributors = new WakamitiContributors();
        wakamitiContributors.setClassLoaders(getClass().getClassLoader());
        Map<Class<?>, List<Contributor>> allContributors = wakamitiContributors.allContributors();
        assertNotNull(allContributors);
        assertTrue(allContributors.containsKey(ConfigContributor.class));
        assertTrue(allContributors.containsKey(DataTypeContributor.class));
        assertTrue(allContributors.containsKey(EventObserver.class));
        assertTrue(allContributors.containsKey(PlanBuilder.class));
        assertTrue(allContributors.containsKey(PlanTransformer.class));
        assertTrue(allContributors.containsKey(Reporter.class));
        assertTrue(allContributors.containsKey(ResourceType.class));
        assertTrue(allContributors.containsKey(StepContributor.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void extractVersionHandlesTwoDigitMinorValues() throws Exception {
        WakamitiContributors wakamitiContributors = new WakamitiContributors();
        Method extractVersion = WakamitiContributors.class.getDeclaredMethod("extractVersion", String.class);
        extractVersion.setAccessible(true);

        Optional<Pair<Integer, Integer>> parsed = (Optional<Pair<Integer, Integer>>) extractVersion.invoke(
                wakamitiContributors, "2.10.0-SNAPSHOT");

        assertTrue(parsed.isPresent());
        assertEquals(Integer.valueOf(2), parsed.get().key());
        assertEquals(Integer.valueOf(10), parsed.get().value());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void getContributorDelegatesToCurrentBackend() {
        WakamitiContributors wakamitiContributors = new WakamitiContributors();
        TestStepContributor historical = new TestStepContributor();
        TestStepContributor scoped = new TestStepContributor();
        Backend backend = mock(Backend.class);
        when(backend.getContributor(TestStepContributor.class)).thenReturn(scoped);
        wakamitiContributors.addStepContributors(List.of(historical));
        WakamitiStepRunContext.set(new WakamitiStepRunContext(null, backend, null, null));

        try {
            assertSame(scoped, wakamitiContributors.getContributor(TestStepContributor.class));
        } finally {
            WakamitiStepRunContext.clear();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void getContributorKeepsBackendScopeAcrossThreads() throws Exception {
        WakamitiContributors wakamitiContributors = new WakamitiContributors();
        TestStepContributor first = new TestStepContributor();
        TestStepContributor second = new TestStepContributor();
        Backend firstBackend = mock(Backend.class);
        Backend secondBackend = mock(Backend.class);
        when(firstBackend.getContributor(TestStepContributor.class)).thenReturn(first);
        when(secondBackend.getContributor(TestStepContributor.class)).thenReturn(second);
        CountDownLatch ready = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<TestStepContributor> firstResult = executor.submit(() -> resolveInContext(
                    wakamitiContributors, firstBackend, ready
            ));
            Future<TestStepContributor> secondResult = executor.submit(() -> resolveInContext(
                    wakamitiContributors, secondBackend, ready
            ));

            assertSame(first, firstResult.get());
            assertSame(second, secondResult.get());
        } finally {
            executor.shutdownNow();
        }
    }

    @SuppressWarnings("deprecation")
    private TestStepContributor resolveInContext(
            WakamitiContributors wakamitiContributors,
            Backend backend,
            CountDownLatch ready
    ) throws InterruptedException {
        WakamitiStepRunContext.set(new WakamitiStepRunContext(null, backend, null, null));
        ready.countDown();
        try {
            ready.await();
            return wakamitiContributors.getContributor(TestStepContributor.class);
        } finally {
            WakamitiStepRunContext.clear();
        }
    }

    private static final class TestStepContributor implements StepContributor {

    }

}

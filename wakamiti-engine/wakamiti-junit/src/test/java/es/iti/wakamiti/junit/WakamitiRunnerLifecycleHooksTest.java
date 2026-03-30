/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class WakamitiRunnerLifecycleHooksTest {

    private static volatile String currentTestName;

    @After
    public void cleanup() {
        currentTestName = null;
        HookAwareRunner.clearHooks();
    }

    @Test
    public void beforeAndAfterClassHooksAreReportedInsideLifecycleEntries() throws Exception {
        RecordingListener listener = new RecordingListener();
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);

        new WakamitiJUnitRunner(HookAwareRunner.class).run(notifier);

        assertThat(HookAwareRunner.hookContexts).containsExactly(
                "before:beforeClass",
                "after:afterClass"
        );
    }

    @AnnotatedConfiguration({
            @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
            @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/redefining"),
            @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.junit.WakamitiSteps")
    })
    @RunWith(WakamitiJUnitRunner.class)
    public static class HookAwareRunner {

        private static final List<String> hookContexts = new ArrayList<>();

        @BeforeClass
        public static void beforeClassHook() {
            hookContexts.add("before:" + currentTestName);
        }

        @AfterClass
        public static void afterClassHook() {
            hookContexts.add("after:" + currentTestName);
        }

        private static void clearHooks() {
            hookContexts.clear();
        }
    }

    private static class RecordingListener extends RunListener {

        @Override
        public void testStarted(org.junit.runner.Description description) {
            currentTestName = description.getMethodName();
        }

        @Override
        public void testFinished(org.junit.runner.Description description) {
            currentTestName = null;
        }
    }
}

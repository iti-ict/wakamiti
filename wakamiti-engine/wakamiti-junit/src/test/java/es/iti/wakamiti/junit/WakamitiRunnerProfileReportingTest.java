/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class WakamitiRunnerProfileReportingTest {

    @After
    public void cleanupProperties() {
        System.clearProperty(ProfileSelector.PROFILE_PROPERTY);
        System.clearProperty(ProfileSelector.PROFILE_FALLBACK_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_FALLBACK_PROPERTY);
    }

    @Test
    public void profileMismatchIsReportedAsSkippedWithReason() throws Exception {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A");
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        RecordingListener listener = new RecordingListener();
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);

        new WakamitiJUnitRunner(ProfileBRunner.class).run(notifier);

        assertThat(listener.ignoredDescriptions).hasSize(1);
        assertThat(listener.ignoredDescriptions.get(0).getMethodName())
                .contains("does not match active profile(s): A");
        assertThat(listener.startedCount).isZero();
        assertThat(listener.finishedCount).isZero();
    }

    @Profile("B")
    @RunWith(WakamitiJUnitRunner.class)
    public static class ProfileBRunner {
        // no-op test runner class for profile mismatch checks
    }

    private static class RecordingListener extends RunListener {
        int startedCount;
        int finishedCount;
        List<org.junit.runner.Description> ignoredDescriptions = new ArrayList<>();

        @Override
        public void testStarted(org.junit.runner.Description description) {
            startedCount++;
        }

        @Override
        public void testFinished(org.junit.runner.Description description) {
            finishedCount++;
        }

        @Override
        public void testIgnored(org.junit.runner.Description description) {
            ignoredDescriptions.add(description);
        }
    }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ProfileSelectorTest {

    @After
    public void cleanupProperties() {
        System.clearProperty(ProfileSelector.PROFILE_PROPERTY);
        System.clearProperty(ProfileSelector.PROFILE_FALLBACK_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_FALLBACK_PROPERTY);
    }

    @Test
    public void noActiveProfileAndNoStrictRunsEveryClass() {
        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isTrue();
    }

    @Test
    public void activeProfileWithoutStrictRunsCommonAndMatchingOnly() {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A");

        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isFalse();
    }

    @Test
    public void strictModeWithActiveProfileRunsOnlyMatching() {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A");
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isFalse();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isFalse();
    }

    @Test
    public void strictModeWithoutActiveProfileRunsOnlyCommonClasses() {
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isFalse();
    }

    @Test
    public void supportsMultipleActiveProfiles() {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A,B");
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileC.class)).isFalse();
    }

    @Test
    public void fallbackPropertiesAreAlsoSupported() {
        System.setProperty(ProfileSelector.PROFILE_FALLBACK_PROPERTY, "B");
        System.setProperty(ProfileSelector.STRICT_FALLBACK_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isFalse();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isTrue();
    }

    static class WithoutProfile {}

    @Profile("A")
    static class ProfileA {}

    @Profile("B")
    static class ProfileB {}

    @Profile("C")
    static class ProfileC {}
}

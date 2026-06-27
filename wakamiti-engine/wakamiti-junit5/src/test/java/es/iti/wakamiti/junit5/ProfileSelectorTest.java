package es.iti.wakamiti.junit5;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileSelectorTest {

    @AfterEach
    void cleanupProperties() {
        System.clearProperty(ProfileSelector.PROFILE_PROPERTY);
        System.clearProperty(ProfileSelector.PROFILE_FALLBACK_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_FALLBACK_PROPERTY);
    }

    @Test
    void noActiveProfileAndNoStrictRunsEveryClass() {
        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isTrue();
    }

    @Test
    void activeProfileWithoutStrictRunsCommonAndMatchingOnly() {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A");

        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isFalse();
    }

    @Test
    void strictModeWithActiveProfileRunsOnlyMatching() {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A");
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isFalse();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isFalse();
    }

    @Test
    void strictModeWithoutActiveProfileRunsOnlyCommonClasses() {
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(WithoutProfile.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isFalse();
    }

    @Test
    void supportsMultipleActiveProfiles() {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A,B");
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isTrue();
        assertThat(ProfileSelector.isEnabled(ProfileC.class)).isFalse();
    }

    @Test
    void fallbackPropertiesAreAlsoSupported() {
        System.setProperty(ProfileSelector.PROFILE_FALLBACK_PROPERTY, "B");
        System.setProperty(ProfileSelector.STRICT_FALLBACK_PROPERTY, "true");

        assertThat(ProfileSelector.isEnabled(ProfileA.class)).isFalse();
        assertThat(ProfileSelector.isEnabled(ProfileB.class)).isTrue();
    }

    static class WithoutProfile {
    }

    @Profile("A")
    static class ProfileA {
    }

    @Profile("B")
    static class ProfileB {
    }

    @Profile("C")
    static class ProfileC {
    }
}

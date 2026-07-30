/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Resolves whether a JUnit 5 test class is enabled for the active profile set.
 * <p>
 * Active profiles are read from {@value #PROFILE_PROPERTY} with fallback to
 * {@value #PROFILE_FALLBACK_PROPERTY}. Strict mode is controlled by
 * {@value #STRICT_PROPERTY} with fallback to
 * {@value #STRICT_FALLBACK_PROPERTY}.
 * </p>
 */
final class ProfileSelector {

    static final String PROFILE_PROPERTY = "wakamiti.junit5.profile";
    static final String PROFILE_FALLBACK_PROPERTY = "wakamiti.profile";
    static final String STRICT_PROPERTY = "wakamiti.junit5.profile.strict";
    static final String STRICT_FALLBACK_PROPERTY = "wakamiti.profile.strict";

    private ProfileSelector() {
        // static utility
    }

    /**
     * Checks whether a profiled test class should run.
     * <p>
     * If the class has no {@link Profile} (or it declares no effective values),
     * execution is allowed unless strict mode is enabled with active profiles.
     * If the class declares profiles, execution requires at least one match with
     * active profiles, except in non-strict mode with no active profiles.
     * </p>
     *
     * @param testClass test class to evaluate
     * @return {@code true} when the class is enabled for execution
     */
    static boolean isEnabled(
            Class<?> testClass
    ) {
        Set<String> activeProfiles = activeProfiles();
        boolean strictMode = strictMode();
        Profile profile = testClass.getAnnotation(Profile.class);

        if (profile == null || normalizeProfiles(profile.value()).isEmpty()) {
            return activeProfiles.isEmpty() || !strictMode;
        }

        if (activeProfiles.isEmpty()) {
            return !strictMode;
        }

        Set<String> declaredProfiles = normalizeProfiles(profile.value());
        return declaredProfiles.stream().anyMatch(activeProfiles::contains);
    }

    static String activeProfilesDescription() {
        Set<String> profiles = activeProfiles();
        return profiles.isEmpty() ? "<none>" : String.join(",", profiles);
    }

    private static Set<String> activeProfiles() {
        return normalizeProfiles(firstNonBlank(
                System.getProperty(PROFILE_PROPERTY),
                System.getProperty(PROFILE_FALLBACK_PROPERTY)
        ));
    }

    private static boolean strictMode() {
        return Boolean.parseBoolean(firstNonBlank(
                System.getProperty(STRICT_PROPERTY),
                System.getProperty(STRICT_FALLBACK_PROPERTY)
        ));
    }

    private static String firstNonBlank(
            String preferred,
            String fallback
    ) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "";
    }

    private static Set<String> normalizeProfiles(
            String... rawProfiles
    ) {
        if (rawProfiles == null || rawProfiles.length == 0) {
            return Set.of();
        }
        return Arrays.stream(rawProfiles)
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}

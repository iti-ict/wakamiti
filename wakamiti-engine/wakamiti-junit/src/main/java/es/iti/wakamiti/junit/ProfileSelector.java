/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


final class ProfileSelector {

    static final String PROFILE_PROPERTY = "wakamiti.junit.profile";
    static final String PROFILE_FALLBACK_PROPERTY = "wakamiti.profile";
    static final String STRICT_PROPERTY = "wakamiti.junit.profile.strict";
    static final String STRICT_FALLBACK_PROPERTY = "wakamiti.profile.strict";


    private ProfileSelector() {
        // static utility
    }


    static boolean isEnabled(Class<?> testClass) {
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


    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "";
    }


    private static Set<String> normalizeProfiles(String... rawProfiles) {
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

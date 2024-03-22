/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.util;


import es.iti.wakamiti.api.util.Pair;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Provides utility methods for calculating the distance between strings.
 * It includes a method to find closer strings from a collection based on
 * a reference string.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class StringDistance {

    private StringDistance() {
        // avoid instantiation
    }

    /**
     * Finds the closest strings to a given reference string from a
     * collection of candidates.
     *
     * <p>The distance between strings is calculated using a similarity
     * metric, and the results are sorted in descending order of similarity.
     *
     * <p>The results can be limited to a specified number if the limitResults
     * parameter is greater than or equal to 0.
     *
     * @param string       The reference string.
     * @param candidates   The collection of candidate strings.
     * @param limitResults The maximum number of results to return. Use -1 to return all results.
     * @return A list of closest strings to the reference string.
     */
    public static List<String> closerStrings(
            String string,
            Collection<String> candidates,
            int limitResults
    ) {
        Comparator<Pair<String, Double>> greaterDistance = Comparator.comparing(Pair::value);
        var stream = candidates.stream()
                .map(Pair.computeValue(candidate -> calculateDistance(string, candidate)))
                .sorted(greaterDistance.reversed());
        if (limitResults >= 0) {
            stream = stream.limit(limitResults);
        }
        return stream
                .map(Pair::key)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the distance between two strings using a similarity metric.
     *
     * <p>The similarity metric used is based on the {@link Simil} class,
     * which provides a percentage similarity between two strings.
     *
     * @param string    The first string.
     * @param candidate The second string.
     * @return The similarity percentage between the two strings.
     * @see Simil
     */
    private static double calculateDistance(String string, String candidate) {
        return new Simil(string).getSimilarityInPercentFor(candidate);
    }

}
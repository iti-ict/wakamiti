/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.util;


import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class StringDistance {


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




    private StringDistance() {
        // avoid instantiation
    }


    private static double calculateDistance(String string, String candidate) {
        return new Simil(string).getSimilarityInPercentFor(candidate);
    }

}
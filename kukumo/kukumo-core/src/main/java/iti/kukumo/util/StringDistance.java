package iti.kukumo.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.debatty.java.stringsimilarity.Levenshtein;

public class StringDistance {

    public static List<String> closerStrings(String string, List<String> candidates, int limitResults) {
        Levenshtein algorithm = new Levenshtein();
        Function<String,Double> calculateDistance = candidate -> algorithm.distance(string, candidate);
        Comparator<Pair<String,Double>> greaterDistance = Comparator.comparing(Pair::value);
        return candidates.stream()
            .map(Pair.computeValue(calculateDistance))
            .sorted(greaterDistance.reversed())
            .limit(limitResults)
            .map(Pair::key)
            .collect(Collectors.toList());
    }


    private StringDistance() {
        // avoid instantiation
    }
    
}

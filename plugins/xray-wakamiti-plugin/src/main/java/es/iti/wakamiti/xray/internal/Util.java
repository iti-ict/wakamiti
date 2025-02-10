/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;


import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;


public abstract class Util {

    private Util() {
        // prevent instantiation
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Matches if file path matches with glob.
     *
     * @param file the base directory to search from.
     * @param glob the glob pattern to match files against.
     * @return {@code true} if the file matches with glob, {@code false} otherwise.
     */
    public static boolean match(Path file, String glob) {
        return FileSystems.getDefault().getPathMatcher("glob:" + glob).matches(file);
    }
}

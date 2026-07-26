/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.internal;


import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.*;


/**
 * Patched variant of {@link DefaultListDelimiterHandler} used as a local workaround for the
 * duplicate-value flattening issue reported in Apache Commons Configuration.
 *
 * <p>The upstream handler keeps track of visited values while flattening nested structures in
 * order to avoid infinite recursion on cyclic object graphs. In affected versions, that traversal
 * strategy may also drop legitimate repeated scalar values from multi-valued properties. This is
 * especially visible with repeated booleans and numbers materialized from native JSON or YAML
 * arrays.</p>
 *
 * <p>The upstream project has already merged a fix for {@code CONFIGURATION-849} in pull request
 * {@code #569}. However, that change only addresses repeated {@link String} values. This local patch
 * is still required for native scalar values such as booleans and numbers.</p>
 *
 * <p>This implementation preserves the standard delimiter-based behavior for strings, but changes
 * the recursion guard so it only protects against cycles in recursive containers. Scalar leaf
 * values are therefore allowed to repeat, while self-referential or mutually referential
 * collections still terminate safely.</p>
 *
 * <p>The handler is intended as a focused compatibility patch and should be considered temporary
 * until Apache Commons Configuration clarifies and fixes {@code CONFIGURATION-857}.</p>
 *
 * @see DefaultListDelimiterHandler
 * @see <a href="https://issues.apache.org/jira/browse/CONFIGURATION-849">Apache Commons
 * Configuration issue CONFIGURATION-849</a>
 * @see <a href="https://issues.apache.org/jira/browse/CONFIGURATION-857">Apache Commons
 * Configuration issue CONFIGURATION-857</a>
 * @see <a href="https://github.com/apache/commons-configuration/pull/569">
 * Apache Commons Configuration pull request #569</a>
 */
public class PatchedListDelimiterHandler extends DefaultListDelimiterHandler {

    /**
     * Creates a patched list delimiter handler for the provided delimiter.
     *
     * @param delimiter delimiter used to split string-based multi-valued properties
     */
    public PatchedListDelimiterHandler(
            char delimiter
    ) {
        super(delimiter);
    }

    /**
     * Parses a raw configuration value into a flattened iterable representation.
     *
     * <p>Strings are split using the configured delimiter. Composite values such as iterables,
     * iterators or arrays are flattened recursively.</p>
     *
     * @param value raw configuration value
     * @return flattened iterable view of the value
     */
    @Override
    public Iterable<?> parse(
            final Object value
    ) {
        return flatten(value);
    }

    /**
     * Flattens a raw configuration value up to the provided limit.
     *
     * <p>This method overrides the Commons implementation and routes the operation through the
     * patched recursive logic in this class.</p>
     *
     * @param value raw configuration value
     * @param limit maximum number of elements to produce
     * @return flattened collection of values
     */
    @Override
    public Collection<?> flatten(
            final Object value,
            final int limit
    ) {
        return flatten(
                this,
                value,
                limit,
                Collections.newSetFromMap(new IdentityHashMap<>())
        );
    }

    /**
     * Flattens a value without applying an upper bound to the number of produced elements.
     *
     * @param value raw configuration value
     * @return flattened collection of values
     */
    private Collection<?> flatten(
            final Object value
    ) {
        return flatten(value, Integer.MAX_VALUE);
    }

    /**
     * Recursive flattening routine.
     *
     * <p>The {@code dejaVu} set is used only as an identity-based guard for recursive containers.
     * Leaf values are never filtered through it, so duplicates remain visible in the resulting
     * flattened collection. This preserves repeated scalar values while still allowing cyclic
     * structures to be traversed safely.</p>
     *
     * @param handler active list delimiter handler
     * @param value   current value to flatten
     * @param limit   maximum number of elements to produce
     * @param dejaVu  identity-based set of recursive containers currently being traversed
     * @return flattened collection of values
     */
    private Collection<?> flatten(
            final ListDelimiterHandler handler,
            final Object value,
            final int limit,
            final Set<Object> dejaVu
    ) {
        if (value instanceof String) {
            return handler.split((String) value, true);
        }
        if (!isRecursiveContainer(value)) {
            return value != null ? List.of(value) : Collections.emptyList();
        }

        dejaVu.add(value);
        try {
            final Collection<Object> result = new LinkedList<>();
            if (value instanceof Iterable) {
                flattenIterator(handler, result, ((Iterable<?>) value).iterator(), limit, dejaVu);
            } else if (value instanceof Iterator) {
                flattenIterator(handler, result, (Iterator<?>) value, limit, dejaVu);
            } else {
                for (
                        int len = Array.getLength(value), idx = 0, size = 0;
                        idx < len && size < limit;
                        idx++, size = result.size()
                ) {
                    result.addAll(flatten(handler, Array.get(value, idx), limit - size, dejaVu));
                }
            }
            return result;
        } finally {
            dejaVu.remove(value);
        }
    }

    /**
     * Flattens the contents of an iterator into the target collection.
     *
     * <p>Repeated scalar values are preserved. Only recursive container instances already present
     * in {@code dejaVu} are skipped in order to prevent infinite recursion.</p>
     *
     * @param handler  active list delimiter handler
     * @param target   target collection receiving the flattened values
     * @param iterator iterator to traverse
     * @param limit    maximum number of elements to produce
     * @param dejaVue  identity-based set of recursive containers currently being traversed
     */
    private void flattenIterator(
            final ListDelimiterHandler handler,
            final Collection<Object> target,
            final Iterator<?> iterator,
            final int limit,
            final Set<Object> dejaVue
    ) {
        int size = target.size();
        while (size < limit && iterator.hasNext()) {
            final Object next = iterator.next();
            if (!dejaVue.contains(next)) {
                target.addAll(flatten(handler, next, limit - size, dejaVue));
                size = target.size();
            }
        }
    }

    private boolean isRecursiveContainer(
            final Object value
    ) {
        return value instanceof Iterator
                || value instanceof Iterable && !(value instanceof Path)
                || value != null && value.getClass().isArray();
    }

}

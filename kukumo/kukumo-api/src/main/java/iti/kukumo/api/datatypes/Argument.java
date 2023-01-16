/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api.datatypes;

import iti.kukumo.api.KukumoAPI;
import iti.kukumo.api.extensions.PropertyEvaluator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Argument {

    private final List<PropertyEvaluator> resolvers = KukumoAPI.instance()
            .extensionManager().getExtensions(PropertyEvaluator.class)
            .collect(Collectors.toList());
    private String value;
    private Object evaluated;
    private final Map<String, String> evaluations = new LinkedHashMap<>();
    private Function<String, Object> mapper;

    public static Argument of(String value, Function<String, Object> mapper) {
        Argument arg = new Argument();
        arg.value = value;
        arg.mapper = mapper;
        return arg;
    }

    public final Object resolve() {
        return (evaluated = doResolve());
    }

    protected Object doResolve() {
        return mapper.apply(resolveForEach(value));
    }

    protected String resolveForEach(String value) {
        for (PropertyEvaluator resolver : resolvers) {
            PropertyEvaluator.Result result = resolver.eval(value);
            result.evaluations().forEach(evaluations::putIfAbsent);
            value = result.value();
        }
        return value;
    }

    public String value() {
        return value;
    }

    public Object evaluated() {
        return evaluated;
    }

    public Map<String, String> evaluations() {
        return evaluations;
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }
}

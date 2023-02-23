/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api.util;

import iti.kukumo.api.extensions.PropertyEvaluator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;


/**
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class Argument {

    private final Map<String, String> evaluations = new LinkedHashMap<>();
    private String value;
    private Object evaluated;
    private Function<String, Object> mapper;

    public static Argument of(String value, Function<String, Object> mapper) {
        Argument arg = new Argument();
        arg.value = value;
        arg.mapper = mapper;
        return arg;
    }

    public final Object resolve() {
        evaluated = doResolve();
        return evaluated;
    }

    protected Object doResolve() {
        return mapper.apply(resolveForEach(value));
    }

    protected final String resolveForEach(String value) {
        PropertyEvaluator.Result result = PropertyEvaluator.makeEval(value);
        result.evaluations().forEach(evaluations::putIfAbsent);
        return result.value();
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

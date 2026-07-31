/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter.datatypes;


import static es.iti.wakamiti.api.util.MapUtils.map;

import java.util.Map;

import es.iti.wakamiti.jmeter.Metric;


/**
 * Provides Long Metric services to the surrounding component.
 */
public class LongMetricProvider extends AbstractMetricProvider {

    /** Localized data-type key for an absolute number of executed samples. */
    public static final String SAMPLES = "metric.long.samples";
    /** Localized data-type key for an absolute number of failed samples. */
    public static final String ERRORS = "metric.long.errors";
    /** Localized data-type key for the total number of received network bytes. */
    public static final String RECEIVED_BYTES = "metric.long.receivedBytes";
    /** Localized data-type key for the total number of sent network bytes. */
    public static final String SENT_BYTES = "metric.long.sentBytes";

    private static final Map<String, Metric<Long>> METRICS = map(
            SAMPLES, s -> s.samples().total(),
            ERRORS, s -> s.errors().total(),
            RECEIVED_BYTES, s -> s.receivedBytes().total(),
            SENT_BYTES, s -> s.sentBytes().total()
    );

    @Override
    protected String[] expressions() {
        return METRICS.keySet().toArray(new String[0]);
    }

    @Override
    protected Metric<?> createMetric(
            String key
    ) {
        return METRICS.get(key);
    }

}

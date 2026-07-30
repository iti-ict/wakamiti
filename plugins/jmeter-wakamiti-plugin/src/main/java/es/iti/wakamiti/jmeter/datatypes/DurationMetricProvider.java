/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter.datatypes;


import static es.iti.wakamiti.api.util.MapUtils.map;

import java.time.Duration;
import java.util.Map;

import es.iti.wakamiti.jmeter.Metric;


/**
 * Provides Duration Metric services to the surrounding component.
 */
public class DurationMetricProvider extends AbstractMetricProvider {

    /** Localized data-type key for the minimum observed sample duration. */
    public static final String MIN = "metric.duration.min";
    /** Localized data-type key for the maximum observed sample duration. */
    public static final String MAX = "metric.duration.max";
    /** Localized data-type key for the arithmetic mean sample duration. */
    public static final String AVG = "metric.duration.avg";
    /** Localized data-type key for the median sample duration. */
    public static final String MEDIAN = "metric.duration.median";
    /** Localized data-type key for the 90th percentile sample duration. */
    public static final String PERCENTILE_90 = "metric.duration.percentile_90";
    /** Localized data-type key for the 95th percentile sample duration. */
    public static final String PERCENTILE_95 = "metric.duration.percentile_95";
    /** Localized data-type key for the 99th percentile sample duration. */
    public static final String PERCENTILE_99 = "metric.duration.percentile_99";

    private static final Map<String, Metric<Duration>> METRICS = map(
            MIN, s -> s.sampleTime().min(),
            MAX, s -> s.sampleTime().max(),
            AVG, s -> s.sampleTime().mean(),
            MEDIAN, s -> s.sampleTime().median(),
            PERCENTILE_90, s -> s.sampleTime().perc90(),
            PERCENTILE_95, s -> s.sampleTime().perc95(),
            PERCENTILE_99, s -> s.sampleTime().perc99()
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

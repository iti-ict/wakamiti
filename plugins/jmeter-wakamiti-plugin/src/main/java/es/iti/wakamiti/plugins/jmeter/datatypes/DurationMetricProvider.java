/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.plugins.jmeter.datatypes;


import es.iti.wakamiti.plugins.jmeter.Metric;

import java.time.Duration;
import java.util.Map;

import static es.iti.wakamiti.api.util.MapUtils.map;


public class DurationMetricProvider extends AbstractMetricProvider {

    public static final String MIN = "metric.duration.min";
    public static final String MAX = "metric.duration.max";
    public static final String AVG = "metric.duration.avg";
    public static final String MEDIAN = "metric.duration.median";
    public static final String PERCENTILE_90 = "metric.duration.percentile_90";
    public static final String PERCENTILE_95 = "metric.duration.percentile_95";
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
    protected Metric<?> createMetric(String key) {
        return METRICS.get(key);
    }
}

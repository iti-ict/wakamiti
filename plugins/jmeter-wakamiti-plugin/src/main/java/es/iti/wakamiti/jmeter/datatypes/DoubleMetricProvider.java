/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter.datatypes;


import es.iti.wakamiti.jmeter.Metric;

import java.util.Map;

import static es.iti.wakamiti.api.util.MapUtils.map;


public class DoubleMetricProvider extends AbstractMetricProvider {


    public static final String SAMPLES_SEC = "metric.double.samples";
    public static final String ERRORS_SEC = "metric.double.errors";
    public static final String RECEIVED_BYTES_SEC = "metric.double.receivedBytes";
    public static final String SENT_BYTES_SEC = "metric.double.sentBytes";

    private static final Map<String, Metric<Double>> METRICS = map(
            SAMPLES_SEC, s -> s.samples().perSecond(),
            ERRORS_SEC, s -> s.errors().perSecond(),
            RECEIVED_BYTES_SEC, s -> s.receivedBytes().perSecond(),
            SENT_BYTES_SEC, s -> s.sentBytes().perSecond()
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

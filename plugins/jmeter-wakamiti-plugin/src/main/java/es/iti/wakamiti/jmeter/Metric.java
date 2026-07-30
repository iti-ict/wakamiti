/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter;


import java.util.function.Function;

import us.abstracta.jmeter.javadsl.core.stats.StatsSummary;


/**
 * Defines the contract implemented by Metric.
 *
 * @param <T> the metric value type
 */
public interface Metric<T> extends Function<StatsSummary, T> {

}

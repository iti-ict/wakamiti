/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.plugins.jmeter.datatypes;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.DataTypeContributor;
import es.iti.wakamiti.plugins.jmeter.Metric;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Extension(provider = "es.iti.wakamiti", name = "metric-types")
public class WakamitiMetricTypes implements DataTypeContributor {

    @SuppressWarnings("rawtypes")
    private static WakamitiDataType<Metric> metric(
            String name,
            String prefix,
            AbstractMetricProvider provider
    ) {
        return new WakamitiMetricDataType(
                name,
                locale -> {
                    String[] expressions = provider.regex(locale).toArray(new String[0]);
                    return "(" + String.join("|", expressions) + ")";
                },
                locale -> AbstractMetricProvider.getAllExpressions(locale, prefix),
                parseProvider(provider)
        );
    }

    /**
     * Parses the provided metric providers and returns a locale-specific type parser for metrics.
     *
     * @param providers The assertion providers to parse.
     * @return The locale-specific type parser.
     */
    @SuppressWarnings("rawtypes")
    private static Function<Locale, Function<String, Metric>> parseProvider(
            AbstractMetricProvider... providers
    ) {
        return locale -> expression -> {
            for (AbstractMetricProvider assertProvider : providers) {
                Optional<Metric<?>> matcher = assertProvider
                        .metricFromExpression(locale, expression);
                return matcher.orElse(null);
            }
            return null;
        };
    }


    @Override
    public List<WakamitiDataType<?>> contributeTypes() {
        return List.of(
                metric("duration-metric", "metric.duration", new DurationMetricProvider()),
                metric("long-metric", "metric.long", new LongMetricProvider()),
                metric("double-metric", "metric.double", new DoubleMetricProvider())
        );
    }


    @SuppressWarnings("rawtypes")
    private static class WakamitiMetricDataType implements WakamitiDataType<Metric> {

        private final String name;
        private final Function<Locale, String> regexProvider;
        private final Function<Locale, List<String>> hintProvider;
        private final Function<Locale, Function<String, Metric>> parserProvider;
        private final Map<Locale, String> regexByLocale = new HashMap<>();
        private final Map<Locale, List<String>> hintsByLocale = new HashMap<>();
        private final Map<Locale, Function<String, Metric>> parserByLocale = new HashMap<>();

        public WakamitiMetricDataType(
                String name,
                Function<Locale, String> regexProvider,
                Function<Locale, List<String>> hintProvider,
                Function<Locale, Function<String, Metric>> parserProvider
        ) {
            this.name = name;
            this.regexProvider = regexProvider;
            this.hintProvider = hintProvider;
            this.parserProvider = parserProvider;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Class<Metric> getJavaType() {
            return Metric.class;
        }

        @Override
        public String getRegex(Locale locale) {
            return regexForLocale(locale);
        }

        @Override
        public List<String> getHints(Locale locale) {
            hintsByLocale.computeIfAbsent(locale, hintProvider);
            return hintsByLocale.get(locale);
        }

        @Override
        public Metric parse(Locale locale, String value) {
            return parserForLocale(locale).apply(value);
        }

        @Override
        public Matcher matcher(Locale locale, CharSequence value) {
            try {
                return Pattern.compile(regexForLocale(locale)).matcher(value);
            } catch (final Exception e) {
                throw new WakamitiException(
                        "Cannot create regex pattern for type {} using language {}", name, locale, e
                );
            }
        }

        /**
         * Retrieves or computes the regular expression for the given locale.
         *
         * @param locale The locale for which the regular expression is retrieved.
         * @return The regular expression.
         */
        protected String regexForLocale(Locale locale) {
            regexByLocale.computeIfAbsent(locale, regexProvider);
            return regexByLocale.get(locale);
        }

        /**
         * Retrieves or computes the type parser for the given locale.
         *
         * @param locale The locale for which the type parser is retrieved.
         * @return The type parser.
         */
        protected Function<String, Metric> parserForLocale(Locale locale) {
            parserByLocale.computeIfAbsent(locale, parserProvider);
            return parserByLocale.get(locale);
        }

    }
}

/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html.factory;


import java.time.Duration;
import java.util.Locale;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.TemplateValueFormatException;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;


/**
 * Creates and configures Duration Template Number Format instances.
 */
public final class DurationTemplateNumberFormatFactory extends TemplateNumberFormatFactory {

    /** Stateless singleton registered as FreeMarker's {@code duration} format. */
    public static final DurationTemplateNumberFormatFactory INSTANCE
            = new DurationTemplateNumberFormatFactory();

    private DurationTemplateNumberFormatFactory() {
        // Defined to decrease visibility
    }

    @Override
    public TemplateNumberFormat get(
            String params,
            Locale locale,
            Environment env
    ) throws TemplateValueFormatException {
        return new DurationNumberFormat(env.getTemplateNumberFormat(params, locale));
    }

    /**
     * Provides the Duration Number Format functionality used by Wakamiti.
     */
    private static final class DurationNumberFormat extends TemplateNumberFormat {

        /** Field value. */
        private final TemplateNumberFormat innerFormat;

        private DurationNumberFormat(
                TemplateNumberFormat innerFormat
        ) {
            this.innerFormat = innerFormat;
        }

        @Override
        public String formatToPlainText(
                TemplateNumberModel numberModel
        ) throws TemplateModelException {
            return DurationTemplateNumberFormatFactory.format(numberModel.getAsNumber().longValue());
        }

        @Override
        public boolean isLocaleBound() {
            return innerFormat.isLocaleBound();
        }

        @Override
        public String getDescription() {
            return "Duration " + innerFormat.getDescription();
        }

    }

    /**
     * Formats milliseconds as compact hours, minutes, seconds and milliseconds.
     * Zero-valued leading components are omitted; milliseconds are always
     * shown. For example, {@code 3_661_007} becomes
     * {@code "1h 1m 1s 7ms"}.
     *
     * @param value duration in milliseconds
     * @return compact human-readable duration
     */
    public static String format(
            long value
    ) {
        Duration duration = Duration.ofMillis(value);
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        int millis = duration.toMillisPart();

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0) {
            sb.append(seconds).append("s ");
        }
        sb.append(millis).append("ms");

        return sb.toString();
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.report.html.factory;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.TemplateValueFormatException;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

import java.time.Duration;
import java.util.Locale;

public class DurationTemplateNumberFormatFactory extends TemplateNumberFormatFactory {

    public static final DurationTemplateNumberFormatFactory INSTANCE
            = new DurationTemplateNumberFormatFactory();

    private DurationTemplateNumberFormatFactory() {
        // Defined to decrease visibility
    }

    @Override
    public TemplateNumberFormat get(String params, Locale locale, Environment env) throws TemplateValueFormatException {
        return new DurationNumberFormat(env.getTemplateNumberFormat(params, locale));
    }

    private static class DurationNumberFormat extends TemplateNumberFormat {

        private final TemplateNumberFormat innerFormat;

        private DurationNumberFormat(TemplateNumberFormat innerFormat) {
            this.innerFormat = innerFormat;
        }

        @Override
        public String formatToPlainText(TemplateNumberModel numberModel) throws TemplateModelException {

            Duration duration = Duration.ofMillis(numberModel.getAsNumber().longValue());
            int hours = duration.toHoursPart();
            int minutes = duration.toMinutesPart();
            int seconds = duration.toSecondsPart();
            int millis = duration.toMillisPart();

            StringBuilder sb = new StringBuilder();
            if (hours > 0) sb.append(hours).append("h ");
            if (minutes > 0) sb.append(minutes).append("m ");
            if (seconds > 0) sb.append(seconds).append("s ");
            sb.append(millis).append("ms");

            return sb.toString();
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

}

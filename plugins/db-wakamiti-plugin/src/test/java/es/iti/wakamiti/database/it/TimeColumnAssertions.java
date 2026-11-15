/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.it;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.database.ConnectionParameters;
import es.iti.wakamiti.database.jdbc.ConnectionProvider;
import es.iti.wakamiti.database.jdbc.Database;


final class TimeColumnAssertions {

    private static final String TABLE = "temporal_value";
    private static final String TIME_COLUMN = "time_value";
    private static final String TIME_WITH_TIMEZONE_COLUMN = "time_with_timezone_value";
    private static final String TIME_VALUE = "13:45:30";

    private TimeColumnAssertions() {
        // utility class
    }

    static void assertTimeColumns(
            String url,
            String username,
            String password,
            boolean withTimezone
    ) {
        Configuration configuration = Wakamiti.defaultConfiguration().appendFromPairs(
                "database.connection.url", url,
                "database.connection.username", username,
                "database.connection.password", password,
                "database.metadata.healthcheck", "false"
        );
        Backend backend = Wakamiti.instance().newBackendFactory().createNonRunnableBackend(configuration);
        WakamitiStepRunContext.set(new WakamitiStepRunContext(
                configuration,
                backend,
                Locale.getDefault(),
                Locale.getDefault()
        ));
        try (ConnectionProvider connection = new ConnectionProvider(
                new ConnectionParameters().url(url).username(username).password(password))) {
            Database database = Database.from(connection);
            String table = database.table(TABLE);
            String timeColumn = database.column(table, TIME_COLUMN);
            Map<String, String> values = new LinkedHashMap<>();
            values.put(TIME_COLUMN, TIME_VALUE);
            if (withTimezone) {
                values.put(TIME_WITH_TIMEZONE_COLUMN, TIME_VALUE + "+01:00");
            }

            Map<String, Object> result = database.processData(table, values);

            assertThat(result.get(timeColumn)).isEqualTo(LocalTime.of(13, 45, 30));
            if (withTimezone) {
                String timeWithTimezoneColumn = database.column(table, TIME_WITH_TIMEZONE_COLUMN);
                assertThat(result.get(timeWithTimezoneColumn)).isEqualTo(LocalTime.of(13, 45, 30));
            }
        } finally {
            WakamitiStepRunContext.clear();
        }
    }

}

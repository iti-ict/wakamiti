/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc.format;


import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.Options;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.database.jdbc.WakamitiTimestamp;
import org.apache.commons.lang3.BooleanUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import static es.iti.wakamiti.database.DatabaseHelper.DATE_TIME_FORMATTER;
import static java.util.Objects.isNull;


public class DefaultSqlFormat implements SqlFormat {

    public Object formatValue(String value, JDBCType type) {
        if (isNull(value)) return null;
        switch (type) {
            case BIT:
            case BOOLEAN:
                return formatBoolean(value);
            case TINYINT:
            case BIGINT:
            case INTEGER:
            case SMALLINT:
                return formatInteger(value);
            case DECIMAL:
            case DOUBLE:
            case FLOAT:
            case NUMERIC:
            case REAL:
                return formatDecimal(value);
            case DATE:
                return formatDate(value);
            case TIMESTAMP:
            case TIME:
            case TIME_WITH_TIMEZONE:
            case TIMESTAMP_WITH_TIMEZONE:
                return formatDateTime(value);
            default:
                return value;
        }
    }

    protected Object formatBoolean(String value) {
        if (value.contains(".")) value = String.valueOf(Float.valueOf(value).intValue());
        return BooleanUtils.toBooleanObject(value);
    }

    protected Object formatInteger(String value) {
        if (!isNull(BooleanUtils.toBooleanObject(value))) {
            value = BooleanUtils.toIntegerObject(BooleanUtils.toBooleanObject(value)).toString();
        }
        if (value.contains(".")) return new BigDecimal(value).toBigInteger();
        return new BigInteger(value);
    }

    protected Object formatDecimal(String value) {
        if (!isNull(BooleanUtils.toBooleanObject(value))) {
            value = BooleanUtils.toIntegerObject(BooleanUtils.toBooleanObject(value)).toString();
        }
        if (!value.contains(".")) return new BigInteger(value);
        return new BigDecimal(value);
    }

    protected Object formatDate(String value) {
        Calendar calendar = Chronic.parse(value, new Options(false)).getBeginCalendar();
        return WakamitiTimestamp.valueOf(
                LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId())
                        .truncatedTo(ChronoUnit.DAYS), true
        );
    }

    protected Object formatDateTime(String value) {
        try {
            LocalDateTime dateTime = parse(value, LocalDateTime.class);
            value = DATE_TIME_FORMATTER.format(dateTime);
        } catch (WakamitiException | DateTimeParseException e) {
            Calendar calendar2 = Chronic.parse(value, new Options(false)).getBeginCalendar();
            value = DATE_TIME_FORMATTER.withZone(calendar2.getTimeZone().toZoneId()).format(calendar2.toInstant());
        }
        return WakamitiTimestamp.valueOf(value, false);
    }

    /**
     * Parses a text expression into the specified data type.
     *
     * @param expression The text expression to parse.
     * @param type       The class of the type into which the expression should be parsed.
     * @param <T>        The type parameter representing the target type.
     * @return The parsed value of the specified type.
     * @throws WakamitiException if no type registry is found for the specified class.
     */
    @SuppressWarnings("unchecked")
    private <T> T parse(String expression, Class<T> type) {
        WakamitiStepRunContext ctx = WakamitiStepRunContext.current();
        return (T) ctx.typeRegistry().findTypesForJavaType(type).findFirst()
                .orElseThrow(() -> new WakamitiException("No type registry found for Class '{}'", type))
                .parse(ctx.stepLocale(), expression);
    }
}

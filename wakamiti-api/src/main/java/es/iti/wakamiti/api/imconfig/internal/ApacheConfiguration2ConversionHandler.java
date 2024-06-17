/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.internal;


import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Custom conversion handler for Apache Commons Configuration 2 library.
 * Provides additional conversion support for specific data types such as
 * BigDecimal, BigInteger, LocalTime, LocalDate, and LocalDateTime.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @see DefaultConversionHandler
 */
public class ApacheConfiguration2ConversionHandler extends DefaultConversionHandler {

    /**
     * Helper method to create a SimpleEntry instance.
     *
     * @param <R> The class type.
     * @param <F> The type of the function.
     * @param c   The class.
     * @param f   The function.
     * @return A SimpleEntry instance.
     */
    private static <R, F extends Function<String, R>> SimpleEntry<Class<R>, F> entry(Class<R> c, F f) {
        return new SimpleEntry<>(c, f);
    }

    /**
     * Map containing converters for specific data types.
     */
    private static final Map<Class<?>, Function<String, ?>> CONVERTERS = Stream.of(
        entry(BigDecimal.class, BigDecimal::new),
        entry(BigInteger.class, BigInteger::new),
        entry(LocalTime.class, LocalTime::parse),
        entry(LocalDate.class, LocalDate::parse),
        entry(LocalDateTime.class, LocalDateTime::parse)
    ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    /**
     * Converts the source object to the specified target class.
     *
     * @param src       The source object to be converted.
     * @param targetCls The target class to convert to.
     * @param ci        The configuration interpolator.
     * @param <T>       The type of the target class.
     * @return The converted value.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected <T> T convertValue(
        Object src,
        Class<T> targetCls,
        ConfigurationInterpolator ci
    ) {
        if (CONVERTERS.containsKey(targetCls)) {
            return (T) CONVERTERS.get(targetCls).apply(src.toString());
        }
        return super.convertValue(src, targetCls, ci);
    }

}

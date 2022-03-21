/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.configurer.internal;


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

import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;


public class ApacheConfiguration2ConversionHandler extends DefaultConversionHandler {

    private static <F extends Function<String, ?>> SimpleEntry<Class<?>, F> entry(Class<?> c, F f) {
        return new SimpleEntry<>(c, f);
    }


    private static final Map<Class<?>, Function<String, ?>> CONVERTERS = Stream.of(
        entry(BigDecimal.class, BigDecimal::new),
        entry(BigInteger.class, BigInteger::new),
        entry(LocalTime.class, LocalTime::parse),
        entry(LocalDate.class, LocalDate::parse),
        entry(LocalDateTime.class, LocalDateTime::parse)
    ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));


    @SuppressWarnings("unchecked")
    @Override
    protected <T extends Object> T convertValue(
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
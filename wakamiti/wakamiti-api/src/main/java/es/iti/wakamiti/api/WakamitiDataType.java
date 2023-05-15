/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.api;


import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;


public interface WakamitiDataType<T> {

    String getName();


    Class<T> getJavaType();


    String getRegex(Locale locale);


    List<String> getHints(Locale locale);


    T parse(Locale locale, String value);


    Matcher matcher(Locale locale, CharSequence value);


}
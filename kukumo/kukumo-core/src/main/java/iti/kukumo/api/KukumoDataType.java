/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;


public interface KukumoDataType<T> {

    String getName();


    Class<T> getJavaType();


    String getRegex(Locale locale);


    List<String> getHints(Locale locale);


    T parse(Locale locale, String value);


    Matcher matcher(Locale locale, CharSequence value);

}

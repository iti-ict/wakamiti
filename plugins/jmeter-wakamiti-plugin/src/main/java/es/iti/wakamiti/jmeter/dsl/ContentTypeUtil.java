/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter.dsl;


import org.apache.http.entity.ContentType;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Utility class for working with {@link ContentType} objects.
 * Provides methods to retrieve content types by their field names.
 */
public class ContentTypeUtil {

    private static final Map<String, ContentType> CONTENT_TYPE_MAP = new HashMap<>();

    static {
        for (Field field : ContentType.class.getDeclaredFields()) {
            if (field.getType() == ContentType.class) {
                try {
                    CONTENT_TYPE_MAP.put(field.getName(), (ContentType) field.get(null));
                } catch (IllegalAccessException ignored) {
                    // ignore error
                }
            }
        }
    }

    private ContentTypeUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns the {@link ContentType} corresponding to the specified content
     * type field name. Possible values are:
     * <ul>
     * <li>{@code APPLICATION_ATOM_XML} &rarr; {@code application/atom+xml}</li>
     * <li>{@code APPLICATION_FORM_URLENCODED} &rarr; {@code application/x-www-form-urlencoded}</li>
     * <li>{@code APPLICATION_JSON} &rarr; {@code application/json}</li>
     * <li>{@code APPLICATION_OCTET_STREAM} &rarr; {@code application/octet-stream}</li>
     * <li>{@code APPLICATION_SOAP_XML} &rarr; {@code application/soap+xml}</li>
     * <li>{@code APPLICATION_SVG_XML} &rarr; {@code application/svg+xml}</li>
     * <li>{@code APPLICATION_XHTML_XML} &rarr; {@code application/xhtml+xml}</li>
     * <li>{@code APPLICATION_XML} &rarr; {@code application/xml}</li>
     * <li>{@code IMAGE_BMP} &rarr; {@code image/bmp}</li>
     * <li>{@code IMAGE_GIF} &rarr; {@code image/gif}</li>
     * <li>{@code IMAGE_JPEG} &rarr; {@code image/jpeg}</li>
     * <li>{@code IMAGE_PNG} &rarr; {@code image/png}</li>
     * <li>{@code IMAGE_SVG} &rarr; {@code image/svg+xml}</li>
     * <li>{@code IMAGE_TIFF} &rarr; {@code image/tiff}</li>
     * <li>{@code IMAGE_WEBP} &rarr; {@code image/webp}</li>
     * <li>{@code MULTIPART_FORM_DATA} &rarr; {@code multipart/form-data}</li>
     * <li>{@code TEXT_HTML} &rarr; {@code text/html}</li>
     * <li>{@code TEXT_PLAIN} &rarr; {@code text/plain}</li>
     * <li>{@code TEXT_XML} &rarr; {@code text/xml}</li>
     * <li>{@code WILDCARD} &rarr; <code>&#42;/&#42;</code></li>
     * </ul>
     *
     * @param contentType The field name of the content type.
     * @return The {@link ContentType} object.
     * @throws IllegalArgumentException if the content type name is not found.
     */
    public static ContentType valueOf(final String contentType) {
        if (!CONTENT_TYPE_MAP.containsKey(contentType)) {
            throw new IllegalArgumentException("No such content type: " + contentType);
        }
        return CONTENT_TYPE_MAP.get(contentType);
    }

    /**
     * Returns a set of all available content type field names.
     *
     * @return A set of content type names.
     */
    public static Set<String> names() {
        return CONTENT_TYPE_MAP.keySet();
    }

}

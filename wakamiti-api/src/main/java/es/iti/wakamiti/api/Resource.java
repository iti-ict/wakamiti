/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


/**
 * Represents a resource with an absolute path, a relative path,
 * and associated content.
 *
 * @param <T> The type of content held by the resource.
 */
public class Resource<T> {

    private final String absolutePath;
    private final String relativePath;
    private final T content;

    /**
     * Creates a resource descriptor.
     *
     * @param absolutePath the fully resolved source location
     * @param relativePath the location relative to the resource search root
     * @param content      the loaded or parsed resource content
     */
    public Resource(
            String absolutePath,
            String relativePath,
            T content
    ) {
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.content = content;
    }

    /**
     * Gets the relative path of the resource.
     *
     * @return Relative path of the resource.
     */
    public String relativePath() {
        return relativePath;
    }

    /**
     * Gets the absolute path of the resource.
     *
     * @return Absolute path of the resource.
     */
    public String absolutePath() {
        return absolutePath;
    }

    /**
     * Gets the content associated with the resource.
     *
     * @return Content associated with the resource.
     */
    public T content() {
        return content;
    }

    /**
     * Returns a diagnostic representation containing both resource paths.
     * Content is deliberately omitted to avoid logging large or sensitive
     * payloads.
     *
     * @return a path-based resource description
     */
    public String toString() {
        return "Resource[absolutePath=" + absolutePath + ", relativePath=" + relativePath + "]";
    }

}

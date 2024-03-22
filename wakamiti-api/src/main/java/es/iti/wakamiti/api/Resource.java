/*
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
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class Resource<T> {

    private final String absolutePath;
    private final String relativePath;
    private final T content;

    public Resource(String absolutePath, String relativePath, T content) {
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

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api;



public class Resource<T> {

    private final String absolutePath;
    private final String relativePath;
    private final T content;


    public Resource(String absolutePath, String relativePath, T content) {
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.content = content;
    }


    public String relativePath() {
        return relativePath;
    }


    public String absolutePath() {
        return absolutePath;
    }


    public T content() {
        return content;
    }
}
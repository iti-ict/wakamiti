/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public class KukumoSkippedException extends KukumoException {

    public KukumoSkippedException() {
        super();
    }


    public KukumoSkippedException(String message, Throwable throwable) {
        super(message, throwable);
    }


    public KukumoSkippedException(String message) {
        super(message);
    }


    public KukumoSkippedException(String message, Object... args) {
        super(message, args);
    }


    public KukumoSkippedException(Throwable throwable) {
        super(throwable);
    }

}
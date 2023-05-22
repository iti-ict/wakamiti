/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public class WakamitiSkippedException extends WakamitiException {

    public WakamitiSkippedException() {
        super();
    }


    public WakamitiSkippedException(String message, Throwable throwable) {
        super(message, throwable);
    }


    public WakamitiSkippedException(String message) {
        super(message);
    }


    public WakamitiSkippedException(String message, Object... args) {
        super(message, args);
    }


    public WakamitiSkippedException(Throwable throwable) {
        super(throwable);
    }

}
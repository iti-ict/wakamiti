/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api;


import java.util.Arrays;


public class WakamitiException extends RuntimeException {

    private static final long serialVersionUID = 3126782976719868151L;


    public WakamitiException() {
        super();
    }


    public WakamitiException(String message, Throwable throwable) {
        super(message, throwable);
    }


    public WakamitiException(String message) {
        super(message);
    }


    public WakamitiException(String message, Object... args) {
        super(replace(message, argsWithoutThrowable(args)), throwable(args));
    }


    public WakamitiException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }


    private static String replace(String message, Object[] args) {
        StringBuilder s = new StringBuilder(message);
        for (Object arg : args) {
            int pos = s.indexOf("{}");
            if (pos == -1) {
                break;
            }
            s.replace(pos, pos + 2, String.valueOf(arg));
        }
        return s.toString();
    }


    private static Object[] argsWithoutThrowable(Object[] args) {
        return throwable(args) == null ? args : Arrays.copyOf(args, args.length - 1);
    }


    private static Throwable throwable(Object... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        return args[args.length - 1] instanceof Throwable ? (Throwable) args[args.length - 1]
                        : null;
    }

}
package es.iti.wakamiti.xray.internal;

import es.iti.wakamiti.api.WakamitiException;


public class WakamitiXRayException extends WakamitiException {

    private static final String MESSAGE = System.lineSeparator()
            + "Please resolve this issue in XRay or ignore it to continue.";

    public WakamitiXRayException(String message) {
        super(message + MESSAGE);
    }

    public WakamitiXRayException(String message, Throwable cause) {
        super(message + MESSAGE, cause);
    }

    public WakamitiXRayException(String message, Object... args) {
        super(message + MESSAGE, args);
    }

}
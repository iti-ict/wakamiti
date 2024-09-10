package es.iti.wakamiti.core.generator.features;

import es.iti.wakamiti.api.WakamitiException;

public class FeatureGeneratorException extends WakamitiException {


    /**
     * Constructs a new FeatureGeneratorException with the specified detail message and cause.
     *
     * @param message   The detail message (which is saved for later retrieval by the
     *                  {@link #getMessage()} method).
     * @param throwable The cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method). (A null value is permitted, and
     *                  indicates that the cause is nonexistent or unknown.)
     */
    public FeatureGeneratorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new FeatureGeneratorException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method).
     */
    public FeatureGeneratorException(String message) {
        super(message);
    }

    /**
     * Constructs a new FeatureGeneratorException with a formatted detail message.
     *
     * @param message The detail message format string.
     * @param args    The arguments referenced by the format specifiers in the format
     *                string. If there are more arguments than format specifiers, the
     *                extra arguments are ignored. The number of arguments is variable
     *                and may be zero.
     */
    public FeatureGeneratorException(String message, Object... args) {
        super(replace(message, argsWithoutThrowable(args)), throwable(args));
    }
}

package iti.commons.jext;

/**
 * This interface allows third-party contributors to implement custom mechanisms to retrieve extension instances,
 * instead of using the Java {@link java.util.ServiceLoader} approach.
 * <p>
 * This is specially suited for IoC injection frameworks that may manage object instances in a wide range of
 * different ways.
 * </p>
 */
public interface ExtensionLoader {
    <T> Iterable<T> load(Class<T> type, ClassLoader loader);
}

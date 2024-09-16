/*
 * @author Luis Iñesta Gelabert -  luiinge@gmail.com
 */
package es.iti.wakamiti.core.maven;

/**
 * Simple runtime exception that wraps other errors occurred during a fetching operation
 */
public class MavenFetchException extends RuntimeException{

    private static final long serialVersionUID = 1L;


    public MavenFetchException(Throwable e) {
        super(e);
    }

    public MavenFetchException(String message, Throwable e) {
        super(message, e);
    }

    public MavenFetchException(String message) {
        super(message);
    }

}

/**
 * @author Luis Iñesta Gelabert -  luiinge@gmail.com
 */
package es.iti.wakamiti.core.maven;


import java.util.stream.Stream;

/**
 * This interface exposes the results of a fetch operation
 */
public interface MavenFetchResult {

    /** @return A new stream with the fetched artifacts requested */
    Stream<FetchedArtifact> artifacts();

    /** @return A new stream with all fetched artifacts, includind dependencies */
    Stream<FetchedArtifact> allArtifacts();

    /** @return <tt>true</tt> if any error has ocurred during the fetching */
    boolean hasErrors();

    /**
     * @return A new stream with all the errors ocurred
     */
    Stream<Exception> errors();

}

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.maven.fetcher;


import java.util.stream.Stream;


public interface MavenFetchResult {

    Stream<FetchedArtifact> dependencies();
    Stream<FetchedArtifact> allDepedencies();
    boolean hasErrors();

}

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.maven.fetcher;


import java.util.Arrays;
import java.util.Collection;



public class MavenFetchRequest {

    private final Collection<String> artifacts;
    private Collection<String> scopes = Arrays.asList("compile", "provided");
    private boolean retrieveOptionals = false;


    public MavenFetchRequest(Collection<String> artifacts) {
        this.artifacts = artifacts;
    }


    public MavenFetchRequest scopes(String... scopes) {
        this.scopes = Arrays.asList(scopes);
        return this;
    }


    public MavenFetchRequest retrieveOptionals(boolean retrieveOptionals) {
        this.retrieveOptionals = retrieveOptionals;
        return this;
    }


    public Collection<String> artifacts() {
        return artifacts;
    }


    public Collection<String> scopes() {
        return scopes;
    }


    public boolean retrieveOptionals() {
        return retrieveOptionals;
    }
}

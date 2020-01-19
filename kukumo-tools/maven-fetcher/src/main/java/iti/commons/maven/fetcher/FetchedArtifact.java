/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.maven.fetcher;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.aether.artifact.Artifact;


public class FetchedArtifact {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final Path path;
    private final List<FetchedArtifact> dependencies;


    public FetchedArtifact(Artifact artifact, Path path, List<FetchedArtifact> dependencies) {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion();
        this.path = path;
        this.dependencies = dependencies;
    }


    public String groupId() {
        return groupId;
    }


    public String artifactId() {
        return artifactId;
    }


    public String version() {
        return version;
    }


    public String coordinates() {
        return groupId + ":" + artifactId + ":" + version;
    }


    public Stream<FetchedArtifact> dependencies() {
        return dependencies.stream();
    }


    public Stream<FetchedArtifact> allDepedencies() {
        return Stream.concat(
            dependencies(),
            dependencies().flatMap(FetchedArtifact::allDepedencies)
        );
    }


    public Path path() {
        return path;
    }


    @Override
    public String toString() {
        return toString(0, new StringBuilder()).toString();
    }


    private StringBuilder toString(int level, StringBuilder string) {
        for (int i = 0; i < level; i++) {
            string.append("  ");
        }
        string
            .append("|- ")
            .append(coordinates())
            .append("  [")
            .append(path)
            .append("]")
            .append("\n");
        for (FetchedArtifact child : dependencies) {
            child.toString(level + 1, string);
        }
        return string;
    }

}
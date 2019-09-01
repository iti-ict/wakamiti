package iti.commons.maven.fetcher;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.LocalRepositoryManager;

/**
 * @author ITI
 * Created by ITI on 25/03/19
 */
public class MavenFetchResult {


    public static class FetchedArtifact {

        private final String groupId;
        private final String artifactId;
        private final String version;
        private final Path path;
        private final List<FetchedArtifact> dependencies;

        FetchedArtifact(Artifact artifact, Path path, List<FetchedArtifact> dependencies) {
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
            return groupId+":"+artifactId+":"+version;
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
            for (int i=0;i<level;i++) {
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
                child.toString(level+1, string);
            }
            return string;
        }

    }



   private final CollectResult result;
   private final List<FetchedArtifact> rootArtifacts;


    MavenFetchResult(CollectResult result, DefaultRepositorySystemSession session) {
        this.result = result;
        LocalRepositoryManager localRepositoryManager = session.getLocalRepositoryManager();
        Path repositoryPath = localRepositoryManager.getRepository().getBasedir().toPath();
        this.rootArtifacts = result.getRoot().getChildren().stream()
            .map(node -> collectArtifact(node,localRepositoryManager,repositoryPath))
            .collect(Collectors.toList());
    }



    private FetchedArtifact collectArtifact(
        DependencyNode node,
        LocalRepositoryManager localRepositoryManager,
        Path repositoryPath
    ) {
        Artifact artifact = node.getArtifact();
        return new FetchedArtifact(
            artifact,
               repositoryPath.resolve(localRepositoryManager.getPathForLocalArtifact(artifact)),
               node.getChildren().stream()
                .map(child -> collectArtifact(child,localRepositoryManager,repositoryPath))
                .collect(Collectors.toList())
           );
    }



    public Stream<FetchedArtifact> dependencies() {
        return rootArtifacts.stream();
    }


    public Stream<FetchedArtifact> allDepedencies() {
        return Stream.concat(
            dependencies(),
            dependencies().flatMap(FetchedArtifact::allDepedencies)
           );
    }


    public boolean hasErrors() {
        return result.getExceptions().isEmpty();
    }


    @Override
    public String toString() {
        return dependencies().map(FetchedArtifact::toString).collect(Collectors.joining("\n"));
    }



}

package iti.commons.maven.fetcher;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.*;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenDependencyFetcher implements DependencySelector {

    private final DefaultRepositorySystemSession session;
    private final Collection<String> scopes;
    private final boolean retrieveOptionals;
    private final List<Dependency> dependencies;
    private final List<RemoteRepository> remoteRepositories;
    private final RepositorySystem system;
    private final Logger logger;
    
    private Set<String> retrievedArtifacts;
    
    public MavenDependencyFetcher(
        RepositorySystem system,
        List<RemoteRepository> remoteRepositories,
        DefaultRepositorySystemSession session,
        MavenFetchRequest fetchRequest,
        Logger logger
    ) {
        this.system = system;
        this.remoteRepositories = remoteRepositories;
        this.session = session.setDependencySelector(this);
        this.scopes = fetchRequest.scopes();
        this.retrieveOptionals = fetchRequest.retrieveOptionals();
        this.dependencies = fetchRequest.artifacts().stream()
                .map(DefaultArtifact::new)
                .map(artifact->new Dependency(artifact,null))
                .collect(Collectors.toList());
        this.logger = logger;
    }
    
    public MavenFetchResult fetch() throws DependencyCollectionException {
        logger.info("Searching in the following repositories: {}", remoteRepositories);
        this.retrievedArtifacts = new HashSet<>();
        CollectRequest request = new CollectRequest(dependencies,null,remoteRepositories);
        CollectResult result = system.collectDependencies(session, request);
        retrieveDependency(result.getRoot());
        resolveLocalPath(result.getRoot());
        return new MavenFetchResult(result);
    }

    
    
    private void retrieveDependency(DependencyNode node) {
        if (node.getArtifact() != null) {
            try {
                ArtifactRequest request = new ArtifactRequest(node.getArtifact(), remoteRepositories, null);
                system.resolveArtifact(session, request);
            } catch (ArtifactResolutionException e) {
                if (!(e.getCause() instanceof ArtifactNotFoundException)) {
                    logger.debug("<caused by>", e);
                }
            }
        }
        for (DependencyNode child : node.getChildren()) {
            retrieveDependency(child);
        }
    }


    @Override
    public boolean selectDependency(Dependency dependency) {
        String artifactKey = key(dependency.getArtifact());
        if (this.retrievedArtifacts.contains(artifactKey) ||
            (dependency.isOptional() && !retrieveOptionals) ||
            (!dependency.getScope().isEmpty() && !scopes.contains(dependency.getScope()))
        ) {
            return false;
        }
        this.retrievedArtifacts.add(artifactKey);
        return true;
    }


    
    @Override
    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        return this;
    }

    
       
    private static String key(Artifact artifact) {
        return artifact.getGroupId()+":"+artifact.getArtifactId()+":"+artifact.getVersion();
    }
    
    
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven.internal;


import es.iti.wakamiti.core.maven.MavenFetchException;
import es.iti.wakamiti.core.maven.MavenFetchRequest;
import es.iti.wakamiti.core.maven.MavenFetchResult;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.*;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;


public class MavenArtifactFetcher implements DependencySelector {

    private final DefaultRepositorySystemSession session;
    private final Collection<String> scopes;
    private final boolean retrieveOptionals;
    private final List<RemoteRepository> remoteRepositories;
    private final RepositorySystem system;
    private final Logger logger;
    private final MavenTransferListener listener;
    private List<Artifact> artifacts;
    private List<Exclusion> exclusions;

    public MavenArtifactFetcher(
            RepositorySystem system,
            List<RemoteRepository> remoteRepositories,
            DefaultRepositorySystemSession session,
            MavenFetchRequest fetchRequest,
            MavenTransferListener listener,
            Logger logger
    ) {
        this(system, remoteRepositories, session, fetchRequest.scopes(), fetchRequest.isRetrievingOptionals(),
                null, null, listener, logger);
        this.artifacts = fetchRequest.artifacts().stream()
                .map(this::artifactFromCoordinates)
                .collect(Collectors.toList());
        this.exclusions = fetchRequest.excludedArtifacts().stream()
                .map(this::exclusionFromCoordinates)
                .collect(Collectors.toList());
    }

    public MavenArtifactFetcher(
            RepositorySystem system,
            List<RemoteRepository> remoteRepositories,
            DefaultRepositorySystemSession session,
            Collection<String> scopes,
            boolean retrieveOptionals,
            List<Exclusion> exclusions,
            List<Artifact> artifacts,
            MavenTransferListener listener,
            Logger logger
    ) {
        this.system = system;
        this.remoteRepositories = remoteRepositories;
        this.session = session;
        this.scopes = scopes;
        this.retrieveOptionals = retrieveOptionals;
        this.exclusions = exclusions;
        this.artifacts = artifacts;
        this.logger = logger;
        this.listener = listener;
    }

    private static String key(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
    }

    private Exclusion exclusionFromCoordinates(String coordinates) {
        var parts = coordinates.split(":");
        if (parts.length >= 2) {
            var groupId = parts[0];
            var artifactId = parts[1];
            var classifier = parts.length > 2 ? parts[2] : null;
            var extension = parts.length > 3 ? parts[3] : "jar";
            return new Exclusion(groupId, artifactId, classifier, extension);
        } else {
            throw new IllegalArgumentException("Invalid exclusion '" + coordinates + "'");
        }
    }

    public MavenFetchResult fetch() throws DependencyCollectionException, ArtifactDescriptorException {
        if (logger.isInfoEnabled()) {
            logger.info("Using the following repositories:");
            for (var remoteRepository : remoteRepositories) {
                if (remoteRepository.getAuthentication() == null) {
                    logger.info("- {repository} [{uri}]", remoteRepository.getId(), remoteRepository.getUrl());
                } else {
                    logger.info("- {repository} [{uri}] (authenticated)", remoteRepository.getId(), remoteRepository.getUrl());
                }
            }
        }
        List<CollectResult> results = new ArrayList<>();
        for (var artifact : artifacts) {
            results.add(collectResult(artifact));
        }
        return new MavenFetchResultImpl(results, session);
    }

    private CollectResult collectResult(Artifact artifact)
            throws ArtifactDescriptorException,
            DependencyCollectionException {
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(artifact);
        descriptorRequest.setRepositories(remoteRepositories);
        ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);
        session.setDependencySelector(this);
        CollectRequest request = new CollectRequest();
        request.setRootArtifact(descriptorResult.getArtifact());
        request.setDependencies(descriptorResult.getDependencies());
        request.setManagedDependencies(descriptorResult.getManagedDependencies());
        request.setRepositories(remoteRepositories);

        CollectResult result = system.collectDependencies(session, request);

        retrieveDependency(result.getRoot());
        this.listener.failedTransfers()
                .forEach(file -> result.addException(new MavenFetchException("Could not fetch artifact " + file)));
        return result;
    }

    private DefaultArtifact artifactFromCoordinates(String coordinates) {
        try {
            return new DefaultArtifact(coordinates);
        } catch (IllegalArgumentException e) {
            // if version is not supplied, try to use the latest
            var parts = coordinates.split(":");
            if (parts.length == 2) {
                try {
                    var groupId = parts[0];
                    var artifactId = parts[1];
                    var versionRequest = new VersionRequest()
                            .setArtifact(new DefaultArtifact(groupId, artifactId, "jar", "LATEST"))
                            .setRepositories(this.remoteRepositories);
                    var version = system.resolveVersion(session, versionRequest).getVersion();
                    return new DefaultArtifact(groupId, artifactId, "jar", version);
                } catch (VersionResolutionException ex) {
                    throw new IllegalArgumentException("Cannot resolve artifact version: " + ex.getMessage(), ex);
                }
            }
            throw e;
        }
    }

    private void retrieveDependency(DependencyNode node) {
        if (node.getArtifact() != null) {
            try {
                ArtifactRequest request = new ArtifactRequest(
                        node.getArtifact(), remoteRepositories, null
                );
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
        requireNonNull(dependency, "dependency cannot be null");
        if ((dependency.isOptional() && !retrieveOptionals) ||
                (!dependency.getScope().isEmpty() && !scopes.contains(dependency.getScope()))
        ) {
            return false;
        }
        for (Exclusion exclusion : exclusions) {
            if (matches(exclusion, dependency.getArtifact())) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(Exclusion exclusion, Artifact artifact) {
        if (!matches(exclusion.getArtifactId(), artifact.getArtifactId())) {
            return false;
        }
        if (!matches(exclusion.getGroupId(), artifact.getGroupId())) {
            return false;
        }
        if (!matches(exclusion.getExtension(), artifact.getExtension())) {
            return false;
        }
        return matches(exclusion.getClassifier(), artifact.getClassifier());
    }

    private boolean matches(String pattern, String value) {
        return "*".equals(pattern) || pattern.equals(value);
    }

    @Override
    public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
        Objects.requireNonNull(context, "context cannot be null");
        Dependency dependency = context.getDependency();
        Collection<Exclusion> exclusions = dependency != null ? dependency.getExclusions() : null;
        if (exclusions != null && !exclusions.isEmpty()) {
            Exclusion[] merged = this.exclusions.toArray(new Exclusion[0]);
            int count = merged.length;

            for (Exclusion exclusion : exclusions) {
                int index = Arrays.binarySearch(merged, exclusion, ExclusionComparator.INSTANCE);
                if (index < 0) {
                    index = -(index + 1);
                    if (count >= merged.length) {
                        Exclusion[] tmp = new Exclusion[merged.length + exclusions.size()];
                        System.arraycopy(merged, 0, tmp, 0, index);
                        tmp[index] = exclusion;
                        System.arraycopy(merged, index, tmp, index + 1, count - index);
                        merged = tmp;
                    } else {
                        System.arraycopy(merged, index, merged, index + 1, count - index);
                        merged[index] = exclusion;
                    }

                    ++count;
                }
            }

            if (new HashSet<>(this.exclusions).containsAll(List.of(merged))
                    && new HashSet<>(List.of(merged)).containsAll(this.exclusions)) {
                return this;
            } else {
                if (merged.length != count) {
                    Exclusion[] tmp = new Exclusion[count];
                    System.arraycopy(merged, 0, tmp, 0, count);
                    merged = tmp;
                }

                return new MavenArtifactFetcher(system, remoteRepositories, session, scopes, retrieveOptionals, List.of(merged), artifacts, listener, logger);
            }
        } else {
            return this;
        }
    }

    private static class ExclusionComparator implements Comparator<Exclusion> {

        static final ExclusionComparator INSTANCE = new ExclusionComparator();

        public int compare(Exclusion e1, Exclusion e2) {
            if (e1 == null) {
                return (e2 == null) ? 0 : 1;
            } else if (e2 == null) {
                return -1;
            }
            int rel = e1.getArtifactId().compareTo(e2.getArtifactId());
            if (rel == 0) {
                rel = e1.getGroupId().compareTo(e2.getGroupId());
                if (rel == 0) {
                    rel = e1.getExtension().compareTo(e2.getExtension());
                    if (rel == 0) {
                        rel = e1.getClassifier().compareTo(e2.getClassifier());
                    }
                }
            }
            return rel;
        }
    }
}

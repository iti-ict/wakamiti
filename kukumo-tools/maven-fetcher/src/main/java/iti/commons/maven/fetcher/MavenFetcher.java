package iti.commons.maven.fetcher;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ITI
 * Created by ITI on 25/03/19
 */
public class MavenFetcher {

    private final List<RemoteRepository> remoteRepositories = new ArrayList<>();
    private final RepositorySystem system;
    
    private LocalRepository localRepository;
    private String proxyURL;
    private String proxyUsername;
    private String proxyPassword;
    private List<String> proxyExceptions;
    private Logger logger = LoggerFactory.getLogger(MavenFetcher.class);
    
    
     
    public MavenFetcher() {
        this.system = newRepositorySystem( MavenRepositorySystemUtils.newServiceLocator() );
    }

    
    
    public MavenFetcher config(Path configFile) throws IOException {
        new MavenFetcherConfig(configFile.toString()).config(this);
        return this;
    }
    
    public MavenFetcher config(Properties properties) throws MalformedURLException {
        new MavenFetcherConfig(properties).config(this);
        return this;
    }
    
    
    /**
     * Set the logger for this object
     */
    public MavenFetcher logger(Logger logger) {
        this.logger = logger;
        return this;
    }
    
   
    /**
     * Set the URL for the net proxy
     */
    public MavenFetcher proxyURL(String url) throws MalformedURLException {
        checkURL(url);
        checkNonNull(url);
        this.proxyURL = url;
        return this;
    }
    
    
    /**
     * Set the credentials for the next proxy
     */
    public MavenFetcher proxyCredentials(String username, String password) {
        checkNonNull(username,password);
        this.proxyUsername = username;
        this.proxyPassword = password;
        return this;
    }
    
    /**
     * Set exceptions for the next proxy
     */
    public MavenFetcher proxyExceptions(Collection<String> exceptions) {
        checkNonNull(exceptions);
        this.proxyExceptions = new ArrayList<>(exceptions);
        return this;
    }

    

    /**
     * Set the local repository path
     */
    public MavenFetcher localRepositoryPath(String localRepositoryPath) {
        this.localRepository = new LocalRepository(localRepositoryPath);
        return this;
    }
    

    /**
     * Set the local repository path
     */
    public MavenFetcher localRepositoryPath(Path localRepositoryPath) {
        if (localRepositoryPath == null) {
            throw new IllegalArgumentException("Local repository path cannot be null");
        }
        return localRepositoryPath(localRepositoryPath.toString());
    }
    
    
    /**
     * Add a remote repository
     */
    public MavenFetcher addRemoteRepository(String id, String url) {
        this.remoteRepositories.add(createRemoteRepository(id, url));
        return this;
    }
    
    
    /**
     * Retrieve the specified artifacts and their dependencies
     * @param artifacts
     * @throws DependencyCollectionException 
     * @throws ArtifactResolutionException 
     */
    public int fetchArtifacts(Collection<String> artifacts, Collection<String> scopes, boolean retrieveOptionals) 
    throws DependencyCollectionException {
        if (remoteRepositories.isEmpty()) {
            throw new IllegalArgumentException("Remote repositories not specified");
        }
        CollectResult result = new MavenDependencyFetcher(
            system, 
            remoteRepositories, 
            newSession(), 
            artifacts, 
            scopes, 
            retrieveOptionals,
            logger
         )
        .fetch();
        if (!result.getExceptions().isEmpty()) {
            logger.warn("Some dependencies were not fetched!");
        }
        return countFetchedArtifacts(result.getRoot(), new AtomicInteger());
    }
    
    
    
    private int countFetchedArtifacts(DependencyNode dependencyNode, AtomicInteger counter) {
        if (dependencyNode.getArtifact() != null && dependencyNode.getArtifact().getFile() != null) {
            counter.incrementAndGet();
        }
        dependencyNode.getChildren().forEach(child -> countFetchedArtifacts(child, counter));
        return counter.get();
    }



    private DefaultRepositorySystemSession newSession() {        
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));
        session.setTransferListener(new MavenTransferLogger(logger));
        proxy().ifPresent(session::setProxySelector);
        return session;
    }

    
    
    private Optional<ProxySelector> proxy() {
        if (proxyURL == null) {
            return Optional.empty();
        }
        URL url;
        try {
            url = new URL(proxyURL);
        } catch (MalformedURLException e) {
            // should never reach this point, URL was checked when setted
            throw new RuntimeException(e);
        }
        int port = url.getPort() < 0 ? 8080 : url.getPort();
        Authentication authentication = null;
        if (proxyUsername != null) {
            authentication = new AuthenticationBuilder().addUsername(proxyUsername).addPassword(proxyPassword).build();
        }
        Proxy proxy = new Proxy(url.getProtocol(),url.getHost(),port,authentication);
        return Optional.of(new DefaultProxySelector().add(proxy, proxyExceptions == null ? Collections.emptyList() : proxyExceptions));
    }


    
    private static RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }
    
    
    private static RemoteRepository createRemoteRepository(String id, String url) {
        return new RemoteRepository.Builder(id,"default",url).build();
    }
  

    private static void checkNonNull(Object... objects) {
        for (Object object : objects) {
            Objects.requireNonNull(object);
        }
    }
    
    private static void checkNonNull(Collection<? extends Object> collection) {
        Objects.requireNonNull(collection);
        for (Object object : collection) {
            Objects.requireNonNull(object);
        }
    }
    
    private static void checkURL(String url) throws MalformedURLException {
        new URL(url);
    }


}

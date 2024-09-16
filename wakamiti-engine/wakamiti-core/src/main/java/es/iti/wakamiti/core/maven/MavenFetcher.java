/*
 * @author Luis Iñesta Gelabert -  luiinge@gmail.com
 */
package es.iti.wakamiti.core.maven;


import es.iti.wakamiti.core.maven.internal.MavenArtifactFetcher;
import es.iti.wakamiti.core.maven.internal.MavenTransferListener;
import org.apache.maven.repository.internal.DefaultModelCacheFactory;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.repository.internal.ModelCacheFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static es.iti.wakamiti.core.maven.MavenFetcherProperties.*;


/**
 * This class allows to fetch Maven artifacts from one or several remote repositories.
 * <p>
 * <em>This class is mutable and not thread-safe.</em>
 */
public class MavenFetcher {



    static {
        AnsiLogger.addStyle("repository", "yellow,bold");
        AnsiLogger.addStyle("artifact", "green,bold");
    }


    private final List<RemoteRepository> remoteRepositories = new ArrayList<>(List.of(
        createRemoteRepository("maven-central",  "https://repo.maven.apache.org/maven2")
    ));

    private RepositorySystem system;
    private LocalRepository localRepository;
    private String proxyURL;
    private String proxyUsername;
    private String proxyPassword;
    private List<String> proxyExceptions;
    private Logger logger = AnsiLogger.of(LoggerFactory.getLogger(MavenFetcher.class));



    /**
     * Set the logger for this object
     */
    public MavenFetcher logger(Logger logger) {
        this.logger = AnsiLogger.of(logger);
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
        checkNonNull(username, password);
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
     * Remove all remote repositories, including the default Maven central repository.
     * <p>
     * Use this method if you want to restrict artifact downloading to a set 
     * of private repositories,
     */
    public MavenFetcher clearRemoteRepositories() {
        this.remoteRepositories.clear();
        return this;
    }


    /**
     * Add a remote repository
     */
    public MavenFetcher addRemoteRepository(Repository repository) {
        if (repository.priority() > -1) {
            this.remoteRepositories.add(repository.priority(),parseRemoteRepository(repository.toString()));
        } else {
            this.remoteRepositories.add(parseRemoteRepository(repository.toString()));
        }
        return this;
    }


    /**
     * Add a remote repository
     */
    public MavenFetcher addRemoteRepository(String id, String url) {
        return addRemoteRepository(new Repository(id,url));
    }






    /**
     * @return A list with the string representation of the configured remote repositories
     */
    public List<String> remoteRepositories() {
        return this.remoteRepositories.stream()
            .map(RemoteRepository::toString)
            .collect(Collectors.toList());
    }


    /**
     * Configure the fetcher according a set of properties
     * @see MavenFetcherProperties
     */
    public MavenFetcher config(Properties properties) {
        if ("false".equalsIgnoreCase(properties.getProperty(USE_DEFAULT_REMOTE_REPOSITORY, "true"))) {
            clearRemoteRepositories();
        }
        for (Object property : properties.keySet()) {
            try {
                var value = properties.getProperty(property.toString());
                switch (property.toString()) {
                    case REMOTE_REPOSITORIES:
                        addRemoteRepositories(Arrays.asList(value.split(";")));
                        break;
                    case LOCAL_REPOSITORY:
                        localRepositoryPath(value);
                        break;
                    case PROXY_USERNAME:
                        this.proxyUsername = value;
                        break;
                    case PROXY_PASSWORD:
                        this.proxyPassword = value;
                        break;
                    case PROXY_EXCEPTIONS:
                        this.proxyExceptions = Arrays.asList(value.split(";"));
                        break;
                    case PROXY_URL:
                        checkURL(value);
                        this.proxyURL = value;
                        break;
                    default:
                        logger.warn("Property {} is not recognized and would be ignored",property);
                }
            } catch (Exception e) {
                throw new MavenFetchException("Invalid value for property '"+property+"' : "+e.getMessage(), e);
            }
        }
        return this;
    }


    private void addRemoteRepositories(List<String> repositories) {
        for (String repository : repositories) {
            this.remoteRepositories.add(parseRemoteRepository(repository));
        }
    }


    /**
     * Retrieve the specified artifacts and their dependencies from the remote
     * repositories. 
     */
    public MavenFetchResult fetchArtifacts(MavenFetchRequest request) {
        try {
            if (remoteRepositories.isEmpty()) {
                throw new IllegalArgumentException("Remote repositories not specified");
            }
            MavenTransferListener listener = new MavenTransferListener(logger);
            MavenFetchResult result = new MavenArtifactFetcher(
                system(),
                remoteRepositories,
                newSession(listener),
                request,
                listener,
                logger
            )
            .fetch();
            if (result.hasErrors()) {
                logger.warn("Some dependencies were not fetched!");
            }

            logger.info("{} artifacts resolved.", result.allArtifacts().count());
            return result;
        } catch (DependencyCollectionException | ArtifactDescriptorException e) {
            throw new MavenFetchException(e);
        }
    }




    private RepositorySystem system() {
        if (system == null) {
            system = newRepositorySystem(MavenRepositorySystemUtils.newServiceLocator());
            if (system == null) {
                throw new NullPointerException("Cannot instantiate system");
            }
        }
        return system;
    }



    private DefaultRepositorySystemSession newSession(MavenTransferListener listener) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session
            .setLocalRepositoryManager(system().newLocalRepositoryManager(session, localRepository));
        session.setTransferListener(listener);
        session.setSystemProperties(System.getProperties());
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
            // should never reach this point, URL was checked when set
            throw new MavenFetchException(e);
        }
        int port = url.getPort() < 0 ? 8080 : url.getPort();
        Authentication authentication = null;
        if (proxyUsername != null) {
            authentication = new AuthenticationBuilder()
                .addUsername(proxyUsername)
                .addPassword(proxyPassword)
                .build();
        }
        var proxy =  new Proxy(url.getProtocol(), url.getHost(), port, authentication);
        return Optional.of(
            new DefaultProxySelector()
                .add(proxy, proxyExceptions == null ? "" : String.join("|", proxyExceptions))
        );
    }


    private RepositorySystem newRepositorySystem(DefaultServiceLocator locator) {
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                logger.error("Cannot create instance of {} for service {}",impl,type,exception);
            }
        });
//        locator.addService(ModelCacheFactory.class, DefaultModelCacheFactory.class);
        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }



    private static RemoteRepository createRemoteRepository(String id, String url) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(url);
        return new RemoteRepository.Builder(id, "default", url).build();
    }



    private static RemoteRepository createRemoteRepository(
        String id,
        String url,
        String user,
        String password
    ) {
        return new RemoteRepository.Builder(id, "default", url)
            .setAuthentication(new AuthenticationBuilder().addUsername(user).addPassword(password).build())
            .build();
    }


    private static RemoteRepository parseRemoteRepository (String value) {
        // id=url
        // id=url [user:password]
        String expression = value.strip().replaceAll("\\s+"," ");
        String[] parts = expression.split("=",2);
        if (parts.length != 2) throwInvalidRepositoryValue(value);
        String id = parts[0];
        expression = parts[1];

        parts = expression.split(" ",2);
        String url = parts[0];
        if (parts.length == 1) {
            return createRemoteRepository(id,url);
        }

        int start = parts[1].indexOf("[");
        int end = parts[1].lastIndexOf("]");
        if (start == -1 || end == -1) throwInvalidRepositoryValue(value);

        expression = parts[1].substring(start+1,end);

        parts = expression.split(":",2);
        if (parts.length != 2) throwInvalidRepositoryValue(value);

        return createRemoteRepository(id,url,parts[0],parts[1]);

    }

    private static void throwInvalidRepositoryValue(String value) {
        throw new IllegalArgumentException("Invalid repository value '"+ value +"' .\n"+
            "Expected formats are 'id=url' and 'id=url [user:pwd]'"
        );
    }


    private static void checkNonNull(Object... objects) {
        for (Object object : objects) {
            Objects.requireNonNull(object);
        }
    }


    private static void checkNonNull(Collection<?> collection) {
        Objects.requireNonNull(collection);
        for (Object object : collection) {
            Objects.requireNonNull(object);
        }
    }


    private static void checkURL(String url) throws MalformedURLException {
        new URL(url);
    }



}

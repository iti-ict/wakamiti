/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class EmbeddedDatabase<D extends Driver> {

    private static Map<Class<? extends Driver>,Driver> registeredDrivers = new HashMap<>();

    protected final int port;
    protected final Optional<String> initialScriptFile;



    protected EmbeddedDatabase(
            int port,
            String initialScriptFile,
            Class<D> driverClass,
            Supplier<D> driver
    ) {
        this.port = port;
        this.initialScriptFile = Optional.ofNullable(initialScriptFile);
        registeredDrivers.computeIfAbsent(driverClass,key->driver.get());
    }


    public final void start() throws Exception {
        startServer();
        System.out.println(String.format("URL Connection: %s [username: '%s', password: '%s']",
            urlConnection(),
            username(),
            password()
        ));
        applyInitialScript();
    }

    public final void stop() throws Exception {
        stopServer();
        cleanUp();
    }

    protected abstract void startServer() throws Exception;

    protected abstract void stopServer() throws Exception;

    protected abstract void cleanUp() throws Exception;


    protected void applyInitialScript() {
        initialScriptFile.map(this::resource).ifPresent(url -> {
            try(
                Connection connection = openConnection();
                InputStream inputStream = url.openStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
            ) {
                new ScriptRunner(connection,true,true).runScript(reader);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public abstract String username();
    public abstract String password();
    public abstract String urlConnection();



    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(urlConnection(),username(),password());
    }


    protected void deleteDir(Path path) throws IOException {
        try (Stream<Path> walker = Files.walk(path)) {
            walker
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    protected URL resource (String resource) {
        return Thread.currentThread().getContextClassLoader().getResource(resource);
    }


    public final void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public final void execute(String sql) throws SQLException {
        try (Connection connection = openConnection()) {
            execute(connection,sql);
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        }
    }

}

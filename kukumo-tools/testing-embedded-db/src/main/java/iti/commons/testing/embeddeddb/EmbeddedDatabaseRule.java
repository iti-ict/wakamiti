/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb;


import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiFunction;


public abstract class
EmbeddedDatabaseRule extends ExternalResource {

    protected interface DatabaseFactory extends BiFunction<Integer,String,EmbeddedDatabase> {};
    private final DatabaseFactory factory;
    private final int port;
    private final String initialScriptFile;

    private EmbeddedDatabase embeddedDatabase;


    protected EmbeddedDatabaseRule(DatabaseFactory factory) {
        this(factory, 0, null);
    }

    public EmbeddedDatabaseRule(DatabaseFactory factory, int port) {
        this(factory, port, null);
    }

    public EmbeddedDatabaseRule(DatabaseFactory engine, String initalScriptFile) {
        this(engine, 0, initalScriptFile);
    }


    public EmbeddedDatabaseRule(DatabaseFactory factory, int port, String initalScriptFile) {
        this.factory = factory;
        this.port = port;
        this.initialScriptFile = initalScriptFile;
    }


    public Connection openConnection() throws SQLException {
        return embeddedDatabase.openConnection();
    }


    public String urlConnection() {
        return embeddedDatabase.urlConnection();
    }


    @Override
    protected void before() throws Throwable {
        super.before();
        embeddedDatabase = factory.apply(port,initialScriptFile);
        embeddedDatabase.start();
    }


    @Override
    protected void after() {
        try {
            embeddedDatabase.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.after();
    }

}

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.mariadb;


import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.mysql.cj.jdbc.Driver;
import iti.commons.testing.embeddeddb.EmbeddedDatabase;

import java.nio.file.Paths;
import java.sql.SQLException;


public class EmbeddedMariaDbDatabase extends EmbeddedDatabase<Driver> {

    private final DBConfigurationBuilder dbConfBuilder;
    private final DBConfiguration dbConfiguration;
    private final DB db;


    public EmbeddedMariaDbDatabase(int port, String initialScriptFile)  {
        super(port,initialScriptFile, Driver.class, EmbeddedMariaDbDatabase::newDriver);
        try {
            this.dbConfBuilder = DBConfigurationBuilder.newBuilder();
            this.dbConfBuilder.setPort(port);
            this.dbConfiguration = dbConfBuilder.build();
            this.db = DB.newEmbeddedDB(dbConfiguration);
        } catch (ManagedProcessException e) {
            throw new RuntimeException(e);
        }
    }


    private static Driver newDriver() {
        try {
            return new Driver();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void startServer() throws Exception {
        db.start();
        db.createDB("test");
        db.run("use test");
    }


    @Override
    protected void stopServer() throws Exception {
        db.stop();
    }


    @Override
    protected void cleanUp() throws Exception {
        deleteDir(Paths.get(db.getConfiguration().getDataDir()));
    }



    @Override
    public String username() {
        return "root";
    }


    @Override
    public String password() {
        return "";
    }


    @Override
    public String urlConnection() {
        return dbConfBuilder.getURL("test")+"?serverTimezone=UTC";
    }




}

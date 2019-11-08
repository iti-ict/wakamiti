/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.hsql;

import iti.commons.testing.embeddeddb.EmbeddedDatabase;
import org.hsqldb.Server;
import org.hsqldb.jdbc.JDBCDriver;

public class EmbeddedHsqlDatabase extends EmbeddedDatabase<JDBCDriver> {

    private final Server server;


    public EmbeddedHsqlDatabase(int port, String initialScriptFile) {
        super(port,initialScriptFile, JDBCDriver.class, JDBCDriver::new);
        this.server = new Server();
        server.setDatabaseName(0,"test");
        server.setDatabasePath(0,"mem:test");
        if (port != 0) {
            server.setPort(port);
        }
    }

    @Override
    protected void startServer() throws Exception {
        server.start();
    }

    @Override
    protected void stopServer() throws Exception {
        server.stop();
    }


    @Override
    protected void cleanUp() throws Exception {
        //
    }

    @Override
    public String username() {
        return "SA";
    }

    @Override
    public String password() {
        return "";
    }

    @Override
    public String urlConnection() {
        return "jdbc:hsqldb:hsql://localhost:"+server.getPort()+"/test";
    }
}

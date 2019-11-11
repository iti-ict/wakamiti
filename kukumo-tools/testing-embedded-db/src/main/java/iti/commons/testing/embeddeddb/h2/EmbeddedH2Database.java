/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.h2;

import iti.commons.testing.embeddeddb.EmbeddedDatabase;
import org.h2.Driver;
import org.h2.tools.Server;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmbeddedH2Database extends EmbeddedDatabase<Driver> {

    private final Server server;

    public EmbeddedH2Database(int port, String initialScriptFile) {
        super(port,initialScriptFile,Driver.class,Driver::new);
        List<String> args = new ArrayList<>();
        if (port != 0) {
            args.add("-tcpPort");
            args.add(String.valueOf(port));
        }
        try {
            DriverManager.getConnection("jdbc:h2:mem:test",username(),password());
            this.server = Server.createTcpServer(args.toArray(new String[0]));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void startServer() throws Exception {
        System.out.println("Starting server...");
        server.start();

    }

    @Override
    protected void stopServer() throws Exception {
        System.out.println("Stopping server...");
        try {
            execute("DROP ALL OBJECTS DELETE FILES");
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.stop();
    }

    @Override
    protected void cleanUp() throws Exception {
        //
    }

    @Override
    public String username() {
        return "sa";
    }

    @Override
    public String password() {
        return "";
    }


    @Override
    public String urlConnection() {
        return "jdbc:h2:tcp://localhost:"+server.getPort()+"/mem:test";
    }


}

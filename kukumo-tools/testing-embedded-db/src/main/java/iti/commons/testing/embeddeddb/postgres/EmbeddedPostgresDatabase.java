/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.postgres;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import iti.commons.testing.embeddeddb.EmbeddedDatabase;
import org.postgresql.Driver;

public class EmbeddedPostgresDatabase extends EmbeddedDatabase<Driver> {


    private final EmbeddedPostgres.Builder embeddedPostgresBuilder;
    private EmbeddedPostgres embeddedPostgres;

    public EmbeddedPostgresDatabase(int port, String initialScriptFile) {
        super(port,initialScriptFile,Driver.class,Driver::new);
        this.embeddedPostgresBuilder = EmbeddedPostgres.builder()
            .setCleanDataDirectory(true)
        ;
        if (port != 0) {
            embeddedPostgresBuilder.setPort(port);
        }
    }


    @Override
    protected void startServer() throws Exception {
        this.embeddedPostgres = embeddedPostgresBuilder.start();
    }

    @Override
    protected void stopServer() throws Exception {
        this.embeddedPostgres.close();
    }

    @Override
    protected void cleanUp() throws Exception {
        //
    }

    @Override
    public String username() {
        return "postgres";
    }

    @Override
    public String password() {
        return "postgres";
    }


    @Override
    public String urlConnection() {
        return embeddedPostgres.getJdbcUrl("postgres","postgres");
    }


}

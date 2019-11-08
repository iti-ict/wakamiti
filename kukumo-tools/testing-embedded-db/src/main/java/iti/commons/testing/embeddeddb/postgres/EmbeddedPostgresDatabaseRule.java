/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.postgres;

import iti.commons.testing.embeddeddb.EmbeddedDatabaseRule;

public class EmbeddedPostgresDatabaseRule extends EmbeddedDatabaseRule {


    protected EmbeddedPostgresDatabaseRule() {
        super(EmbeddedPostgresDatabase::new);
    }

    public EmbeddedPostgresDatabaseRule(int port) {
        super(EmbeddedPostgresDatabase::new, port);
    }

    public EmbeddedPostgresDatabaseRule(String initalScriptFile) {
        super(EmbeddedPostgresDatabase::new, initalScriptFile);
    }

    public EmbeddedPostgresDatabaseRule(int port, String initalScriptFile) {
        super(EmbeddedPostgresDatabase::new, port, initalScriptFile);
    }

}

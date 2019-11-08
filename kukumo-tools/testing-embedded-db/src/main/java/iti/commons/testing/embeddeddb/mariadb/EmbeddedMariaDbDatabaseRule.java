/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.mariadb;

import iti.commons.testing.embeddeddb.EmbeddedDatabaseRule;

public class EmbeddedMariaDbDatabaseRule extends EmbeddedDatabaseRule {


    protected EmbeddedMariaDbDatabaseRule() {
        super(EmbeddedMariaDbDatabase::new);
    }

    public EmbeddedMariaDbDatabaseRule(int port) {
        super(EmbeddedMariaDbDatabase::new, port);
    }

    public EmbeddedMariaDbDatabaseRule(String initalScriptFile) {
        super(EmbeddedMariaDbDatabase::new, initalScriptFile);
    }

    public EmbeddedMariaDbDatabaseRule(int port, String initalScriptFile) {
        super(EmbeddedMariaDbDatabase::new, port, initalScriptFile);
    }

}

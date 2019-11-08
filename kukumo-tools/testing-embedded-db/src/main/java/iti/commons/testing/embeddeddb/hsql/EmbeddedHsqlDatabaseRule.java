/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.hsql;

import iti.commons.testing.embeddeddb.EmbeddedDatabaseRule;

public class EmbeddedHsqlDatabaseRule extends EmbeddedDatabaseRule {


    protected EmbeddedHsqlDatabaseRule() {
        super(EmbeddedHsqlDatabase::new);
    }

    public EmbeddedHsqlDatabaseRule(int port) {
        super(EmbeddedHsqlDatabase::new, port);
    }

    public EmbeddedHsqlDatabaseRule(String initalScriptFile) {
        super(EmbeddedHsqlDatabase::new, initalScriptFile);
    }

    public EmbeddedHsqlDatabaseRule(int port, String initalScriptFile) {
        super(EmbeddedHsqlDatabase::new, port, initalScriptFile);
    }

}

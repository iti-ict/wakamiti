/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.testing.embeddeddb.h2;

import iti.commons.testing.embeddeddb.EmbeddedDatabaseRule;

public class EmbeddedH2DatabaseRule extends EmbeddedDatabaseRule {


    protected EmbeddedH2DatabaseRule() {
        super(EmbeddedH2Database::new);
    }

    public EmbeddedH2DatabaseRule(int port) {
        super(EmbeddedH2Database::new, port);
    }

    public EmbeddedH2DatabaseRule(String initalScriptFile) {
        super(EmbeddedH2Database::new, initalScriptFile);
    }

    public EmbeddedH2DatabaseRule(int port, String initalScriptFile) {
        super(EmbeddedH2Database::new, port, initalScriptFile);
    }

}

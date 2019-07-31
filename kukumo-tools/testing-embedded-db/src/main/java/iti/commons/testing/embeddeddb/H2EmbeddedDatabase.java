package iti.commons.testing.embeddeddb;

public class H2EmbeddedDatabase extends EmbeddedDatabase {

    public H2EmbeddedDatabase(EmbeddedDatabaseTemplate template) {
        super(template);
    }

    @Override
    public void start() throws Exception {
        // nothing
    }

    @Override
    public void stop() throws Exception {
        // nothing
    }

}

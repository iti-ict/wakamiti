package iti.commons.testing.embeddeddb;

import org.junit.rules.ExternalResource;

public class EmbeddedDatabaseRule extends ExternalResource {

    private final EmbeddedDatabaseTemplate template;
    private final String initialScriptFile;
    private EmbeddedDatabase embeddedDatabase;

    
    public EmbeddedDatabaseRule(EmbeddedDatabaseTemplate template) {
        this(template,null);
    }
    
    
    public EmbeddedDatabaseRule(EmbeddedDatabaseTemplate template, String initalScriptFile) {
        this.template = template;
        this.initialScriptFile = initalScriptFile;
    }
    
    
    @Override
    protected void before() throws Throwable {
        super.before();
        embeddedDatabase = template.create();
        if (this.initialScriptFile != null) {
            embeddedDatabase.withInitialScript(initialScriptFile);
        }
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

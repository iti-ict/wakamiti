package iti.commons.testing.embeddeddb;

import java.util.Optional;

public abstract class EmbeddedDatabase {

    protected final EmbeddedDatabaseTemplate template;
    protected Optional<String> scriptFile = Optional.empty();

    public EmbeddedDatabase(EmbeddedDatabaseTemplate template) {
        this.template = template;
    }
    
    public EmbeddedDatabase withInitialScript(String scriptFile) {
        this.scriptFile = Optional.of(scriptFile);
        return this;
    }
    
    public abstract void start() throws Exception;
    
    public abstract void stop() throws Exception;
    
    
}

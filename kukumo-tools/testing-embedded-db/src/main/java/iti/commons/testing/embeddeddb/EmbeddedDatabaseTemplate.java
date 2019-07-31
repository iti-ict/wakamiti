package iti.commons.testing.embeddeddb;

import java.util.function.Function;

public class EmbeddedDatabaseTemplate {

    private final String connectionURL;
    private final String username;
    private final String password;
    private final String schema;
    private final int port;
    private final Function<EmbeddedDatabaseTemplate, EmbeddedDatabase> supplier;
    
    
    public EmbeddedDatabaseTemplate(
            String connectionURL, 
            String username,
            String password, 
            String schema, 
            int port,
            Function<EmbeddedDatabaseTemplate, EmbeddedDatabase> supplier
    ) {
        this.connectionURL = connectionURL;
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.port = port;
        this.supplier = supplier;
    }

    public String connectionURL() {
        return connectionURL;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String schema() {
        return schema;
    }

    public int port() {
        return port;
    }
    
    public EmbeddedDatabase create() {
        return supplier.apply(this);
    }
    
}

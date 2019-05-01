package iti.commons.testing.embeddeddb;

public class EmbeddedDatabaseTemplates {

    public static final EmbeddedDatabaseTemplate H2 = new EmbeddedDatabaseTemplate(
        "jdbc:h2:mem:testdb",
        "sa",
        "",
        "testdb",
        0,
        H2EmbeddedDatabase::new
    );

    
    public static final EmbeddedDatabaseTemplate MYSQL = new EmbeddedDatabaseTemplate(
            "jdbc:mysql://localhost:3307/testdb",
            "test",
            "test",
            "testdb",
            3307,
            MySqlEmbeddedDatabase::new
        );
    
    private EmbeddedDatabaseTemplates() { /* avoid instantiation */ }
    


}

package iti.kukumo.database.test.dialect;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.GenericContainer;

import static org.junit.Assert.assertEquals;

public class SQLServerDatabaseTest {

    @ClassRule
    public static GenericContainer sqlServerContainer = new GenericContainer("mcr.microsoft.com/mssql/server:2022-latest")
            .withExposedPorts(1433)
            .withEnv("ACCEPT_EULA", "Y")
            .withEnv("SA_PASSWORD", "P@ssw0rd");

    private static JdbcTemplate jdbcTemplate;

    @BeforeClass
    public static void setUp() {
        System.setProperty("javax.net.ssl.trustStore", "");
        String jdbcUrl = "jdbc:sqlserver://" + sqlServerContainer.getContainerIpAddress() + ":" + sqlServerContainer.getMappedPort(1433) + ";databaseName=master;";
        jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(jdbcUrl, "sa", "P@ssw0rd"));

        jdbcTemplate.execute("CREATE DATABASE mydb");

        jdbcUrl = "jdbc:sqlserver://" + sqlServerContainer.getContainerIpAddress() + ":" + sqlServerContainer.getMappedPort(1433) + ";databaseName=mydb;";
        jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(jdbcUrl, "sa", "P@ssw0rd"));
    }

    @Test
    public void testInsert() {
        jdbcTemplate.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255))");
        jdbcTemplate.execute("INSERT INTO users VALUES (1, 'John Doe')");
        jdbcTemplate.execute("INSERT INTO users VALUES (2, 'Jane Doe')");

        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        assertEquals(2, count);
    }
}

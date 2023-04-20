package iti.kukumo.database.test.dialect;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.GenericContainer;

import static org.junit.Assert.assertEquals;

public class MariaDBDatabaseTest {

    @ClassRule
    public static GenericContainer mariadbContainer = new GenericContainer("mariadb:10.5")
            .withExposedPorts(3306)
            .withEnv("MARIADB_ROOT_PASSWORD", "password");

    private static JdbcTemplate jdbcTemplate;

    @BeforeClass
    public static void setUp() {
        String jdbcUrl = "jdbc:mariadb://" + mariadbContainer.getContainerIpAddress() + ":" + mariadbContainer.getMappedPort(3306) + "/";
        jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(jdbcUrl, "root", "password"));

        jdbcTemplate.execute("CREATE DATABASE mydb");

        jdbcUrl = "jdbc:mariadb://" + mariadbContainer.getContainerIpAddress() + ":" + mariadbContainer.getMappedPort(3306) + "/mydb";
        jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(jdbcUrl, "root", "password"));
    }

    @Test
    public void testInsert() {
        jdbcTemplate.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name VARCHAR(255))");
        jdbcTemplate.execute("INSERT INTO users VALUES (1, 'John Doe')");
        jdbcTemplate.execute("INSERT INTO users VALUES (2, 'Jane Doe')");

        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        assertEquals(2, count);
    }
}

package iti.kukumo.database.test.dialect;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.GenericContainer;

import static org.junit.Assert.assertEquals;
public class OracleDatabaseTest {
    @ClassRule
    public static GenericContainer oracleContainer = new GenericContainer("oracleinanutshell/oracle-xe-11g")
            .withExposedPorts(1521)
            .withEnv("ORACLE_ALLOW_REMOTE", "true");

    private static JdbcTemplate jdbcTemplate;

    @BeforeClass
    public static void setUp() {
        String jdbcUrl = "jdbc:oracle:thin:@//" + oracleContainer.getContainerIpAddress() + ":" + oracleContainer.getMappedPort(1521) + "/XE";
        jdbcTemplate = new JdbcTemplate(
                new DriverManagerDataSource(jdbcUrl, "system", "oracle"));

        jdbcTemplate.execute("CREATE USER myuser IDENTIFIED BY mypassword");
        jdbcTemplate.execute("GRANT CONNECT, RESOURCE, DBA TO myuser");
        jdbcTemplate.execute("CREATE TABLE mytable (id NUMBER PRIMARY KEY, name VARCHAR2(255))");
    }

    @Test
    public void testInsert() {
        jdbcTemplate.execute("INSERT INTO mytable VALUES (1, 'John Doe')");
        jdbcTemplate.execute("INSERT INTO mytable VALUES (2, 'Jane Doe')");

        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mytable", Integer.class);
        assertEquals(2, count);
    }

}

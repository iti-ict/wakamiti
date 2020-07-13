package iti.kukumo.database.sql;

import iti.kukumo.database.CaseSensitivity;
import iti.kukumo.database.SQLParser;
import iti.kukumo.database.dataset.DataSet;
import iti.kukumo.database.dataset.OoxmlDataSet;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SQLParseTest {

    private static final String SQL_OK = "/*comment*/INSERT INTO T (A, B) VALUES (1, NOW());DELETE FROM T WHERE A = 1;";
    private static final String SQL_KO = "/*comment*/INSERT INTO T;";
    private static Logger log = LoggerFactory.getLogger("iti.kukumo.test");
    private SQLParser parser = new SQLParser(CaseSensitivity.INSENSITIVE);

    @Test
    public void testParseStatementsWhenSqlIsOkWithSuccess() throws IOException, JSQLParserException {
        List<Statement> result = parser.parseStatements(SQL_OK);
        log.debug("Result: {}", result);
        assertEquals(2, result.size());
        assertEquals(Insert.class, result.get(0).getClass());
        assertEquals(Delete.class, result.get(1).getClass());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseStatementsWhenSqlIsNotOkWithError() throws IOException, JSQLParserException {
        try {
            parser.parseStatements(SQL_KO);
        } catch (JSQLParserException e) {
            log.debug(e.getCause().getMessage());
            throw e;
        }
    }

    @Test
    public void testToSelectWhenIsInsertWithSuccess() throws JSQLParserException {
        Statement insert = CCJSqlParserUtil.parse("INSERT INTO T (A, B) VALUES (1, NOW());");
        Optional<Select> result = parser.toSelect(insert);
        log.debug("Result insert: {}", result);
        assertTrue(result.isPresent());
    }

    @Test
    public void testToSelectWhenIsUpdateWithSuccess() throws JSQLParserException {
        Statement update = CCJSqlParserUtil.parse("UPDATE T SET B = NOW(), C = 1 WHERE A = 1;");
        Optional<Select> result = parser.toSelect(update);
        log.debug("Result update: {}", result);
        assertTrue(result.isPresent());
        assertEquals("SELECT * FROM T WHERE A = 1", result.get().toString());
    }

    @Test
    public void testToSelectWhenIsDeleteWithSuccess() throws JSQLParserException {
        Statement delete = CCJSqlParserUtil.parse("DELETE FROM T WHERE A = 1;");
        Optional<Select> result = parser.toSelect(delete);
        log.debug("Result delete: {}", result);
        assertTrue(result.isPresent());
        assertEquals("SELECT * FROM T WHERE A = 1", result.get().toString());
    }

    @Test
    public void testToSelectWhenIsDataSetWithSuccess() throws IOException {
        File file = new File("src/test/resources/data1.xlsx");
        try (OoxmlDataSet multiDataSet = new OoxmlDataSet(file, "#.*", "<null>")) {
            Iterator<DataSet> iterator = multiDataSet.iterator();
            DataSet clients = iterator.next();
            Select result = parser.toSelect(clients);
            log.debug("Result: {}", result);
            String expected = "SELECT * FROM client " +
                    "WHERE ((trim(id) = ? OR (trim(id) IS NULL AND ? IS NULL)) " +
                    "AND (trim(first_name) = ? OR (trim(first_name) IS NULL AND ? IS NULL)) " +
                    "AND (trim(second_name) = ? OR (trim(second_name) IS NULL AND ? IS NULL)) " +
                    "AND (trim(active) = ? OR (trim(active) IS NULL AND ? IS NULL)) " +
                    "AND (trim(birth_date) = ? OR (trim(birth_date) IS NULL AND ? IS NULL)))";
            assertEquals(expected, result.toString());
        } catch (IOException e) {
            log.error("Test error", e);
            throw e;
        }
    }

    @Test
    public void testSqlSelectFromWithSuccess() {
        Select result = parser.sqlSelectFrom("T");
        log.debug("Result: {}", result);
        assertEquals("SELECT * FROM T", result.toString());
    }

    @Test
    public void testSqlSelectCountWhenTableFromWithSuccess() {
        Select result = parser.sqlSelectCountFrom("T");
        log.debug("Result: {}", result);
        assertEquals("SELECT count(*) FROM T", result.toString());
    }

    @Test
    public void testSqlSelectCountFromWhenDataSetWithSuccess() throws IOException {
        File file = new File("src/test/resources/data1.xlsx");
        try (OoxmlDataSet multiDataSet = new OoxmlDataSet(file, "#.*", "<null>")) {
            Iterator<DataSet> iterator = multiDataSet.iterator();
            DataSet clients = iterator.next();
            Select result = parser.sqlSelectCountFrom(clients.table(), clients.columns());
            log.debug("Result: {}", result);
            String expected = "SELECT count(*) FROM client " +
                    "WHERE ((trim(id) = ? OR (trim(id) IS NULL AND ? IS NULL)) " +
                    "AND (trim(first_name) = ? OR (trim(first_name) IS NULL AND ? IS NULL)) " +
                    "AND (trim(second_name) = ? OR (trim(second_name) IS NULL AND ? IS NULL)) " +
                    "AND (trim(active) = ? OR (trim(active) IS NULL AND ? IS NULL)) " +
                    "AND (trim(birth_date) = ? OR (trim(birth_date) IS NULL AND ? IS NULL)))";
            assertEquals(expected, result.toString());
        } catch (IOException e) {
            log.error("Test error", e);
            throw e;
        }
    }

    @Test
    public void testSqlDeleteFromWithSuccess() {
        Delete result = parser.sqlDeleteFrom("T");
        log.debug("Result: {}", result);
        assertEquals("DELETE FROM T", result.toString());
    }

    @Test
    public void testSqlInsertIntoValuesWithSuccess() throws IOException {
        File file = new File("src/test/resources/data1.xlsx");
        try (OoxmlDataSet multiDataSet = new OoxmlDataSet(file, "#.*", "<null>")) {
            Iterator<DataSet> iterator = multiDataSet.iterator();
            DataSet clients = iterator.next();
            Insert result = parser.sqlInsertIntoValues(clients);
            log.debug("Result: {}", result);
            String expected = "INSERT INTO client (id, first_name, second_name, active, birth_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
            assertEquals(expected, result.toString());
        } catch (IOException e) {
            log.error("Test error", e);
            throw e;
        }
    }

    @Test
    public void testSqlUpdateSetWithSuccess() throws IOException {
        File file = new File("src/test/resources/data1.xlsx");
        try (OoxmlDataSet multiDataSet = new OoxmlDataSet(file, "#.*", "<null>")) {
            Iterator<DataSet> iterator = multiDataSet.iterator();
            DataSet clients = iterator.next();
            Update result = parser.sqlUpdateSet(clients, new String[] {"id"});
            log.debug("Result: {}", result);
            String expected = "UPDATE client SET first_name = ?, second_name = ?, active = ?, birth_date = ? " +
                    "WHERE (trim(id) = ?)";
            assertEquals(expected, result.toString());
        } catch (IOException e) {
            log.error("Test error", e);
            throw e;
        }
    }

}

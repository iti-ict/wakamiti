/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SQLParseTest {

    private static final String SQL_OK = "/*comment*/INSERT INTO T (A, B) VALUES (1, NOW());DELETE FROM T WHERE A = 1;";
    private static final String SQL_KO = "/*comment*/INSERT INTO T;";
    private static final Logger log = LoggerFactory.getLogger("es.iti.wakamiti.test");
    private final SQLParser parser = new SQLParser();

    @Test
    public void testParseStatementsWhenSqlIsOkWithSuccess() throws JSQLParserException {
        List<Statement> result = SQLParser.parseStatements(SQL_OK);
        log.debug("Result: {}", result);
        assertEquals(2, result.size());
        assertEquals(Insert.class, result.get(0).getClass());
        assertEquals(Delete.class, result.get(1).getClass());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseStatementsWhenSqlIsNotOkWithError() throws JSQLParserException {
        try {
            SQLParser.parseStatements(SQL_KO);
        } catch (JSQLParserException e) {
            log.debug(e.getCause().getMessage());
            throw e;
        }
    }

    @Test
    public void testToSelectWhenIsInsertWithSuccess() throws JSQLParserException {
        Statement insert = CCJSqlParserUtil.parse("INSERT INTO T (A, B) VALUES (1, NOW());");
        Optional<PlainSelect> result = parser.toSelect(insert);
        log.debug("Result insert: {}", result);
        assertTrue(result.isPresent());
    }

    @Test
    public void testToSelectWhenIsUpdateWithSuccess() throws JSQLParserException {
        Statement update = CCJSqlParserUtil.parse("UPDATE T SET B = NOW(), C = 1 WHERE A = 1;");
        Optional<PlainSelect> result = parser.toSelect(update);
        log.debug("Result update: {}", result);
        assertTrue(result.isPresent());
        assertEquals("SELECT * FROM T WHERE A = 1", result.get().toString());
    }

    @Test
    public void testToSelectWhenIsDeleteWithSuccess() throws JSQLParserException {
        Statement delete = CCJSqlParserUtil.parse("DELETE FROM T WHERE A = 1;");
        Optional<PlainSelect> result = parser.toSelect(delete);
        log.debug("Result delete: {}", result);
        assertTrue(result.isPresent());
        assertEquals("SELECT * FROM T WHERE A = 1", result.get().toString());
    }

    @Test
    public void testSqlSelectCountWhenTableFromWithSuccess() {
        Select result = parser.sqlSelectCountFrom("T");
        log.debug("Result: {}", result);
        assertEquals("SELECT count(*) FROM T", result.toString());
    }

    @Test
    public void testSqlDeleteFromWithSuccess() {
        Delete result = parser.toDelete("T");
        log.debug("Result: {}", result);
        assertEquals("DELETE FROM T", result.toString());
    }

    @Test
    public void testSqlDb2() throws JSQLParserException {
        String sql = "SELECT 'MINUTES', TO_CHAR(CURRENT_DATE - CAST(col1 AS NUMERIC) DAYS, 'YYYYMMDD') FROM \"table1\" WHERE col2 = 'DAYS'";
        Statement result = SQLParser.parseStatement(sql);
        log.debug("Result query: {}", result);
        assertThat(result).hasToString(
                "SELECT 'MINUTES', TO_CHAR(CURRENT_DATE - CAST(col1 AS NUMERIC) DAY, 'YYYYMMDD') " +
                        "FROM \"table1\" WHERE col2 = 'DAYS'");

        sql = "SELECT * FROM table1 WHERE " +
                "TO_TIMESTAMP(TO_CHAR(FECHA1), 'YYYYMMDD') <= CURRENT_TIMESTAMP AND " +
                "TO_TIMESTAMP(TO_CHAR(FECHA1), 'YYYYMMDD') >= (CURRENT_TIMESTAMP - 1 MINUTES) AND " +
                "TO_TIMESTAMP(TO_CHAR(FECHA2), 'YYYYMMDD') <= CURRENT_TIMESTAMP AND " +
                "TO_TIMESTAMP(TO_CHAR(FECHA2), 'YYYYMMDD') >= (CURRENT_TIMESTAMP - 1 MINUTE)";
        result = SQLParser.parseStatement(sql);
        log.debug("Result query: {}", result);
        assertThat(result)
                .hasToString("SELECT * FROM table1 " +
                        "WHERE TO_TIMESTAMP(TO_CHAR(FECHA1), 'YYYYMMDD') <= CURRENT_TIMESTAMP " +
                        "AND TO_TIMESTAMP(TO_CHAR(FECHA1), 'YYYYMMDD') >= (CURRENT_TIMESTAMP - 1 MINUTE) " +
                        "AND TO_TIMESTAMP(TO_CHAR(FECHA2), 'YYYYMMDD') <= CURRENT_TIMESTAMP " +
                        "AND TO_TIMESTAMP(TO_CHAR(FECHA2), 'YYYYMMDD') >= (CURRENT_TIMESTAMP - 1 MINUTE)");
    }

    @Test
    public void testAlter() throws JSQLParserException {
        String sql = "ALTER TRIGGER TRIGNAME DISABLE";
        Statement result = SQLParser.parseStatement(sql);
        log.debug("Result query: {}", result);
        assertThat(result).hasToString(sql);
    }

}
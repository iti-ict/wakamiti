/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.wakamiti.database.jdbc.DatabaseType;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static es.iti.wakamiti.api.util.StringUtils.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SQLParseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("es.iti.wakamiti.test");


    @Test
    public void testParseStatementsWhenSqlIsOkWithSuccess() throws JSQLParserException {
        // prepare
        String sql = "/*comment*/INSERT INTO T (A, B) VALUES (1, NOW());DELETE FROM T WHERE A = 1;";

        // act
        List<Statement> result = SQLParser.parseStatements(sql);
        LOGGER.debug("Result: {}", result);

        // check
        assertEquals(2, result.size());
        assertEquals(Insert.class, result.get(0).getClass());
        assertEquals(Delete.class, result.get(1).getClass());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseStatementsWhenSqlIsNotOkWithError() throws JSQLParserException {
        // prepare
        String sql = "/*comment*/INSERT INTO T;";

        // act
        try {
            SQLParser.parseStatements(sql);
        } catch (JSQLParserException e) {
            LOGGER.debug(e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testParseStatementWhenSqlIsOkWithSuccess() throws JSQLParserException {
        // prepare
        String sql = "/*comment*/INSERT INTO T (A, B) VALUES (1, NOW());";

        // act
        Statement result = SQLParser.parseStatement(sql);
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isNotNull()
                .isOfAnyClassIn(Insert.class);
    }

    @Test(expected = JSQLParserException.class)
    public void testParseStatementWhenMultipleStatementsWithSuccess() throws JSQLParserException {
        // prepare
        String sql = "/*comment*/INSERT INTO T (A, B) VALUES (1, NOW());DELETE FROM T WHERE A = 1;";

        // act
        try {
            SQLParser.parseStatement(sql);
        } catch (JSQLParserException e) {
            LOGGER.debug(e.getMessage(), e);
            assertThat(e).hasMessage("There are more than one sentence");
            throw e;
        }
    }

    @Test(expected = JSQLParserException.class)
    public void testParseStatementWhenSqlIsNotOkWithError() throws JSQLParserException {
        // prepare
        String sql = "/*comment*/INSERT INTO T;";

        // act
        try {
            SQLParser.parseStatement(sql);
        } catch (JSQLParserException e) {
            LOGGER.debug(e.getCause().getMessage());
            throw e;
        }
    }

    @Test
    public void testParseExpressionWhenSqlIsOkWithSuccess() throws JSQLParserException {
        // prepare
        String sql = "A = 1 AND B = (CURRENT_TIMESTAMP - 1 MINUTE)";

        // act
        Expression result = SQLParser.parseExpression(sql);
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isNotNull()
                .isOfAnyClassIn(AndExpression.class);
    }

    @Test(expected = JSQLParserException.class)
    public void testParseExpressionWhenSqlIsNotOkWithError() throws JSQLParserException {
        // prepare
        String sql = "WHERE 1";

        // act
        try {
            SQLParser.parseExpression(sql);
        } catch (JSQLParserException e) {
            LOGGER.debug(e.getCause().getMessage());
            throw e;
        }
    }

    @Test
    public void testToSelectWhenIsInsertWithSuccess() throws JSQLParserException {
        // prepare
        Statement sql = CCJSqlParserUtil.parse("INSERT INTO T (A, B) VALUES (1, NOW());");

        // act
        Optional<PlainSelect> result = parser(false).toSelect(sql);
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isPresent().get()
                .hasToString("SELECT * FROM T WHERE (A = 1 AND B = NOW())");
    }


    @Test
    public void testToSelectWhenIsUpdateWithSuccess() throws JSQLParserException {
        // prepare
        Statement sql = CCJSqlParserUtil.parse("UPDATE T SET B = NOW(), C = 1 WHERE A = 1;");

        // act
        Optional<PlainSelect> result = parser(false).toSelect(sql);
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isPresent().get()
                .hasToString("SELECT * FROM T WHERE A = 1");
    }

    @Test
    public void testToSelectWhenIsDeleteWithSuccess() throws JSQLParserException {
        // prepare
        Statement sql = CCJSqlParserUtil.parse("DELETE FROM T WHERE A = 1;");

        // act
        Optional<PlainSelect> result = parser(false).toSelect(sql);
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isPresent().get()
                .hasToString("SELECT * FROM T WHERE A = 1");
    }

    @Test
    public void testToSelectWhenIsInvalidStatementWithSuccess() throws JSQLParserException {
        // prepare
        Statement sql = CCJSqlParserUtil.parse("SELECT * FROM T;");

        // act
        Optional<PlainSelect> result = parser(false).toSelect(sql);
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isNotPresent();
    }

    @Test
    public void testToValuesWhenNumberWithSuccess() {
        // prepare
        List.of(1, 1L, new BigInteger("1"), 1.1D, 1.1F, new BigDecimal("1.1"), true).forEach(value -> {
            LOGGER.debug("{}", value.getClass());

            // act
            Values result = parser(false).toValues(new Object[]{value});
            LOGGER.debug("Result: {}", result);

            // check
            assertThat(result).isNotNull()
                    .extracting(Values::getExpressions).asList()
                    .hasSize(1)
                    .map(Object::toString)
                    .contains(value.toString());
        });
    }

    @Test
    public void testToValuesWhenStringWithSuccess() {
        // prepare
        String value = "abc";

        // act
        Values result = parser(false).toValues(new Object[]{value});
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isNotNull()
                .extracting(Values::getExpressions).asList()
                .hasSize(1)
                .map(Object::toString)
                .contains(format("'{}'", value));
    }

    @Test
    public void testToValuesWhenDateWithSuccess() {
        // prepare
        String value = "2021-10-24";

        // act
        Values result = parser(false).toValues(new Object[]{value});
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isNotNull()
                .extracting(Values::getExpressions).asList()
                .hasSize(1)
                .map(Object::toString)
                .contains(format("DATE '{}'", value));
    }

    @Test
    public void testToValuesWhenDatetimeWithSuccess() {
        // prepare
        String value = "2015-12-24 12:34:56.789";

        // act
        Values result = parser(false).toValues(new Object[]{value});
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isNotNull()
                .extracting(Values::getExpressions).asList()
                .hasSize(1)
                .map(Object::toString)
                .contains(format("TIMESTAMP '{}'", value));
    }

    @Test
    public void testToWhereWhenUpdateSetWithSuccess() {
        // prepare
        UpdateSet set = new UpdateSet();
        set.add(new Column("A"), new StringValue("abc"));
        set.add(new Column("B"), new LongValue("1"));

        // act
        Expression result = parser(false).toWhere(set);
        LOGGER.debug("Result: {}", result);

        // check
        assertThat(result).isNotNull()
                .isOfAnyClassIn(MultiAndExpression.class)
                .hasToString("(A = 'abc' AND B = 1)");
    }







    @Test
    public void testSqlSelectCountWhenTableFromWithSuccess() {
        Select result = parser(false).sqlSelectCountFrom("T");
        LOGGER.debug("Result: {}", result);
        assertEquals("SELECT count(*) FROM T", result.toString());
    }

    @Test
    public void testSqlDeleteFromWithSuccess() {
        Delete result = parser(false).toDelete("T");
        LOGGER.debug("Result: {}", result);
        assertEquals("DELETE FROM T", result.toString());
    }

    @Test
    public void testSqlDb2() throws JSQLParserException {
        String sql = "SELECT 'MINUTES', TO_CHAR(CURRENT_DATE - CAST(col1 AS NUMERIC) DAYS, 'YYYYMMDD') FROM \"table1\" WHERE col2 = 'DAYS'";
        Statement result = SQLParser.parseStatement(sql);
        LOGGER.debug("Result query: {}", result);
        assertThat(result).hasToString(
                "SELECT 'MINUTES', TO_CHAR(CURRENT_DATE - CAST(col1 AS NUMERIC) DAY, 'YYYYMMDD') " +
                        "FROM \"table1\" WHERE col2 = 'DAYS'");

        sql = "SELECT * FROM table1 WHERE " +
                "TO_TIMESTAMP(TO_CHAR(FECHA1), 'YYYYMMDD') <= CURRENT_TIMESTAMP AND " +
                "TO_TIMESTAMP(TO_CHAR(FECHA1), 'YYYYMMDD') >= (CURRENT_TIMESTAMP - 1 MINUTES) AND " +
                "TO_TIMESTAMP(TO_CHAR(FECHA2), 'YYYYMMDD') <= CURRENT_TIMESTAMP AND " +
                "TO_TIMESTAMP(TO_CHAR(FECHA2), 'YYYYMMDD') >= (CURRENT_TIMESTAMP - 1 MINUTE)";
        result = SQLParser.parseStatement(sql);
        LOGGER.debug("Result query: {}", result);
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
        LOGGER.debug("Result query: {}", result);
        assertThat(result).hasToString(sql);
    }

    private SQLParser parser(boolean autoTrim) {
        return new SQLParser(DatabaseType.OTHER, autoTrim);
    }

}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.wakamiti.database.jdbc.DatabaseType;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.DatabaseHelper.isDateOrDateTime;
import static es.iti.wakamiti.database.DatabaseHelper.unquotedRegex;


/**
 * Provides methods for parsing SQL statements, constructing SQL queries, and generating
 * WHERE clause expressions. It also handles conversions between different data types and
 * formats SQL expressions.
 *
 * @author María Galbis Calomarde - mgalbis@iti.es
 */
public class SQLParser {

    private static final String TRIM = "trim";
    private static final String COUNT = "count";
    private static final Map<Class<?>, java.util.function.Function<Object, Expression>> CONVERTER = Map.of(
            Integer.class, o -> new LongValue(o.toString()),
            Long.class, o -> new LongValue(o.toString()),
            Double.class, o -> new DoubleValue(o.toString()),
            Float.class, o -> new DoubleValue(o.toString()),
            BigInteger.class, o -> new LongValue(o.toString()),
            BigDecimal.class, o -> new DoubleValue(o.toString()),
            String.class, o -> new StringValue(o.toString())
    );

    private static final Map<DatabaseType, java.util.function.Function<Column, Function>> TRIM_FUNCTION = Map.of(
            DatabaseType.POSTGRESQL, c -> SQLParser.trimFunction(new CastExpression()
                    .withLeftExpression(c)
                    .withUseCastKeyword(true)
                    .withType(new ColDataType("text"))),
            DatabaseType.OTHER, SQLParser::trimFunction
    );

    private static final Map<DatabaseType, java.util.function.Function<String, Expression>> DATE_CAST = Map.of(
            DatabaseType.OTHER, e -> new DateTimeLiteralExpression()
                    .withType(
                            DatabaseHelper.isDate(e) ?
                                    DateTimeLiteralExpression.DateTime.DATE :
                                    DateTimeLiteralExpression.DateTime.TIMESTAMP
                    )
                    .withValue(new StringValue(e).toString()),
            DatabaseType.SQLSERVER, e -> new CastExpression()
                    .withLeftExpression(new StringValue(e))
                    .withUseCastKeyword(true)
                    .withType(new ColDataType((//DatabaseHelper.isDate(e) ?
                            DateTimeLiteralExpression.DateTime.DATE /*:
                            DateTimeLiteralExpression.DateTime.TIMESTAMP*/).toString()))
    );

    private static final Map<DatabaseType, java.util.function.Function<String, String>> FORMAT = Map.of(
            DatabaseType.POSTGRESQL, c -> !c.startsWith("\"") ? String.format("\"%s\"", c) : c,
            DatabaseType.OTHER, c -> c
    );

    private final DatabaseType type;

    public SQLParser() {
        this.type = DatabaseType.OTHER;
    }

    public SQLParser(DatabaseType type) {
        this.type = type;
    }

    /**
     * Parses multiple SQL statements from the given SQL string.
     *
     * @param sql The SQL string containing the statements
     * @return A list of parsed SQL statements
     * @throws JSQLParserException If an error occurs during parsing
     */
    public static List<Statement> parseStatements(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(fix(sql));
    }

    /**
     * Parses a single SQL statement from the given SQL string.
     *
     * @param sql The SQL string containing the statement
     * @return The parsed SQL statement
     * @throws JSQLParserException If an error occurs during parsing
     */
    public static Statement parseStatement(String sql) throws JSQLParserException {
        List<Statement> statements = parseStatements(sql);
        if (statements.size() > 1) {
            throw new JSQLParserException("There are more than one sentence");
        }
        return statements.get(0);
    }

    /**
     * Parses an expression from the given expression string.
     *
     * @param expression The expression string
     * @return The parsed expression
     * @throws JSQLParserException If an error occurs during parsing
     */
    public static Expression parseExpression(String expression) throws JSQLParserException {
        return CCJSqlParserUtil.parseExpression(fix(expression));
    }

    /**
     * Fixes the provided SQL script by replacing certain unquoted occurrences
     * of temporal units with their singular forms.
     *
     * @param script The SQL script to fix
     * @return The fixed SQL script
     */
    private static String fix(String script) {
        String regex = "(?i)(YEAR|QUARTER|MONTH|WEEK|DAY|HOUR|MINUTE|SECOND|MICROSECOND)S";
        return script.replaceAll(unquotedRegex(regex), "$1");
    }

    /**
     * Creates a SQL function to trim whitespace from the given column expression.
     *
     * @param column The expression representing the column to trim
     * @return The SQL function for trimming whitespace from the column
     */
    private static Function trimFunction(Expression column) {
        Function f = new Function();
        f.setName(TRIM);
        f.setParameters(new ExpressionList<>(column));
        return f;
    }

    /**
     * Converts the given object to an SQL expression.
     *
     * @param o The object to convert
     * @return The SQL expression representing the object
     */
    public Expression toExpression(Object o) {
        if (o == null) return new NullValue();
        if (o instanceof Number) {
            return CONVERTER.get(o.getClass()).apply(o);
        } else if (o instanceof Boolean) {
            return new LongValue(((Boolean) o).compareTo(false));
        } else {
            return isDateOrDateTime(o.toString()) ? dateCast(o.toString()) : new StringValue(o.toString());
        }
    }

    /**
     * Converts a SELECT statement from an INSERT, UPDATE, or DELETE statement.
     *
     * @param statement The SQL statement to convert
     * @return An Optional containing the PlainSelect object if the statement type
     * is supported, otherwise an empty Optional
     */
    public Optional<PlainSelect> toSelect(Statement statement) {
        AtomicReference<Optional<PlainSelect>> result = new AtomicReference<>(Optional.empty());

        statement.accept(new StatementVisitorAdapter() {
            @Override
            public void visit(Delete delete) {
                result.set(Optional.of(createSelect(delete.getTable(), delete.getWhere())));
            }

            @Override
            public void visit(Update update) {
                result.set(Optional.of(createSelect(update.getTable(), update.getWhere())));
            }

            @Override
            public void visit(Insert insert) {
                if (insert.getSelect() instanceof PlainSelect) {
                    result.set(Optional.of(createSelect(insert.getTable(),
                            ((PlainSelect) insert.getSelect()).getWhere())));
                } else {
                    result.set(Optional.of(createSelect(insert.getTable(),
                            createWhere(insert.getColumns(), insert.getValues().getExpressions()))));
                }
            }
        });
        return result.get();
    }

    /**
     * Converts an array of values to a Values object.
     *
     * @param values The array of values to convert
     * @return The Values object containing the expressions representing the values
     */
    public Values toValues(Object[] values) {
        Values result = new Values();
        List<Expression> expressions = Stream.of(values).map(this::toExpression).collect(Collectors.toList());
        result.addExpressions(expressions);
        return result;
    }

    /**
     * Converts a list of UpdateSet objects to a single Expression representing the WHERE
     * clause with AND logical operators.
     *
     * @param us The list of UpdateSet objects to convert
     * @return The Expression representing the WHERE clause with AND logical operators
     */
    public Expression toWhere(List<UpdateSet> us) {
        return new MultiAndExpression(us.stream().map(this::toWhere).collect(Collectors.toList()));
    }

    /**
     * Converts an UpdateSet object to an Expression representing the WHERE clause.
     *
     * @param us The UpdateSet object to convert
     * @return The Expression representing the WHERE clause
     */
    public Expression toWhere(UpdateSet us) {
        return createWhere(new ArrayList<>(us.getColumns()), us.getValues());
    }

    /**
     * Formats the columns in the given expression using the provided mapper function.
     *
     * @param expression The expression containing the columns to format
     * @param mapper     The function used to format the column names
     */
    public void formatColumns(Expression expression, UnaryOperator<String> mapper) {
        if (Objects.isNull(expression)) return;
        expression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Column column) {
                column.setColumnName(mapper.apply(column.getColumnName()));
            }
        });
    }

    /**
     * Creates a PlainSelect object with the specified table and select items.
     *
     * @param table The table to select from
     * @param items The select items to include in the select clause
     * @return The PlainSelect object representing the SQL SELECT statement
     */
    private PlainSelect createSelect(Table table, SelectItem<?>... items) {
        return createSelect(table, null, items);
    }

    /**
     * Creates a PlainSelect object with the specified table and WHERE clause.
     *
     * @param table The table to select from
     * @param where The WHERE clause expression
     * @return The PlainSelect object representing the SQL SELECT statement
     */
    private PlainSelect createSelect(Table table, Expression where) {
        return createSelect(table, where, new SelectItem<>(new AllColumns()));
    }

    /**
     * Creates a PlainSelect object with the specified table, WHERE clause, and select items.
     *
     * @param table The table to select from
     * @param where The WHERE clause expression
     * @param items The select items to include in the select clause
     * @return The PlainSelect object representing the SQL SELECT statement
     */
    private PlainSelect createSelect(Table table, Expression where, SelectItem<?>... items) {
        PlainSelect body = new PlainSelect();
        body.addSelectItems(items);
        body.setFromItem(table);
        if (where != null) body.setWhere(where);
        return body;
    }

    /**
     * Creates a WHERE clause expression based on the given columns and values.
     *
     * @param columns The list of columns
     * @param values  The list of values corresponding to the columns
     * @return The WHERE clause expression
     */
    public Expression createWhere(List<Column> columns, ExpressionList<?> values) {
        List<Expression> result = new LinkedList<>();
        for (int i = 0; i < values.size(); i++) {
            Expression expression = values.get(i);
            Column column = columns.get(i);
            column.setColumnName(format(column.getColumnName()));
            if (expression instanceof NullValue) {
                result.add(isNull(column));
            } else {
                result.add(equalsTo(column, expression));
            }
        }
        return new MultiAndExpression(result);
    }

    /**
     * Creates a WHERE clause expression using the specified columns with null control.
     *
     * @param columns The array of column names
     * @return The WHERE clause expression
     */
    public Expression createWhere(String[] columns) {
        return createWhere(columns, true);
    }

    /**
     * Creates a WHERE clause expression using the specified columns with an optional null control.
     *
     * @param columns     The array of column names
     * @param nullControl Indicates whether null control should be applied
     * @return The WHERE clause expression
     */
    public Expression createWhere(String[] columns, boolean nullControl) {
        List<Expression> expressions = Stream.of(columns)
                .map(column -> equalsTo(new Column(column), new JdbcParameter())).map(exp -> {
                    if (!nullControl) return exp;
                    IsNullExpression cIsNull = new IsNullExpression();
                    cIsNull.setLeftExpression(exp.getLeftExpression());
                    IsNullExpression vIsNull = new IsNullExpression();
                    vIsNull.setLeftExpression(exp.getRightExpression());
                    return new Parenthesis(new OrExpression(exp, new Parenthesis(new AndExpression(cIsNull, vIsNull))));
                }).collect(Collectors.toCollection(LinkedList::new));
        return new MultiAndExpression(expressions);
    }

    /**
     * Creates an {@code IS NULL} expression for the specified column.
     *
     * @param column The column to check for NULL
     * @return The {@code IS NULL} expression
     */
    private IsNullExpression isNull(Column column) {
        IsNullExpression isNull = new IsNullExpression();
        isNull.setLeftExpression(column);
        return isNull;
    }

    /**
     * Creates an equality expression between the specified column and expression.
     * If the expression is a string value and not a date or date-time value, trims
     * the column before comparison.
     *
     * @param column     The column to compare
     * @param expression The expression to compare against
     * @return The equality expression
     */
    private EqualsTo equalsTo(Column column, Expression expression) {
        EqualsTo exp = new EqualsTo();
        if (expression instanceof StringValue && !isDateOrDateTime(((StringValue) expression).getValue())) {
            exp.setLeftExpression(trim(column));
        } else {
            exp.setLeftExpression(column);
        }
        if (expression instanceof StringValue && isDateOrDateTime(((StringValue) expression).getValue())) {
            expression = dateCast(((StringValue) expression).getValue());
        }
        exp.setRightExpression(expression);
        return exp;
    }

    /**
     * Constructs a SELECT statement querying specified columns from the given table.
     *
     * @param table   The name of the table
     * @param columns An array of column names to be selected
     * @return The constructed SELECT statement
     */
    public Select sqlSelectFrom(String table, String[] columns) {
        List<Column> columnList = Stream.of(columns).map(Column::new).collect(Collectors.toCollection(LinkedList::new));
        return createSelect(new Table(table), new SelectItem<>(new ExpressionList<>(columnList)));
    }

    /**
     * Constructs a SELECT statement querying the count of all columns from the given table.
     *
     * @param table The name of the table
     * @return The constructed SELECT statement
     */
    public Select sqlSelectCountFrom(String table) {
        Function count = new Function().withName(COUNT).withParameters(new AllColumns());
        return createSelect(new Table(table), new SelectItem<>(count));
    }

    /**
     * Constructs a SELECT statement querying the count of specified columns from the given
     * table with the specified values.
     *
     * @param table   The name of the table
     * @param columns An array of column names to be selected
     * @param values  An array of values to match against the specified columns
     * @return The constructed SELECT statement
     */
    public Select sqlSelectCountFrom(String table, String[] columns, Object[] values) {
        Function count = new Function().withName(COUNT).withParameters(new AllColumns());
        List<Column> columnList = Stream.of(columns)
                .map(Column::new).collect(Collectors.toCollection(LinkedList::new));
        Expression[] expressions = Stream.of(values).map(this::toExpression).toArray(Expression[]::new);
        return createSelect(new Table(table),
                createWhere(columnList, new ExpressionList<>(expressions)), new SelectItem<>(count));
    }

    /**
     * Constructs a DELETE statement for the specified table without any conditions.
     *
     * @param table The name of the table
     * @return The constructed DELETE statement
     */
    public Delete toDelete(String table) {
        return toDelete(table, new String[0]);
    }

    /**
     * Constructs a DELETE statement for the specified table with the specified conditions
     * on columns.
     *
     * @param table   The name of the table
     * @param columns An array of column names representing the conditions
     * @return The constructed DELETE statement
     */
    public Delete toDelete(String table, String[] columns) {
        Delete delete = new Delete();
        delete.setTable(new Table(table));
        if (columns != null && columns.length > 0) delete.setWhere(createWhere(columns));
        return delete;
    }

    /**
     * Constructs an INSERT statement for the specified table with the given column-value mappings.
     *
     * @param table  The name of the table
     * @param values A map representing column-value pairs to be inserted
     * @return The constructed INSERT statement
     */
    public Insert toInsert(String table, Map<String, Object> values) {
        Insert insert = new Insert();
        insert.setTable(new Table(this.format(table)));
        insert.setColumns(new ExpressionList<>(
                values.keySet().stream()
                        .map(this::format)
                        .map(Column::new)
                        .collect(Collectors.toCollection(LinkedList::new))
        ));
        insert.setSelect(new Values(new ParenthesedExpressionList<>(new ExpressionList<>(
                values.values().stream()
                        .map(this::toExpression)
                        .collect(Collectors.toCollection(LinkedList::new))
        ))));
        return insert;
    }

    /**
     * Constructs an UPDATE statement for the specified table with the given column-value
     * mappings and condition.
     *
     * @param table The name of the table
     * @param sets  A map representing column-value pairs to be updated
     * @param where The condition for updating records
     * @return The constructed UPDATE statement
     */
    public Update toUpdate(String table, Map<String, Object> sets, Expression where) {
        Update update = new Update();
        update.setTable(new Table(this.format(table)));
        update.setUpdateSets(new LinkedList<>());
        sets.entrySet().stream()
                .map(set -> new UpdateSet(new Column(set.getKey()), toExpression(set.getValue())))
                .forEach(update::addUpdateSet);
        update.setWhere(where);
        return update;
    }

    /**
     * Constructs an UPDATE statement for the specified table with the given column-value
     * mappings and conditions.
     *
     * @param table The name of the table
     * @param sets  A map representing column-value pairs to be updated
     * @param where A map representing conditions for updating records
     * @return The constructed UPDATE statement
     */
    public Update toUpdate(String table, Map<String, Object> sets, Map<String, Object> where) {
        return toUpdate(table, sets, createWhere(where));
    }

    /**
     * Constructs a DELETE statement for the specified table with the given conditions.
     *
     * @param table The name of the table
     * @param where A map representing conditions for deleting records
     * @return The constructed DELETE statement
     */
    public Delete toDelete(String table, Map<String, Object> where) {
        Delete delete = new Delete();
        delete.setTable(new Table(this.format(table)));
        delete.setWhere(createWhere(where));
        return delete;
    }

    /**
     * Creates a WHERE clause expression based on the given column-value mappings.
     *
     * @param where A map representing column-value pairs for conditions
     * @return The constructed WHERE clause expression
     */
    public Expression createWhere(Map<String, Object> where) {
        List<Column> columns = where.keySet().stream()
                .map(this::format).map(Column::new)
                .collect(Collectors.toCollection(LinkedList::new));
        List<Expression> values = where.values().stream().map(this::toExpression)
                .collect(Collectors.toCollection(LinkedList::new));
        return createWhere(columns, new ExpressionList<>(values));
    }

    /**
     * Trims the specified column based on the database type.
     *
     * @param column The column to trim
     * @return The trimmed column function
     */
    private Function trim(Column column) {
        return Optional.ofNullable(TRIM_FUNCTION.get(type))
                .orElse(TRIM_FUNCTION.get(DatabaseType.OTHER))
                .apply(column);
    }

    /**
     * Casts the specified expression to a date format based on the database type.
     *
     * @param expression The expression to cast
     * @return The casted expression
     */
    private Expression dateCast(String expression) {
        return Optional.ofNullable(DATE_CAST.get(type))
                .orElse(DATE_CAST.get(DatabaseType.OTHER))
                .apply(expression);
    }

    /**
     * Formats the given name based on the database type.
     *
     * @param name The name to format
     * @return The formatted name
     */
    public String format(String name) {
        return Optional.ofNullable(FORMAT.get(type))
                .orElse(FORMAT.get(DatabaseType.OTHER))
                .apply(name);
    }

    /**
     * Removes quotes from the specified string and converts it to uppercase.
     *
     * @param str The string to unquote and uppercase
     * @return The unquoted and uppercase string
     */
    public String unquote(String str) {
        return str.replaceAll("^\"|\"$|^`|`$", "").toUpperCase();
    }

}

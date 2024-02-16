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
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.DatabaseHelper.isDateOrDateTime;
import static es.iti.wakamiti.database.DatabaseHelper.unquotedRegex;


/**
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

    public static List<Statement> parseStatements(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(fix(sql));
    }

    public static Statement parseStatement(String sql) throws JSQLParserException {
        List<Statement> statements = parseStatements(sql);
        if (statements.size() > 1) {
            throw new JSQLParserException("There are more than one sentence");
        }
        return statements.get(0);
    }

    public static Expression parseExpression(String expression) throws JSQLParserException {
        return CCJSqlParserUtil.parseExpression(fix(expression));
    }

    public static String extractTableName(String sql) throws JSQLParserException {
        AtomicReference<String> table = new AtomicReference<>();
        SQLParser.parseStatement(sql).accept(new StatementVisitorAdapter() {

            @Override
            public void visit(Delete delete) {
                table.set(delete.getTable().getName());
            }

            @Override
            public void visit(Update update) {
                table.set(update.getTable().getName());
            }

            @Override
            public void visit(Insert insert) {
                table.set(insert.getTable().getName());
            }

            @Override
            public void visit(Drop drop) {
                table.set(drop.getName().getName());
            }

            @Override
            public void visit(Truncate truncate) {
                table.set(truncate.getTable().getName());
            }

            @Override
            public void visit(CreateIndex createIndex) {
                table.set(createIndex.getTable().getName());
            }

            @Override
            public void visit(CreateTable createTable) {
                table.set(createTable.getTable().getName());
            }

            @Override
            public void visit(CreateView createView) {
                table.set(createView.getView().getName());
            }

            @Override
            public void visit(AlterView alterView) {
                table.set(alterView.getView().getName());
            }

            @Override
            public void visit(Alter alter) {
                table.set(alter.getTable().getName());
            }

            @Override
            public void visit(Upsert upsert) {
                table.set(upsert.getTable().getName());
            }

        });
        if (table.get() == null) {
            throw new JSQLParserException("Cannot extract table name of statement: " + sql);
        }
        return table.get();
    }

    private static String fix(String script) {
        String regex = "(?i)(YEAR|QUARTER|MONTH|WEEK|DAY|HOUR|MINUTE|SECOND|MICROSECOND)S";
        return script.replaceAll(unquotedRegex(regex), "$1");
    }

    private static Function trimFunction(Expression column) {
        Function f = new Function();
        f.setName(TRIM);
        f.setParameters(new ExpressionList<>(column));
        return f;
    }

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
                if (insert.getSelect() != null && insert.getSelect() instanceof PlainSelect) {
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

    public Values toValues(Object[] values) {
        Values result = new Values();
        List<Expression> expressions = Stream.of(values).map(this::toExpression).collect(Collectors.toList());
        result.addExpressions(expressions);
        return result;
    }

    public void formatColumns(Expression expression, java.util.function.Function<String, String> mapper) {
        if (Objects.isNull(expression)) return;
        expression.accept(new ExpressionVisitorAdapter() {
            public void visit(Column column) {
                column.setColumnName(mapper.apply(column.getColumnName()));
            }
        });
    }

    private PlainSelect createSelect(Table table, SelectItem item) {
        return createSelect(table, item, null);
    }

    private PlainSelect createSelect(Table table, Expression where) {
        return createSelect(table, new SelectItem(new AllColumns()), where);
    }

    private PlainSelect createSelect(Table table, SelectItem item, Expression where) {
        PlainSelect body = new PlainSelect();
        body.addSelectItems(item);
        body.setFromItem(table);
        if (where != null) body.setWhere(where);
        return body;
    }

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

    public Expression createWhere(String[] columns) {
        return createWhere(columns, true);
    }

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

    private IsNullExpression isNull(Column column) {
        IsNullExpression isNull = new IsNullExpression();
        isNull.setLeftExpression(column);
        return isNull;
    }

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

    public Select sqlSelectFrom(String table, String[] columns) {
        List<Column> columnList = Stream.of(columns).map(Column::new).collect(Collectors.toCollection(LinkedList::new));
        return createSelect(new Table(table), new SelectItem(new ExpressionList<>(columnList)));
    }

    public Select sqlSelectCountFrom(String table) {
        Function count = new Function().withName(COUNT).withParameters(new AllColumns());
        return createSelect(new Table(table), new SelectItem<>(count));
    }

    public Select sqlSelectCountFrom(String table, String[] columns, Object[] values) {
        Function count = new Function().withName(COUNT).withParameters(new AllColumns());
        List<Column> columnList = Stream.of(columns)//.map(this::format)
                .map(Column::new).collect(Collectors.toCollection(LinkedList::new));
        Expression[] expressions = Stream.of(values).map(this::toExpression).toArray(Expression[]::new);
        return createSelect(new Table(table), new SelectItem<>(count),
                createWhere(columnList, new ExpressionList<>(expressions)));
    }

    public Delete toDelete(String table) {
        return toDelete(table, new String[0]);
    }

    public Delete toDelete(String table, String[] columns) {
        Delete delete = new Delete();
        delete.setTable(new Table(table));
        if (columns != null && columns.length > 0) delete.setWhere(createWhere(columns));
        return delete;
    }

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

    public Update toUpdate(String table, Map<String, Object> sets, Map<String, Object> where) {
        Update update = new Update();
        update.setTable(new Table(this.format(table)));
        sets.entrySet().stream()
                .map(set -> new UpdateSet(new Column(set.getKey()), toExpression(set.getValue())))
                .forEach(update::addUpdateSet);
        update.setWhere(createWhere(where));
        return update;
    }

    public Delete toDelete(String table, Map<String, Object> where) {
        Delete delete = new Delete();
        delete.setTable(new Table(this.format(table)));
        delete.setWhere(createWhere(where));
        return delete;
    }

    private Expression createWhere(Map<String, Object> where) {
        List<Column> columns = where.keySet().stream()
                .map(this::format).map(Column::new)
                .collect(Collectors.toCollection(LinkedList::new));
        List<Expression> values = where.values().stream().map(this::toExpression)
                .collect(Collectors.toCollection(LinkedList::new));
        return createWhere(columns, new ExpressionList<>(values));
    }

    private Function trim(Column column) {
        return Optional.ofNullable(TRIM_FUNCTION.get(type))
                .orElse(TRIM_FUNCTION.get(DatabaseType.OTHER))
                .apply(column);
    }

    private Expression dateCast(String expression) {
        return Optional.ofNullable(DATE_CAST.get(type))
                .orElse(DATE_CAST.get(DatabaseType.OTHER))
                .apply(expression);
    }

    public String format(String name) {
        return Optional.ofNullable(FORMAT.get(type))
                .orElse(FORMAT.get(DatabaseType.OTHER))
                .apply(name);
    }

    public String unquote(String str) {
        return str.replaceAll("^\"|\"$|^`|`$", "").toUpperCase();
    }

}

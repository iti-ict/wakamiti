/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.database;

import es.iti.wakamiti.database.dataset.DataSet;
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
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLParser {

    private static final String TRIM = "trim";
    private static final String COUNT = "count";

    private final CaseSensitivity caseSensitivity;

    public SQLParser(CaseSensitivity caseSensitivity) {
        this.caseSensitivity = caseSensitivity;
    }

    public List<Statement> parseStatements(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(sql);
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
                    result.set(Optional.of(createSelect(insert.getTable(), ((PlainSelect) insert.getSelect()).getWhere())));
                } else {
                    result.set(Optional.of(createSelect(insert.getTable(), createWhere(insert.getColumns(), insert.getValues().getExpressions()))));
                }
            }
        });
        return result.get();
    }

    public Select toSelect(DataSet dataSet) {
        PlainSelect select = new PlainSelect();
        select.addSelectItems(new AllColumns());
        select.setFromItem(new Table(dataSet.table()));
        select.setWhere(createWhere(dataSet.columns()));
        return select;
    }

    private PlainSelect createSelect(FromItem table) {
        return createSelect(table, (Expression) null);
    }

    private PlainSelect createSelect(FromItem table, SelectItem item) {
        return createSelect(table, item, null);
    }

    private PlainSelect createSelect(FromItem table, Expression where) {
        return createSelect(table, new SelectItem(new AllColumns()), where);
    }

    private PlainSelect createSelect(FromItem table, SelectItem item, Expression where) {
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
            if (expression instanceof NullValue) {
                IsNullExpression isNull = new IsNullExpression();
                isNull.setLeftExpression(columns.get(i));
                result.add(isNull);
                continue;
            }
            if (expression.getClass().getSimpleName().contains("Value")) {
                EqualsTo equalsTo = new EqualsTo();
                Function f = new Function();
                f.setName(TRIM);
                f.setParameters(new ExpressionList(columns.get(i)));
                equalsTo.setLeftExpression(f);
                equalsTo.setRightExpression(expression);
                result.add(equalsTo);
            }
        }
        return new MultiAndExpression(result);
    }

    public Expression createWhere(String[] columns) {
        return createWhere(columns, true);
    }

    public Expression createWhere(String[] columns, boolean nullControl) {
        List<Expression> expressions = Stream.of(columns).map(caseSensitivity::format).map(column -> {
            EqualsTo exp = new EqualsTo();
            Function f = new Function();
            f.setName(TRIM);
            f.setParameters(new ExpressionList(new Column(column)));
            exp.setLeftExpression(f);
            exp.setRightExpression(new JdbcParameter());
            return exp;
        }).map(exp -> {
            if (!nullControl) return exp;
            IsNullExpression cIsNull = new IsNullExpression();
            cIsNull.setLeftExpression(exp.getLeftExpression());
            IsNullExpression vIsNull = new IsNullExpression();
            vIsNull.setLeftExpression(exp.getRightExpression());
            return new Parenthesis(new OrExpression(exp, new Parenthesis(new AndExpression(cIsNull, vIsNull))));
        }).collect(Collectors.toCollection(LinkedList::new));
        return new MultiAndExpression(expressions);
    }

    public Select sqlSelectFrom(String table) {
        return createSelect(new Table(caseSensitivity.format(table)));
    }

    public Select sqlSelectFrom(String table, String[] columns) {
        return createSelect(new Table(caseSensitivity.format(table)), createWhere(columns));
    }

    public Select sqlSelectCountFrom(String table) {
        Function count = new Function().withName(COUNT).withParameters(new AllColumns());
        return createSelect(new Table(caseSensitivity.format(table)), new SelectItem(count));
    }

    public Select sqlSelectCountFrom(String table, String where) throws JSQLParserException {
        Function count = new Function().withName(COUNT).withParameters(new AllColumns());
        return createSelect(new Table(caseSensitivity.format(table)), new SelectItem(count), CCJSqlParserUtil.parseCondExpression(where));
    }

    public Select sqlSelectCountFrom(String table, String[] columns) {
        Function count = new Function().withName(COUNT).withParameters(new AllColumns());
        return createSelect(new Table(caseSensitivity.format(table)), new SelectItem(count), createWhere(columns));
    }

    public Delete sqlDeleteFrom(String table) {
        return sqlDeleteFrom(table, null);
    }

    public Delete sqlDeleteFrom(String table, String[] columns) {
        Delete delete = new Delete();
        delete.setTable(new Table(caseSensitivity.format(table)));
        if (columns != null && columns.length > 0) delete.setWhere(createWhere(columns));
        return delete;
    }

    public Insert sqlInsertIntoValues(DataSet dataSet) {
        Insert insert = new Insert();
        insert.setTable(new Table(caseSensitivity.format(dataSet.table())));
        insert.setColumns(new ExpressionList<>(Stream.of(dataSet.columns()).map(caseSensitivity::format).map(Column::new).collect(Collectors.toCollection(LinkedList::new))));
        insert.setSelect(new Values(new ParenthesedExpressionList<>(new ExpressionList<>(Stream.of(dataSet.columns()).map(column -> new JdbcParameter()).collect(Collectors.toCollection(LinkedList::new))))));
        return insert;
    }

    public Update sqlUpdateSet(DataSet dataSet, String[] columns) {
        Update update = new Update();
        update.setTable(new Table(caseSensitivity.format(dataSet.table())));
        Stream.of(dataSet.columns()).filter(column -> !columns[0].equals(column)).forEach(column -> {
            update.addUpdateSet(new Column(column), new JdbcParameter());
        });
        update.setWhere(createWhere(columns, false));
        return update;
    }

    public String extractValue(Expression expression) {
        StringBuilder builder = new StringBuilder();
        expression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(DateTimeLiteralExpression literal) {
                builder.append(literal.getValue());
            }

            @Override
            public void visit(JdbcParameter value) {
                builder.append("?");
            }

            @Override
            public void visit(DoubleValue value) {
                builder.append(value.getValue());
            }

            @Override
            public void visit(LongValue value) {
                builder.append(value.getStringValue());
            }

            @Override
            public void visit(DateValue value) {
                builder.append(value.getValue());
            }

            @Override
            public void visit(TimeValue value) {
                builder.append(value.getValue());
            }

            @Override
            public void visit(TimestampValue value) {
                builder.append(value.getValue());
            }

            @Override
            public void visit(StringValue value) {
                builder.append(value.getValue());
            }

            @Override
            public void visit(HexValue hexValue) {
                builder.append(hexValue.getValue());
            }
        });
        return builder.toString();
    }

}
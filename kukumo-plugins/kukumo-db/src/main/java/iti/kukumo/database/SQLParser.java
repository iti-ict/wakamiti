/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.database;

import iti.kukumo.database.dataset.DataSet;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLParser {

    private final CaseSensitivity caseSensitivity;

    public SQLParser(CaseSensitivity caseSensitivity) {
        this.caseSensitivity = caseSensitivity;
    }

    public List<Statement> parseStatements(String sql) throws JSQLParserException {
        Statements stmt = CCJSqlParserUtil.parseStatements(sql);
        return stmt.getStatements();
    }

    public Optional<Select> toSelect(Statement statement) {
        Select result = new Select();
        statement.accept(new StatementVisitorAdapter() {
            @Override
            public void visit(Delete delete) {
                result.setSelectBody(createSelectBody(delete.getTable(), delete.getWhere()));
            }

            @Override
            public void visit(Update update) {
                result.setSelectBody(createSelectBody(update.getTable(), update.getWhere()));
            }

            @Override
            public void visit(Insert insert) {
                result.setSelectBody(createSelectBody(insert.getTable(),
                        createWhere(insert.getColumns(), ((ExpressionList) insert.getItemsList()).getExpressions())));
            }
        });
        return result.getSelectBody() == null ? Optional.empty() : Optional.of(result);
    }

    public Select toSelect(DataSet dataSet) {
        Select result = new Select();
        PlainSelect body = new PlainSelect();
        body.addSelectItems(new AllColumns());
        body.setFromItem(new Table(dataSet.table()));
        body.setWhere(createWhere(dataSet.columns()));
        result.setSelectBody(body);
        return result;
    }

    private SelectBody createSelectBody(FromItem table) {
        return createSelectBody(table, (Expression) null);
    }

    private SelectBody createSelectBody(FromItem table, SelectItem item) {
        return createSelectBody(table, item, null);
    }

    private SelectBody createSelectBody(FromItem table, Expression where) {
        return createSelectBody(table, new AllColumns(), where);
    }

    private SelectBody createSelectBody(FromItem table, SelectItem item, Expression where) {
        PlainSelect body = new PlainSelect();
        body.addSelectItems(item);
        body.setFromItem(table);
        if (where != null) body.setWhere(where);
        return body;
    }

    public Expression createWhere(List<Column> columns, List<Expression> values) {
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
                f.setName("trim");
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
        List<Expression> expressions = Stream.of(columns)
                .map(caseSensitivity::format)
                .map(column -> {
                    EqualsTo exp = new EqualsTo();
                    Function f = new Function();
                    f.setName("trim");
                    f.setParameters(new ExpressionList(new Column(column)));
                    exp.setLeftExpression(f);
                    exp.setRightExpression(new JdbcParameter());
                    return exp;
                })
                .map(exp -> {
                    if (!nullControl) return exp;
                    IsNullExpression cIsNull = new IsNullExpression();
                    cIsNull.setLeftExpression(exp.getLeftExpression());
                    IsNullExpression vIsNull = new IsNullExpression();
                    vIsNull.setLeftExpression(exp.getRightExpression());
                    return new Parenthesis(new OrExpression(exp, new Parenthesis(new AndExpression(cIsNull, vIsNull))));
                })
                .collect(Collectors.toCollection(LinkedList::new));
        return new MultiAndExpression(expressions);
    }

    public Select sqlSelectFrom(String table) {
        Select select = new Select();
        select.setSelectBody(createSelectBody(new Table(caseSensitivity.format(table))));
        return select;
    }

    public Select sqlSelectFrom(String table, String[] columns) {
        Select select = new Select();
        select.setSelectBody(createSelectBody(new Table(caseSensitivity.format(table)), createWhere(columns)));
        return select;
    }

    public Select sqlSelectCountFrom(String table) {
        Select select = new Select();
        Function count = new Function();
        count.setName("count");
        count.setAllColumns(true);
        SelectItem countAll = new SelectExpressionItem(count);
        select.setSelectBody(createSelectBody(new Table(caseSensitivity.format(table)), countAll));
        return select;
    }

    public Select sqlSelectCountFrom(String table, String where) throws JSQLParserException {
        Select select = new Select();
        Function count = new Function();
        count.setName("count");
        count.setAllColumns(true);
        SelectItem countAll = new SelectExpressionItem(count);
        select.setSelectBody(createSelectBody(new Table(caseSensitivity.format(table)), countAll, CCJSqlParserUtil.parseCondExpression(where)));
        return select;
    }

    public Select sqlSelectCountFrom(String table, String[] columns) {
        Select select = new Select();
        Function count = new Function();
        count.setName("count");
        count.setAllColumns(true);
        SelectItem countAll = new SelectExpressionItem(count);
        select.setSelectBody(createSelectBody(new Table(caseSensitivity.format(table)), countAll, createWhere(columns)));
        return select;
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
        insert.setColumns(
                Stream.of(dataSet.columns())
                        .map(caseSensitivity::format).map(Column::new)
                        .collect(Collectors.toCollection(LinkedList::new))
        );
        insert.setItemsList(new ExpressionList(
                Stream.of(dataSet.columns()).map(column -> new JdbcParameter())
                        .collect(Collectors.toCollection(LinkedList::new))
        ));
        return insert;
    }

    public Update sqlUpdateSet(DataSet dataSet, String[] columns) {
        Update update = new Update();
        update.setTable(new Table(caseSensitivity.format(dataSet.table())));
        List<String> setColumns = Stream.of(dataSet.columns())
                .filter(column -> !columns[0].equals(column))
                .collect(Collectors.toCollection(LinkedList::new));
        update.setColumns(setColumns.stream().map(Column::new).collect(Collectors.toCollection(LinkedList::new)));
        update.setExpressions(setColumns.stream().map(column -> new JdbcParameter()).collect(Collectors.toCollection(LinkedList::new)));
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
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.gherkin.parser.internal;


import static es.iti.wakamiti.core.gherkin.parser.internal.StringUtils.join;

import java.util.*;
import java.util.stream.Stream;

import es.iti.wakamiti.core.gherkin.parser.Background;
import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.DataTable;
import es.iti.wakamiti.core.gherkin.parser.DocString;
import es.iti.wakamiti.core.gherkin.parser.Examples;
import es.iti.wakamiti.core.gherkin.parser.Feature;
import es.iti.wakamiti.core.gherkin.parser.GherkinDocument;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.Node;
import es.iti.wakamiti.core.gherkin.parser.ParserException;
import es.iti.wakamiti.core.gherkin.parser.Scenario;
import es.iti.wakamiti.core.gherkin.parser.ScenarioDefinition;
import es.iti.wakamiti.core.gherkin.parser.ScenarioOutline;
import es.iti.wakamiti.core.gherkin.parser.Step;
import es.iti.wakamiti.core.gherkin.parser.TableCell;
import es.iti.wakamiti.core.gherkin.parser.TableRow;
import es.iti.wakamiti.core.gherkin.parser.Tag;
import es.iti.wakamiti.core.gherkin.parser.internal.AstNode;
import es.iti.wakamiti.core.gherkin.parser.internal.GherkinLineSpan;
import es.iti.wakamiti.core.gherkin.parser.internal.Parser;
import es.iti.wakamiti.core.gherkin.parser.internal.Token;


public class GherkinAstBuilder implements Parser.Builder<GherkinDocument> {

    private Deque<es.iti.wakamiti.core.gherkin.parser.internal.AstNode> stack;
    private List<Comment> currentComments;
    private final Map<Token, List<Comment>> comments = new HashMap<>();


    public GherkinAstBuilder() {
        reset();
    }


    @Override
    public void reset() {
        stack = new ArrayDeque<>();
        stack.push(new es.iti.wakamiti.core.gherkin.parser.internal.AstNode(Parser.RuleType.None));
        currentComments = new ArrayList<>();
        comments.clear();
    }


    private es.iti.wakamiti.core.gherkin.parser.internal.AstNode currentNode() {
        return stack.peek();
    }


    @Override
    public void build(Token token) {
        Parser.RuleType ruleType = Parser.RuleType.cast(token.matchedType);
        switch (token.matchedType) {
        case Comment:
            currentComments.add(new Comment(getLocation(token, 0), token.matchedText));
            break;
        case FeatureLine:
        case ScenarioLine:
        case ScenarioOutlineLine:
        case BackgroundLine:
        case StepLine:
            comments.put(token, currentComments);
            currentComments = new ArrayList<>();
            currentNode().add(ruleType, token, currentComments);
            break;
        default:
            currentNode().add(ruleType, token, currentComments);
        }
    }


    @Override
    public void startRule(Parser.RuleType ruleType) {
        stack.push(new es.iti.wakamiti.core.gherkin.parser.internal.AstNode(ruleType));
    }


    @Override
    public void endRule(Parser.RuleType ruleType) {
        es.iti.wakamiti.core.gherkin.parser.internal.AstNode node = stack.pop();
        Object transformedNode = getTransformedNode(node);
        currentNode().add(node.ruleType, transformedNode);
    }


    private Object getTransformedNode(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        switch (node.ruleType) {
        case Step:
            return getTransformedStep(node);
        case DocString:
            return getTransformedDocString(node);
        case DataTable:
            return new DataTable(getTableRows(node));
        case Background:
            return getTransformedBackground(node);
        case Scenario_Definition:
            return getTransformedScenarioDefinition(node);
        case Examples_Definition:
            return getTransformedExamplesDefinition(node);
        case Examples_Table:
            return getTableRows(node);
        case Description:
            return getTransformedDescription(node);
        case Feature:
            return getTransformedFeature(node);
        case GherkinDocument:
            Feature feature = node.getSingle(Parser.RuleType.Feature, null);
            return new GherkinDocument(feature);
        default:
            return node;
        }
    }


    private Object getTransformedStep(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        Token stepLine = node.getToken(Parser.TokenType.StepLine);
        Node stepArg = node.getSingle(Parser.RuleType.DataTable, null);
        if (stepArg == null) {
            stepArg = node.getSingle(Parser.RuleType.DocString, null);
        }
        return new Step(
            getLocation(stepLine, 0), stepLine.matchedKeyword, stepLine.matchedText, stepArg, comments(node)
        );
    }


    private Object getTransformedDocString(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        Token separatorToken = node.getTokens(Parser.TokenType.DocStringSeparator).get(0);
        String contentType = separatorToken.matchedText.length() > 0 ? separatorToken.matchedText
                        : null;
        List<Token> lineTokens = node.getTokens(Parser.TokenType.Other);
        StringBuilder content = new StringBuilder();
        boolean newLine = false;
        for (Token lineToken : lineTokens) {
            if (newLine) {
                content.append("\n");
            }
            newLine = true;
            content.append(lineToken.matchedText);
        }
        return new DocString(getLocation(separatorToken, 0), contentType, content.toString());
    }


    private Object getTransformedBackground(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        Token backgroundLine = node.getToken(Parser.TokenType.BackgroundLine);
        String description = getDescription(node);
        List<Step> steps = getSteps(node);
        return new Background(
            getLocation(backgroundLine, 0), backgroundLine.matchedKeyword, backgroundLine.matchedText, description, steps, comments(node)
        );
    }


    private Object getTransformedScenarioDefinition(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        List<Tag> tags = getTags(node);
        es.iti.wakamiti.core.gherkin.parser.internal.AstNode scenarioNode = node.getSingle(Parser.RuleType.Scenario, null);

        if (scenarioNode != null) {
            Token scenarioLine = scenarioNode.getToken(Parser.TokenType.ScenarioLine);
            String description = getDescription(scenarioNode);
            List<Step> steps = getSteps(scenarioNode);

            return new Scenario(
                tags, getLocation(scenarioLine, 0), scenarioLine.matchedKeyword, scenarioLine.matchedText, description, steps, comments(scenarioNode)
            );
        } else {
            es.iti.wakamiti.core.gherkin.parser.internal.AstNode scenarioOutlineNode = node.getSingle(Parser.RuleType.ScenarioOutline, null);
            if (scenarioOutlineNode == null) {
                throw new IllegalArgumentException("Internal grammar error");
            }
            Token scenarioOutlineLine = scenarioOutlineNode.getToken(Parser.TokenType.ScenarioOutlineLine);
            String description = getDescription(scenarioOutlineNode);
            List<Step> steps = getSteps(scenarioOutlineNode);

            List<Examples> examplesList = scenarioOutlineNode
                .getItems(Parser.RuleType.Examples_Definition);

            return new ScenarioOutline(
                tags, getLocation(scenarioOutlineLine, 0), scenarioOutlineLine.matchedKeyword, scenarioOutlineLine.matchedText, description, steps, examplesList, comments(scenarioOutlineNode)
            );
        }
    }


    private Object getTransformedExamplesDefinition(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        List<Tag> tags = getTags(node);
        es.iti.wakamiti.core.gherkin.parser.internal.AstNode examplesNode = node.getSingle(Parser.RuleType.Examples, null);
        Token examplesLine = examplesNode.getToken(Parser.TokenType.ExamplesLine);
        String description = getDescription(examplesNode);
        List<TableRow> rows = examplesNode.getSingle(Parser.RuleType.Examples_Table, null);
        TableRow tableHeader = rows != null && !rows.isEmpty() ? rows.get(0) : null;
        List<TableRow> tableBody = rows != null && !rows.isEmpty() ? rows.subList(1, rows.size())
                        : null;
        return new Examples(
            getLocation(examplesLine, 0), tags, examplesLine.matchedKeyword, examplesLine.matchedText, description, tableHeader, tableBody
        );
    }


    private Object getTransformedDescription(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        List<Token> lineTokens = node.getTokens(Parser.TokenType.Other);
        // Trim trailing empty lines
        int end = lineTokens.size();
        while (end > 0 && lineTokens.get(end - 1).matchedText.matches("\\s*")) {
            end--;
        }
        lineTokens = lineTokens.subList(0, end);
        return join(token -> token.matchedText, "\n", lineTokens);
    }


    private Object getTransformedFeature(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        es.iti.wakamiti.core.gherkin.parser.internal.AstNode header = node
            .getSingle(Parser.RuleType.Feature_Header, new es.iti.wakamiti.core.gherkin.parser.internal.AstNode(Parser.RuleType.Feature_Header));
        if (header == null) {
            return null;
        }
        List<Tag> tags = getTags(header);
        Token featureLine = header.getToken(Parser.TokenType.FeatureLine);
        if (featureLine == null) {
            return null;
        }
        List<ScenarioDefinition> scenarioDefinitions = new ArrayList<>();
        Background background = node.getSingle(Parser.RuleType.Background, null);
        if (background != null) {
            scenarioDefinitions.add(background);
        }
        scenarioDefinitions.addAll(node.<ScenarioDefinition>getItems(Parser.RuleType.Scenario_Definition));
        String description = getDescription(header);
        if (featureLine.matchedGherkinDialect == null) {
            return null;
        }
        String language = featureLine.matchedGherkinDialect.getLanguage();

        return new Feature(
            tags, getLocation(featureLine, 0), language, featureLine.matchedKeyword, featureLine.matchedText, description, scenarioDefinitions, comments(header)
        );
    }


    private List<TableRow> getTableRows(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        List<TableRow> rows = new ArrayList<>();
        for (Token token : node.getTokens(Parser.TokenType.TableRow)) {
            rows.add(new TableRow(getLocation(token, 0), getCells(token)));
        }
        ensureCellCount(rows);
        return rows;
    }


    private void ensureCellCount(List<TableRow> rows) {
        if (rows.isEmpty()) {
            return;
        }

        int cellCount = rows.get(0).getCells().size();
        for (TableRow row : rows) {
            if (row.getCells().size() != cellCount) {
                throw new ParserException.AstBuilderException(
                    "inconsistent cell count within the table", row.getLocation()
                );
            }
        }
    }


    private List<TableCell> getCells(Token token) {
        List<TableCell> cells = new ArrayList<>();
        for (GherkinLineSpan cellItem : token.mathcedItems) {
            cells.add(new TableCell(getLocation(token, cellItem.column), cellItem.text));
        }
        return cells;
    }


    private List<Step> getSteps(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        return node.getItems(Parser.RuleType.Step);
    }


    private Location getLocation(Token token, int column) {
        return column == 0 ? token.location : new Location(token.location.getLine(), column);
    }


    private String getDescription(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        return node.getSingle(Parser.RuleType.Description, null);
    }


    private List<Tag> getTags(es.iti.wakamiti.core.gherkin.parser.internal.AstNode node) {
        es.iti.wakamiti.core.gherkin.parser.internal.AstNode tagsNode = node.getSingle(Parser.RuleType.Tags, new es.iti.wakamiti.core.gherkin.parser.internal.AstNode(Parser.RuleType.None));
        if (tagsNode == null) {
            return new ArrayList<>();
        }

        List<Token> tokens = tagsNode.getTokens(Parser.TokenType.TagLine);
        List<Tag> tags = new LinkedList<>();
        for (Token token : tokens) {
            for (GherkinLineSpan tagItem : token.mathcedItems) {
                tags.add(new Tag(getLocation(token, tagItem.column), tagItem.text));
            }
        }
        return tags;
    }


    @Override
    public GherkinDocument getResult() {
        return currentNode().getSingle(Parser.RuleType.GherkinDocument, null);
    }


    private List<Comment> comments(AstNode node) {
        List<Token> tokens = Stream.of(Parser.TokenType.values()).map(node::getTokens)
            .collect(ArrayList::new, List::addAll, List::addAll);
        return tokens.stream().map(comments::get).filter(Objects::nonNull)
            .collect(ArrayList::new, List::addAll, List::addAll);
    }

}
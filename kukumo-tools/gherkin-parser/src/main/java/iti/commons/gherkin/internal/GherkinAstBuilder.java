/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.gherkin.internal;


import static iti.commons.gherkin.internal.Parser.RuleType.Scenario_Definition;
import static iti.commons.gherkin.internal.StringUtils.join;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import iti.commons.gherkin.Background;
import iti.commons.gherkin.Comment;
import iti.commons.gherkin.DataTable;
import iti.commons.gherkin.DocString;
import iti.commons.gherkin.Examples;
import iti.commons.gherkin.Feature;
import iti.commons.gherkin.GherkinDocument;
import iti.commons.gherkin.Location;
import iti.commons.gherkin.Node;
import iti.commons.gherkin.Scenario;
import iti.commons.gherkin.ScenarioDefinition;
import iti.commons.gherkin.ScenarioOutline;
import iti.commons.gherkin.Step;
import iti.commons.gherkin.TableCell;
import iti.commons.gherkin.TableRow;
import iti.commons.gherkin.Tag;
import iti.commons.gherkin.internal.Parser.Builder;
import iti.commons.gherkin.internal.Parser.RuleType;
import iti.commons.gherkin.internal.Parser.TokenType;


public class GherkinAstBuilder implements Builder<GherkinDocument> {

    private Deque<AstNode> stack;
    private List<Comment> currentComments;
    private final Map<Token, List<Comment>> comments = new HashMap<>();


    public GherkinAstBuilder() {
        reset();
    }


    @Override
    public void reset() {
        stack = new ArrayDeque<>();
        stack.push(new AstNode(RuleType.None));
        currentComments = new ArrayList<>();
        comments.clear();
    }


    private AstNode currentNode() {
        return stack.peek();
    }


    @Override
    public void build(Token token) {
        RuleType ruleType = RuleType.cast(token.matchedType);
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
    public void startRule(RuleType ruleType) {
        stack.push(new AstNode(ruleType));
    }


    @Override
    public void endRule(RuleType ruleType) {
        AstNode node = stack.pop();
        Object transformedNode = getTransformedNode(node);
        currentNode().add(node.ruleType, transformedNode);
    }


    private Object getTransformedNode(AstNode node) {
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
            Feature feature = node.getSingle(RuleType.Feature, null);
            return new GherkinDocument(feature);
        default:
            return node;
        }
    }


    private Object getTransformedStep(AstNode node) {
        Token stepLine = node.getToken(TokenType.StepLine);
        Node stepArg = node.getSingle(RuleType.DataTable, null);
        if (stepArg == null) {
            stepArg = node.getSingle(RuleType.DocString, null);
        }
        return new Step(
            getLocation(stepLine, 0), stepLine.matchedKeyword, stepLine.matchedText, stepArg, comments(node)
        );
    }


    private Object getTransformedDocString(AstNode node) {
        Token separatorToken = node.getTokens(TokenType.DocStringSeparator).get(0);
        String contentType = separatorToken.matchedText.length() > 0 ? separatorToken.matchedText
                        : null;
        List<Token> lineTokens = node.getTokens(TokenType.Other);
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


    private Object getTransformedBackground(AstNode node) {
        Token backgroundLine = node.getToken(TokenType.BackgroundLine);
        String description = getDescription(node);
        List<Step> steps = getSteps(node);
        return new Background(
            getLocation(backgroundLine, 0), backgroundLine.matchedKeyword, backgroundLine.matchedText, description, steps, comments(node)
        );
    }


    private Object getTransformedScenarioDefinition(AstNode node) {
        List<Tag> tags = getTags(node);
        AstNode scenarioNode = node.getSingle(RuleType.Scenario, null);

        if (scenarioNode != null) {
            Token scenarioLine = scenarioNode.getToken(TokenType.ScenarioLine);
            String description = getDescription(scenarioNode);
            List<Step> steps = getSteps(scenarioNode);

            return new Scenario(
                tags, getLocation(scenarioLine, 0), scenarioLine.matchedKeyword, scenarioLine.matchedText, description, steps, comments(scenarioNode)
            );
        } else {
            AstNode scenarioOutlineNode = node.getSingle(RuleType.ScenarioOutline, null);
            if (scenarioOutlineNode == null) {
                throw new IllegalArgumentException("Internal grammar error");
            }
            Token scenarioOutlineLine = scenarioOutlineNode.getToken(TokenType.ScenarioOutlineLine);
            String description = getDescription(scenarioOutlineNode);
            List<Step> steps = getSteps(scenarioOutlineNode);

            List<Examples> examplesList = scenarioOutlineNode
                .getItems(RuleType.Examples_Definition);

            return new ScenarioOutline(
                tags, getLocation(scenarioOutlineLine, 0), scenarioOutlineLine.matchedKeyword, scenarioOutlineLine.matchedText, description, steps, examplesList, comments(scenarioOutlineNode)
            );
        }
    }


    private Object getTransformedExamplesDefinition(AstNode node) {
        List<Tag> tags = getTags(node);
        AstNode examplesNode = node.getSingle(RuleType.Examples, null);
        Token examplesLine = examplesNode.getToken(TokenType.ExamplesLine);
        String description = getDescription(examplesNode);
        List<TableRow> rows = examplesNode.getSingle(RuleType.Examples_Table, null);
        TableRow tableHeader = rows != null && !rows.isEmpty() ? rows.get(0) : null;
        List<TableRow> tableBody = rows != null && !rows.isEmpty() ? rows.subList(1, rows.size())
                        : null;
        return new Examples(
            getLocation(examplesLine, 0), tags, examplesLine.matchedKeyword, examplesLine.matchedText, description, tableHeader, tableBody
        );
    }


    private Object getTransformedDescription(AstNode node) {
        List<Token> lineTokens = node.getTokens(TokenType.Other);
        // Trim trailing empty lines
        int end = lineTokens.size();
        while (end > 0 && lineTokens.get(end - 1).matchedText.matches("\\s*")) {
            end--;
        }
        lineTokens = lineTokens.subList(0, end);
        return join(token -> token.matchedText, "\n", lineTokens);
    }


    private Object getTransformedFeature(AstNode node) {
        AstNode header = node
            .getSingle(RuleType.Feature_Header, new AstNode(RuleType.Feature_Header));
        if (header == null) {
            return null;
        }
        List<Tag> tags = getTags(header);
        Token featureLine = header.getToken(TokenType.FeatureLine);
        if (featureLine == null) {
            return null;
        }
        List<ScenarioDefinition> scenarioDefinitions = new ArrayList<>();
        Background background = node.getSingle(RuleType.Background, null);
        if (background != null) {
            scenarioDefinitions.add(background);
        }
        scenarioDefinitions.addAll(node.<ScenarioDefinition>getItems(Scenario_Definition));
        String description = getDescription(header);
        if (featureLine.matchedGherkinDialect == null) {
            return null;
        }
        String language = featureLine.matchedGherkinDialect.getLanguage();

        return new Feature(
            tags, getLocation(featureLine, 0), language, featureLine.matchedKeyword, featureLine.matchedText, description, scenarioDefinitions, comments(header)
        );
    }


    private List<TableRow> getTableRows(AstNode node) {
        List<TableRow> rows = new ArrayList<>();
        for (Token token : node.getTokens(TokenType.TableRow)) {
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


    private List<Step> getSteps(AstNode node) {
        return node.getItems(RuleType.Step);
    }


    private Location getLocation(Token token, int column) {
        return column == 0 ? token.location : new Location(token.location.getLine(), column);
    }


    private String getDescription(AstNode node) {
        return node.getSingle(RuleType.Description, null);
    }


    private List<Tag> getTags(AstNode node) {
        AstNode tagsNode = node.getSingle(RuleType.Tags, new AstNode(RuleType.None));
        if (tagsNode == null) {
            return new ArrayList<>();
        }

        List<Token> tokens = tagsNode.getTokens(TokenType.TagLine);
        List<Tag> tags = new ArrayList<>();
        for (Token token : tokens) {
            for (GherkinLineSpan tagItem : token.mathcedItems) {
                tags.add(new Tag(getLocation(token, tagItem.column), tagItem.text));
            }
        }
        return tags;
    }


    @Override
    public GherkinDocument getResult() {
        return currentNode().getSingle(RuleType.GherkinDocument, null);
    }


    private List<Comment> comments(AstNode node) {
        List<Token> tokens = Stream.of(TokenType.values()).map(node::getTokens)
            .collect(ArrayList::new, List::addAll, List::addAll);
        return tokens.stream().map(comments::get).filter(Objects::nonNull)
            .collect(ArrayList::new, List::addAll, List::addAll);
    }

}

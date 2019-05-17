package iti.kukumo.gherkin.parser;

import gherkin.AstNode;
import gherkin.Parser;
import gherkin.ast.Comment;

import java.util.List;

public class CommentedAstNode extends AstNode {

    public CommentedAstNode(Parser.RuleType ruleType) {
        super(ruleType);
    }

    public void add(Parser.RuleType ruleType, Object obj, List<Comment> comments) {
        super.add(ruleType, obj);
    }
}

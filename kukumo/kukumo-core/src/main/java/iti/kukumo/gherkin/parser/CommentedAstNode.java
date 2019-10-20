/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.gherkin.parser;


import java.util.List;

import gherkin.AstNode;
import gherkin.Parser;
import gherkin.ast.Comment;


public class CommentedAstNode extends AstNode {

    public CommentedAstNode(Parser.RuleType ruleType) {
        super(ruleType);
    }


    public void add(Parser.RuleType ruleType, Object obj, List<Comment> comments) {
        super.add(ruleType, obj);
    }
}

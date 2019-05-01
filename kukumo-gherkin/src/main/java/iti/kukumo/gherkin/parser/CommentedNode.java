package iti.kukumo.gherkin.parser;

import java.util.List;

import gherkin.ast.Comment;

public interface CommentedNode {

    List<Comment> getComments();
}

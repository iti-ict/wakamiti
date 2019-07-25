package iti.kukumo.gherkin.parser;

import gherkin.ast.Comment;

import java.util.List;

public interface CommentedNode {

    List<Comment> getComments();
}

package iti.kukumo.gherkin.parser;

import gherkin.ast.Background;
import gherkin.ast.Comment;
import gherkin.ast.Location;
import gherkin.ast.Step;

import java.util.Collections;
import java.util.List;

public class CommentedBackground extends Background implements CommentedNode {

    private final List<Comment> comments;

    public CommentedBackground(
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<Comment> comments
    ) {
        super(location, keyword, name, description, steps);
        this.comments = comments == null ? Collections.emptyList() : comments;
    }

    public List<Comment> getComments() {
        return this.comments;
    }
}

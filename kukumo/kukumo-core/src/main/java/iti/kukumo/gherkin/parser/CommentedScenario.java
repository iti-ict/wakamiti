package iti.kukumo.gherkin.parser;

import gherkin.ast.*;

import java.util.Collections;
import java.util.List;

public class CommentedScenario extends Scenario implements CommentedNode {

    private final List<Comment> comments;

    public CommentedScenario(
            List<Tag> tags,
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<Comment> comments
    ) {
        super(tags, location, keyword, name, description, steps);
        this.comments = comments == null ? Collections.emptyList() : comments;
    }

    public List<Comment> getComments() {
        return this.comments;
    }
}

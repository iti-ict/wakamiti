package iti.kukumo.gherkin.parser;

import java.util.Collections;
import java.util.List;

import gherkin.ast.Comment;
import gherkin.ast.Location;
import gherkin.ast.Scenario;
import gherkin.ast.Step;
import gherkin.ast.Tag;

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

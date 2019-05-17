package iti.kukumo.gherkin.parser;

import java.util.Collections;
import java.util.List;

import gherkin.ast.Comment;
import gherkin.ast.Examples;
import gherkin.ast.Location;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.Tag;

public class CommentedScenarioOutline extends ScenarioOutline implements CommentedNode {

    private final List<Comment> comments;

    public CommentedScenarioOutline(
            List<Tag> tags,
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<Examples> examples,
            List<Comment> comments
    ) {
        super(tags, location, keyword, name, description, steps, examples);
        this.comments = comments == null ? Collections.emptyList() : comments;
    }

    public List<Comment> getComments() {
        return this.comments;
    }
}

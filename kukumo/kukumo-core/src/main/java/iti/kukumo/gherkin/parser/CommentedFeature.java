package iti.kukumo.gherkin.parser;

import gherkin.ast.*;

import java.util.Collections;
import java.util.List;

public class CommentedFeature extends Feature implements CommentedNode  {

    private final List<Comment> comments;

    public CommentedFeature(
            List<Tag> tags,
            Location location,
            String language,
            String keyword,
            String name,
            String description,
            List<ScenarioDefinition> children,
            List<Comment> comments
    ) {
        super(tags, location, language, keyword, name, description, children);
        this.comments = comments == null ? Collections.emptyList() : comments;
    }

    public List<Comment> getComments() {
        return this.comments;
    }
}

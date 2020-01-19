package iti.commons.gherkin;

import java.util.Collections;
import java.util.List;

public class Scenario extends ScenarioDefinition implements TaggedNode {

    private final List<Tag> tags;

    public Scenario(
        List<Tag> tags,
        Location location,
        String keyword,
        String name,
        String description,
        List<Step> steps,
        List<Comment> comments
    ) {
        super(location, keyword, name, description, steps, comments);
        this.tags = Collections.unmodifiableList(tags);
    }

    public List<Tag> getTags() {
        return tags;
    }
}

package iti.commons.gherkin;

import java.util.List;

public class Background extends ScenarioDefinition {

    public Background(
        Location location,
        String keyword,
        String name,
        String description,
        List<Step> steps,
        List<Comment> comments
    ) {
        super(location, keyword, name, description, steps, comments);
    }
}

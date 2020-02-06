package iti.commons.gherkin;

public class GherkinDocument extends Node {

    private final Feature feature;

    public GherkinDocument(
        Feature feature
    ) {
        super(null);
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

}

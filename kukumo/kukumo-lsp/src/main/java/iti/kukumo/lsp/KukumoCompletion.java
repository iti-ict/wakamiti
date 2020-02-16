package iti.kukumo.lsp;

import iti.commons.configurer.Configuration;
import iti.commons.gherkin.Comment;
import iti.commons.gherkin.CommentedNode;
import iti.commons.gherkin.Feature;
import iti.commons.gherkin.GherkinDocument;
import iti.commons.gherkin.GherkinParser;
import iti.commons.gherkin.Node;
import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.lsp.CompletionContextMap.CompletionContext;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.*;

public class KukumoCompletion {

    private final static BackendFactory backendFactory = Kukumo.instance().newBackendFactory();

    private final GherkinParser parser = new GherkinParser();

    private Configuration globalConfiguration = Kukumo.defaultConfiguration();
    private Configuration documentConfiguration = Configuration.empty();
    private Configuration contextConfiguration = Configuration.empty();
    private Backend backend = backendFactory.createNonRunnableBackend(globalConfiguration);
    private int maxSuggestions = 20;

    private String[] rawDocument;
    private GherkinDocument parsedDocument;
    private GherkinDocumentMap documentMap;


    public KukumoCompletion updateGlobalConfiguration(Configuration configuration) {
        this.globalConfiguration = configuration;
        return this;
    }

    public KukumoCompletion setMaxSuggestions(int maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
        return this;
    }

    public KukumoCompletion updateDocument(String document) {
        this.rawDocument = document.split("\n");
        try {
            this.parsedDocument = parser.parse(new StringReader(document));
            this.documentConfiguration = extractConfiguration(parsedDocument);
        } catch (Exception e) {
            this.parsedDocument = null;
            this.documentConfiguration = Configuration.empty();
        }
        this.backend = backendFactory.createNonRunnableBackend(documentConfiguration);
        this.documentMap = new GherkinDocumentMap(document);
        return this;
    }


    private Configuration extractConfiguration(GherkinDocument document) {
        Feature feature = document.getFeature();
        if (feature == null) {
            return Configuration.empty();
        }
        return extractConfigurationFromComments(feature.getComments());
    }



    private Configuration extractConfigurationFromComments(List<Comment> comments) {
        if (comments == null) {
            return Configuration.empty();
        }
        return Configuration.fromMap(comments.stream()
            .map(Comment::getText)
            .map(s->s.replace("#", "").strip())
            .map(line -> line.split(":"))
            .filter(line -> line.length == 2)
            .collect(toMap(line -> line[0].strip(), line -> line[1].strip()))
        );
    }



    public List<String> suggest(int lineNumber, String lineContent) {
        String strippedLine = lineContent.stripLeading();
        if (strippedLine.startsWith("#") && !strippedLine.contains(":")) {
            return suggestConfigurationProperties(lineContent);
        }
        if (documentMap.isStep(lineNumber,lineContent)) {
            return suggestSteps(lineContent);
        }
        return documentMap.followingKeywords(lineNumber-1).stream()
            .filter(k -> k.startsWith(strippedLine))
            .collect(toList());
    }


    private List<String> suggestConfigurationProperties(String lineContent) {
        String line = lineContent.strip().replace("#","").strip();
        return globalConfiguration.keyStream()
            .filter(key -> {
                return key.startsWith(line);
            })
            .collect(toList());
    }


    private List<String> suggestSteps(String lineContent) {
        for (String keyword : documentMap.dialect().getStepKeywords()) {
            if (lineContent.startsWith(keyword)) {
                lineContent = lineContent.substring(keyword.length());
                break;
            }
        }
        var suggestions = backend.getAvailableSteps(documentMap.locale(),true);
        if (suggestions.size() > maxSuggestions) {
            suggestions = backend.getAvailableSteps(documentMap.locale(),false);
        }
        String suggestionPrefix = lineContent;
        return suggestions.stream()
            .filter(suggestion -> suggestion.startsWith(suggestionPrefix))
            .collect(toList());
    }



}

package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import iti.kukumo.lsp.LoggerUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import iti.commons.configurer.Configuration;
import iti.commons.gherkin.Comment;
import iti.commons.gherkin.Feature;
import iti.commons.gherkin.GherkinParser;
import iti.commons.gherkin.ParserException;
import iti.commons.gherkin.ParserException.CompositeParserException;
import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GherkinDocument {

    private static final BackendFactory backendFactory = Kukumo.instance().newBackendFactory();
    private static final Logger LOGGER = LoggerFactory.getLogger("document.synchronization");
    private static final String DOTS = "---------------------------";

    private final GherkinParser parser = new GherkinParser();

    private Configuration globalConfiguration = Kukumo.defaultConfiguration();
    private Backend backend = backendFactory.createNonRunnableBackend(globalConfiguration);
    private int maxSuggestions = 20;

    private GherkinDocumentMap documentMap;
    private iti.commons.gherkin.GherkinDocument parsedDocument;
    private Exception parsingError;


    public GherkinDocument updateGlobalConfiguration(Configuration configuration) {
        this.globalConfiguration = configuration;
        return this;
    }

    public GherkinDocument setMaxSuggestions(int maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
        return this;
    }


    public synchronized GherkinDocument resetDocument(String document) {
        Configuration documentConfiguration;
        try {
            this.parsedDocument = parser.parse(new StringReader(document));
            documentConfiguration = extractConfiguration(parsedDocument);
            this.parsingError = null;
        } catch (Exception e) {
            this.parsingError = e;
            documentConfiguration = Configuration.empty();
        }
        this.backend = backendFactory.createNonRunnableBackend(documentConfiguration);
        this.documentMap = new GherkinDocumentMap(document);
        LOGGER.debug("{}{}{}",DOTS,documentMap.document().raw(),DOTS);
        return this;
    }


    public synchronized GherkinDocument updateDocument(TextRange range, String delta) {
        boolean requiresParsing = documentMap.replace(range,delta);
        if (requiresParsing) {
            resetDocument(documentMap.raw());
        }
        LOGGER.debug("{}{}{}",DOTS,documentMap.document().raw(),DOTS);
        return this;
    }



    private Configuration extractConfiguration(iti.commons.gherkin.GherkinDocument document) {
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



    public List<CompletionItem> collectCompletions(int lineNumber, int rowPosition) {
        return collectCompletions(
            lineNumber,
            documentMap.document().extract(TextRange.of(lineNumber,0,lineNumber,rowPosition))
        );
    }



    private List<CompletionItem> collectCompletions(int lineNumber, String lineContent) {
        String strippedLine = lineContent.stripLeading();
        if (strippedLine.startsWith("#") && !strippedLine.contains(":")) {
            return completeConfigurationProperties(lineContent);
        } else if (documentMap.isStep(lineNumber,lineContent)) {
            return completeSteps(lineContent);
        } else {
            return completeKeywords(lineNumber, strippedLine);
        }
    }



    private List<CompletionItem> completeConfigurationProperties(String lineContent) {
        String line = lineContent.strip().replace("#","").strip();
        return globalConfiguration.keyStream()
            .filter(key -> key.startsWith(line))
            .map(property -> completionProperty(property))
            .collect(toList());
    }


    private CompletionItem completionProperty(String property) {
        String suggestion = property+": <value>";
        var item = new CompletionItem(suggestion);
        item.setKind(CompletionItemKind.Property);
        return item;
    }


    private List<CompletionItem> completeSteps(String lineContent) {
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
            .map(step -> completionItem(step,CompletionItemKind.Text))
            .collect(toList());
    }


    private CompletionItem completionItem(String suggestion, CompletionItemKind kind) {
        var item = new CompletionItem(suggestion);
        item.setKind(kind);
        return item;
    }


    private List<CompletionItem> completeKeywords(int lineNumber, String strippedLine) {
        return documentMap.followingKeywords(lineNumber-1).stream()
            .filter(k -> k.startsWith(strippedLine))
            .map(keyword -> completionItem(keyword,CompletionItemKind.Keyword))
            .collect(toList());
    }






    public List<Diagnostic> collectDiagnostics() {
        if (parsingError == null) {
            return List.of();
        }
        parsingError.printStackTrace();
        if (parsingError instanceof ParserException) {
            return collectDiagnostics((ParserException)parsingError, new ArrayList<>());
        } else {
            return List.of(new Diagnostic(
                new Range(new Position(0,0),new Position(documentMap.document().lines(),0)),
                parsingError.toString(),
                DiagnosticSeverity.Error,
                ""
            ));
        }
    }



    private List<Diagnostic> collectDiagnostics(ParserException parsingError, List<Diagnostic> results) {
        if (parsingError instanceof CompositeParserException) {
            for (ParserException e : ((CompositeParserException)parsingError).getErrors()) {
                collectDiagnostics(e,results);
            }
        } else {
            int line = parsingError.getLocation().getLine();
            int column = parsingError.getLocation().getColumn();
            if (column == 0) {
                results.add(new Diagnostic(
                    new Range(new Position(line,column),new Position(line,column)),
                    parsingError.getMessage(),
                    DiagnosticSeverity.Error,
                    ""
                ));
            } else {
                line --;
                column --;
                results.add(new Diagnostic(
                    new Range(new Position(line,column),new Position(line,documentMap.document().line(line).length())),
                    parsingError.getMessage(),
                    DiagnosticSeverity.Error,
                    ""
                ));
            }
        }
        return results;
    }





    public String content() {
        return documentMap.document().raw();
    }


}

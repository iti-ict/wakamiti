package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import iti.commons.configurer.Configuration;
import iti.commons.gherkin.Comment;
import iti.commons.gherkin.Feature;
import iti.commons.gherkin.GherkinDocument;
import iti.commons.gherkin.GherkinParser;
import iti.commons.gherkin.ParserException;
import iti.commons.gherkin.ParserException.CompositeParserException;
import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;

public class GherkinCompleter {

    private static final BackendFactory backendFactory = Kukumo.instance().newBackendFactory();

    private final GherkinParser parser = new GherkinParser();

    private Configuration globalConfiguration = Kukumo.defaultConfiguration();
    private Backend backend = backendFactory.createNonRunnableBackend(globalConfiguration);
    private int maxSuggestions = 20;

    private GherkinDocumentMap documentMap;
    private GherkinDocument parsedDocument;
    private Exception parsingError;


    public GherkinCompleter updateGlobalConfiguration(Configuration configuration) {
        this.globalConfiguration = configuration;
        return this;
    }

    public GherkinCompleter setMaxSuggestions(int maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
        return this;
    }


    public synchronized GherkinCompleter resetDocument(String document) {
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

        return this;
    }


    public synchronized void updateDocument(TextRange range, String delta) {
        boolean requiresParsing = false;
        if (range.isSingleLine() || range.isEmpty()) {
            requiresParsing = documentMap.updateLine(range.startLine(), range.toLineRange(), delta);
        } else {
            requiresParsing = true;
        }
        if (requiresParsing) {
            resetDocument(documentMap.replaceDocumentSegment(range,delta));
        }
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



    public List<CompletionItem> suggest(int lineNumber, int rowPosition) {
        return suggest(lineNumber,documentMap.lineContent(lineNumber,LineRange.of(0,rowPosition)));
    }



    public List<CompletionItem> suggest(int lineNumber, String lineContent) {
        String strippedLine = lineContent.stripLeading();
        if (strippedLine.startsWith("#") && !strippedLine.contains(":")) {
            return suggestConfigurationProperties(lineContent);
        }
        else if (documentMap.isStep(lineNumber,lineContent)) {
            return suggestSteps(lineContent);
        }
        else {
            return suggestKeywords(lineNumber, strippedLine);
        }
    }



    private List<CompletionItem> suggestConfigurationProperties(String lineContent) {
        String line = lineContent.strip().replace("#","").strip();
        return
            globalConfiguration.keyStream()
            .filter(key -> key.startsWith(line))
            .map(property -> completionProperty(property))
            .collect(toList())
        ;
    }


    private List<CompletionItem> suggestSteps(String lineContent) {
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



    private List<CompletionItem> suggestKeywords(int lineNumber, String strippedLine) {
        return
            documentMap.followingKeywords(lineNumber-1).stream()
            .filter(k -> k.startsWith(strippedLine))
            .map(keyword -> completionItem(keyword,CompletionItemKind.Keyword))
            .collect(toList());
    }



    private CompletionItem completionItem(String suggestion, CompletionItemKind kind) {
        var item = new CompletionItem(suggestion);
        item.setKind(kind);
        return item;
    }


    private CompletionItem completionProperty(String property) {
        String suggestion = property+": <value>";
        var item = new CompletionItem(suggestion);
        item.setKind(CompletionItemKind.Property);
        return item;
    }

    public String currentContent() {
        return documentMap.currentContent();
    }



    public List<Diagnostic> diagnostics() {
        if (parsingError != null) {
            parsingError.printStackTrace();
            if (parsingError instanceof ParserException) {
                return diagnostics((ParserException)parsingError, new ArrayList<>());
            } else {
               return List.of(new Diagnostic(
                    new Range(new Position(0,0),new Position(documentMap.lines().size(),0)),
                    parsingError.toString(),
                    DiagnosticSeverity.Error,
                    ""
                ));
            }
        } else {
            return List.of();
        }
    }



    private List<Diagnostic> diagnostics(ParserException parsingError, List<Diagnostic> results) {
        if (parsingError instanceof CompositeParserException) {
            for (ParserException e : ((CompositeParserException)parsingError).getErrors()) {
                diagnostics(e,results);
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
                    new Range(new Position(line,column),new Position(line,documentMap.lines().get(line).length())),
                    parsingError.getMessage(),
                    DiagnosticSeverity.Error,
                    ""
                ));
            }
        }
        return results;
    }



}

package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.toList;

import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.*;

import iti.commons.configurer.Configuration;
import iti.commons.gherkin.*;
import iti.commons.gherkin.ParserException.CompositeParserException;
import iti.kukumo.api.*;
import iti.kukumo.util.Pair;


public class GherkinDocumentAssessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("document.synchronization");
    private static final String DOTS = "---------------------------";
    private static final GherkinParser DEFAULT_PARSER = new GherkinParser();
    private static final Kukumo kukumo = Kukumo.instance();


    private final GherkinParser parser;
    private final Function<Configuration, Hinter> hinterProvider;
    private final Map<Range,List<CodeAction>> undefinedStepQuickFixes = new HashMap<>();

    private Configuration globalConfiguration;
    private Configuration documentConfiguration;
    private Configuration hinterConfiguration;
    private Hinter hinter;
    private int maxSuggestions = 20;


    private GherkinDocumentMap documentMap;
    private Exception parsingError;


    public GherkinDocumentAssessor(String document) {
        this(
            DEFAULT_PARSER,
            kukumo::createHinterFor,
            Kukumo.defaultConfiguration(),
            document
        );
    }


    public GherkinDocumentAssessor(
        GherkinParser parser,
        Function<Configuration, Hinter> hinterProvider,
        Configuration globalConfiguration,
        String document
    ) {
        this.parser = parser;
        this.hinterProvider = hinterProvider;
        this.globalConfiguration = globalConfiguration;
        resetDocument(document);
    }



    public GherkinDocumentAssessor updateGlobalConfiguration(Configuration configuration) {
        this.globalConfiguration = configuration;
        return this;
    }

    public GherkinDocumentAssessor setMaxSuggestions(int maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
        return this;
    }






    public synchronized GherkinDocumentAssessor resetDocument(String document) {
        if (!document.isBlank()) {
            try {
                this.documentConfiguration = extractDocumentConfiguration(document);
                this.parsingError = null;
            } catch (Exception e) {
                this.documentConfiguration = Configuration.empty();
                this.parsingError = e;
            }
        } else {
            this.documentConfiguration = Configuration.empty();
            this.parsingError = null;
        }
        hinterConfiguration = globalConfiguration.append(documentConfiguration);
        this.hinter = hinterProvider.apply(hinterConfiguration);
        this.documentMap = new GherkinDocumentMap(document);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}{}{}",DOTS,documentMap.document().rawText(),DOTS);
        }
        return this;
    }


    public synchronized GherkinDocumentAssessor updateDocument(TextRange range, String delta) {
        boolean requiresParsing = documentMap.replace(range,delta);
        if (requiresParsing) {
            resetDocument(documentMap.rawContent());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}{}{}",DOTS,documentMap.document().rawText(),DOTS);
        }
        return this;
    }



    private Configuration extractDocumentConfiguration(String document) {
        // TODO: parsing is an intensive operation, it should work if we just
        //       take the lines starting with # until the first keyword appears
        //       Parsing should be delayed as long as possible
        iti.commons.gherkin.GherkinDocument parsedDocument = parser.parse(new StringReader(document));
        Feature feature = parsedDocument.getFeature();
        if (feature == null) {
            return Configuration.empty();
        }
        return extractConfigurationFromComments(feature.getComments());
    }



    private Configuration extractConfigurationFromComments(List<Comment> comments) {
        if (comments == null) {
            return Configuration.empty();
        }
        return Configuration.fromMap(
            comments.stream()
            .map(Comment::getText)
            .filter(s->s.contains(":"))
            .map(s->s.replace("#", ""))
            .map(s -> new Pair<>(
                s.substring(0, s.indexOf(':')).strip(),
                s.substring(s.indexOf(':')+1).strip())
            )
            .collect(Pair.toMap())
        );
    }



    public List<CompletionItem> collectCompletions(int lineNumber, int rowPosition) {
        return collectCompletions(
            lineNumber,
            documentMap.document().extractRange(TextRange.of(lineNumber,0,lineNumber,rowPosition))
        );
    }



    private List<CompletionItem> collectCompletions(int lineNumber, String lineContent) {
        String strippedLine = lineContent.stripLeading();
        if (strippedLine.startsWith("#") && !strippedLine.contains(":")) {
            return completeConfigurationProperties(lineContent);
        } else if (documentMap.isStep(lineNumber,strippedLine)) {
            return completeSteps(strippedLine);
        } else {
            return completeKeywords(lineNumber, strippedLine);
        }
    }



    private List<CompletionItem> completeConfigurationProperties(String lineContent) {
        String line = lineContent.strip().replace("#","").strip();
        return hinter.getAvailableProperties()
            .stream()
            .filter(property -> property.startsWith(line))
            .map(this::completionProperty)
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
        var suggestions = hinter.getExpandedAvailableSteps();
        if (suggestions.isEmpty()) {
            LOGGER.debug(
                "no steps available! used configuration for hinter is:\n{}",
                hinterConfiguration
            );
        }
        if (suggestions.size() > maxSuggestions) {
            suggestions = hinter.getCompactAvailableSteps();
        }
        String suggestionPrefix = lineContent;
        return suggestions.stream()
            .filter(suggestion -> suggestion.startsWith(suggestionPrefix))
            .map(this::completionStep)
            .collect(toList());
    }


    private CompletionItem completionStep(String step) {
        var item = new CompletionItem(step);
        item.setKind(CompletionItemKind.Interface);
        String insertText = step;

        int snippetArgumentCount = 0;
        var stepSnippetPattern = Pattern.compile("\\*|\\{[^\\}]*\\}");
        Matcher m = stepSnippetPattern.matcher(step);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            snippetArgumentCount ++;
            String capture = insertText.substring(m.start(), m.end());
            capture = capture.replace("}", "\\}");
            capture = "${"+snippetArgumentCount+":"+capture+"}";
            m.appendReplacement(sb, Matcher.quoteReplacement(capture));
        }
        m.appendTail(sb);

        if (snippetArgumentCount > 0) {
            item.setInsertText(sb.toString());
            item.setInsertTextFormat(InsertTextFormat.Snippet);
        }

        item.setDocumentation(step);
        return item;
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
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (parsingError instanceof ParserException) {
            collectDiagnosticsFromParserException((ParserException)parsingError, diagnostics);
        } else if (parsingError != null){
            Range errorRange = new Range(
                new Position(0,0),
                new Position(documentMap.document().numberOfLines(),0)
            );
            diagnostics.add(diagnosticError(errorRange,parsingError.toString()));
        }
        collectDiagnosticsFromUndefinedSteps(diagnostics);
        return diagnostics;
    }




    private void collectDiagnosticsFromUndefinedSteps(List<Diagnostic> diagnostics) {
        clearUndefinedStepQuickFixes();
        String [] lines = documentMap.document().extractLines();
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber];
            String stripLine = line.strip();
            if (!documentMap.isStep(lineNumber, stripLine)) {
                continue;
            }
            var keywordRange = documentMap.detectStepKeyword(lineNumber, line);
            var step = documentMap.removeKeyword(lineNumber, stripLine);
            if (!hinter.isValidStep(step)) {
                var errorRange = new Range(
                    new Position(lineNumber, keywordRange.endLinePosition()),
                    new Position(lineNumber, line.length())
                );
                var stepDiagnostics = diagnosticError(errorRange, "Undefined step");
                diagnostics.add(stepDiagnostics);
                registerUndefinedStepQuickFixes(step, stepDiagnostics, errorRange);
            }
        }
    }


    private void clearUndefinedStepQuickFixes() {
        undefinedStepQuickFixes.clear();
    }


    private void registerUndefinedStepQuickFixes(String step, Diagnostic diagnostic, Range range) {
        var codeActions = undefinedStepQuickFixes
            .computeIfAbsent(range, x->new ArrayList<>());
        for (String hint : hinter.getHintsForInvalidStep(step, maxSuggestions, true)) {
            TextEdit textEdit = new TextEdit(range, hint);
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
            textDocumentEdit.setEdits(List.of(textEdit));
            WorkspaceEdit edit = new WorkspaceEdit(List.of(Either.forLeft(textDocumentEdit)));
            CodeAction codeAction = new CodeAction("Replace step with: "+hint);
            codeAction.setIsPreferred(Boolean.TRUE);
            codeAction.setDiagnostics(List.of(diagnostic));
            codeAction.setEdit(edit);
            codeActions.add(codeAction);
        }
    }



    public List<CodeAction> retrieveQuickFixes(Diagnostic errorDiagnostic) {
        LOGGER.debug("XXXXXXXXXXXXX QuickFix for error {}", errorDiagnostic);
        LOGGER.debug("XXXXXXXXXXXXX Computed Fixes are: {}", undefinedStepQuickFixes);
        return undefinedStepQuickFixes.getOrDefault(errorDiagnostic.getRange(), List.of());
    }



    private Diagnostic diagnosticError(Range range, String error) {
        return new Diagnostic(
            range,
            error,
            DiagnosticSeverity.Error,
            ""
        );
    }


    private Diagnostic diagnosticHint(Range range, String hint) {
        return new Diagnostic(
            range,
            hint,
            DiagnosticSeverity.Hint,
            ""
        );
    }

    private void collectDiagnosticsFromParserException(
        ParserException parsingError,
        List<Diagnostic> results
    ) {
        if (parsingError instanceof CompositeParserException) {
            for (ParserException e : ((CompositeParserException)parsingError).getErrors()) {
                collectDiagnosticsFromParserException(e,results);
            }
        } else {
            int lineNumber = parsingError.getLocation().getLine();
            int column = parsingError.getLocation().getColumn();
            Range range = (column == 0 ?
                emptyRange(lineNumber, column) :
                wholeLineRange(lineNumber-1, column-1)
            );
            results.add(diagnosticError(range,parsingError.getMessage()));
        }
    }




    private Range wholeLineRange(int lineNumber, int column) {
        return new Range(
            new Position(lineNumber,column),
            new Position(lineNumber,documentMap.document().extractLine(lineNumber).length())
        );
    }



    private Range emptyRange(int lineNumber, int column) {
        return new Range(
            new Position(lineNumber,column),
            new Position(lineNumber,column)
        );
    }



    public String content() {
        return documentMap.document().rawText();
    }


    Configuration globalConfiguration() {
        return this.globalConfiguration;
    }


    Configuration documentConfiguration() {
        return this.documentConfiguration;
    }

}

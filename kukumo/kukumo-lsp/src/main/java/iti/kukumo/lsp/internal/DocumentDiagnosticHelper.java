/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.lsp.internal;

import java.util.*;
import java.util.stream.*;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import iti.commons.gherkin.*;
import iti.commons.gherkin.ParserException.CompositeParserException;

public class DocumentDiagnosticHelper {


	private final GherkinDocumentAssessor assessor;
	private final Map<Range,List<CodeAction>> quickFixes = new HashMap<>();



	public DocumentDiagnosticHelper(GherkinDocumentAssessor assessor) {
		this.assessor = assessor;
	}


	public List<Diagnostic> collectDiagnostics() {
		quickFixes.clear();
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (assessor.parsingError != null) {
            collectDiagnosticsFromError(diagnostics);
        } else {
        	collectDiagnosticsFromContent(diagnostics);
        }
        return diagnostics;
    }


	public List<CodeAction> retrieveQuickFixes(Diagnostic errorDiagnostic) {
		return quickFixes.getOrDefault(errorDiagnostic.getRange(), List.of());
	}



	private void collectDiagnosticsFromError(List<Diagnostic> diagnostics) {
        if (assessor.parsingError instanceof ParserException) {
            collectDiagnosticsFromParserException((ParserException)assessor.parsingError, diagnostics);
        } else {
            Range errorRange = new Range(
                new Position(0,0),
                new Position(assessor.documentMap.document().numberOfLines(),0)
            );
            diagnostics.add(diagnostic(
        		DiagnosticSeverity.Error,
        		uri(),
        		errorRange,
        		assessor.parsingError.toString()
    		));
        }
    }


    private void collectDiagnosticsFromContent(List<Diagnostic> diagnostics) {
    	collectDiagnosticsFromUndefinedSteps(diagnostics);
    	collectDiagnosticsFromMissingId(diagnostics);
        if (assessor.parsedDocument != null) {
        	var numScenarios = assessor.parsedDocument.getFeature().getChildren().stream()
    			.filter(ScenarioDefinition.class::isInstance)
    			.count();
        	if (numScenarios == 0L) {
        		diagnostics.add(diagnostic(
    				DiagnosticSeverity.Warning,
    				uri(),
    				emptyRange(),
    				"No scenarios defined"
				));
        	}
        }
    }



	private void collectDiagnosticsFromUndefinedSteps(List<Diagnostic> diagnostics) {

		var additionalInfo = assessor.additionalInfo;
		var documentMap = assessor.documentMap;
		var hinter = assessor.hinter;

        if (additionalInfo.hasRedefinitionDefinitionTag) {
        	return;
        }

        String [] lines = documentMap.document().extractLines();
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber];
            String stripLine = line.strip();
            if (line.isBlank() || !documentMap.isStep(lineNumber, stripLine)) {
            	continue;
            }
            int marginLeft = line.length() - line.stripLeading().length();
            var step = documentMap.removeKeyword(lineNumber, stripLine);
            if (!hinter.isValidStep(step)) {
                var errorRange = new Range(
                    new Position(lineNumber, marginLeft),
                    new Position(lineNumber, line.length())
                );
                var stepDiagnostics = diagnostic(
            		DiagnosticSeverity.Error,
            		uri(),
            		errorRange,
            		"Undefined step",
            		step
        		);
                diagnostics.add(stepDiagnostics);
                registerUndefinedStepQuickFixes(step, stepDiagnostics, errorRange);
            }
        }
    }



	private void collectDiagnosticsFromMissingId(List<Diagnostic> diagnostics) {

		var additionalInfo = assessor.additionalInfo;
		var documentMap = assessor.documentMap;

        String [] lines = documentMap.document().extractLines();

        int lastScenarioLineNumber = -1;
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber];
            String stripLine = line.strip();
            if (line.isBlank()) {
            	continue;
            }
            int marginLeft = line.length() - line.stripLeading().length();

            if (!documentMap.detectScenarioKeyword(lineNumber, stripLine).isEmpty()) {
            	var ids = documentMap.segmentsInLines(
        			lastScenarioLineNumber+1,
        			lineNumber-1,
        			additionalInfo.idTagPattern,
        			1
    			);
            	if (ids.isEmpty()) {
            		var range = wholeLineRange(lineNumber, marginLeft);
            		var diagnostic = diagnostic(
        				DiagnosticSeverity.Warning,
        				uri(),
        				range,
        				"This scenario should have an ID tag"
    				);
            		diagnostics.add(diagnostic);
            		registerMissingIdQuickFix(diagnostic);
            	}
            	lastScenarioLineNumber = lineNumber;
            }
        }
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
            results.add(diagnostic(DiagnosticSeverity.Error,uri(),range,parsingError.getMessage()));
        }
    }


    private void registerUndefinedStepQuickFixes(String step, Diagnostic diagnostic, Range range) {
        var codeActions = quickFixes.computeIfAbsent(range, x->new ArrayList<>());
        var hinter = assessor.hinter;
        for (String hint : hinter.getHintsForInvalidStep(step, assessor.maxSuggestions, true)) {
            TextEdit textEdit = new TextEdit(range, hint);
            var textDocument = new VersionedTextDocumentIdentifier(assessor.uri(),null);
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
            textDocumentEdit.setTextDocument(textDocument);
            textDocumentEdit.setEdits(List.of(textEdit));
            WorkspaceEdit edit = new WorkspaceEdit(List.of(Either.forLeft(textDocumentEdit)));
            CodeAction codeAction = new CodeAction("Replace step with: "+hint);
            codeAction.setIsPreferred(Boolean.TRUE);
            codeAction.setDiagnostics(List.of(diagnostic));
            codeAction.setEdit(edit);
            codeAction.setKind(CodeActionKind.QuickFix);
            codeActions.add(codeAction);
        }
    }


    private void registerMissingIdQuickFix(Diagnostic diagnostic) {
    	Range range = diagnostic.getRange();
		var codeActions = quickFixes.computeIfAbsent(range, x->new ArrayList<>());
        int marginLeft = range.getStart().getCharacter();
        String id = assessor.additionalInfo.idTagGenerator.generate();
		TextEdit textEdit = new TextEdit(
    		startPositionRange(range),
    		id+"\n"+" ".repeat(marginLeft)
		);
		var textDocument = new VersionedTextDocumentIdentifier(assessor.uri(),null);
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
        textDocumentEdit.setEdits(List.of(textEdit));
        textDocumentEdit.setTextDocument(textDocument);
        WorkspaceEdit edit = new WorkspaceEdit(List.of(Either.forLeft(textDocumentEdit)));
        CodeAction codeAction = new CodeAction("Add ID tag to this scenario");
        codeAction.setIsPreferred(Boolean.TRUE);
        codeAction.setDiagnostics(List.of(diagnostic));
        codeAction.setEdit(edit);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeActions.add(codeAction);
    }



    public static Diagnostic diagnostic(
    		DiagnosticSeverity severity,
    		String uri,
    		Range range,
    		String warning,
    		String... extra
    ) {
        var location = new Location(uri, range);
        var diagnostic = new Diagnostic();
        diagnostic.setRange(range);
        diagnostic.setMessage(warning);
        diagnostic.setSeverity(severity);
        diagnostic.setSource("kukumo-language-server");
        diagnostic.setRelatedInformation(
    		Stream.of(extra).map(data->new DiagnosticRelatedInformation(location, data))
    		.collect(Collectors.toList())
    	);
        return diagnostic;
    }



    private Range emptyRange() {
		return new Range(new Position(0, 0), new Position(0, 0));
	}


    private Range wholeLineRange(int lineNumber, int columnFrom) {
        return new Range(
            new Position(lineNumber,columnFrom),
            new Position(lineNumber,assessor.documentMap.document().extractLine(lineNumber).length())
        );
    }


    private Range emptyRange(int lineNumber, int column) {
        return new Range(
            new Position(lineNumber,column),
            new Position(lineNumber,column)
        );
    }


    private Range startPositionRange(Range range) {
        return new Range(range.getStart(), range.getStart());
    }


    private String uri() {
    	return assessor.uri();
    }

}
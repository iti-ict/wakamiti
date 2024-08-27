/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp.internal;

import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import es.iti.wakamiti.core.gherkin.parser.*;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.Hinter;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.WakamitiConfiguration;
import org.eclipse.lsp4j.*;
import org.slf4j.*;


public class GherkinDocumentAssessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("document.synchronization");
    private static final String DOTS = "---------------------------";
    private static final GherkinParser DEFAULT_PARSER = new GherkinParser();
    private static final Wakamiti wakamiti = Wakamiti.instance();

    private final String uri;
    private final GherkinParser parser;
    private final Function<Configuration, Hinter> hinterProvider;


    Configuration globalConfiguration;
    Configuration workspaceConfiguration;
    Configuration documentConfiguration;
    Configuration effectiveConfiguration;
    int maxSuggestions = 20;
    Hinter hinter;
    GherkinDocumentMap documentMap;
    DocumentAdditionalInfo additionalInfo;
    GherkinDocument parsedDocument;
    Exception parsingError;
    DocumentDiagnosticHelper diagnosticHelper;
    CompletionHelper completionHelper;


    public GherkinDocumentAssessor(String document) {
        this("", document);
    }


    public GherkinDocumentAssessor(String uri, String document) {
        this(uri, document, Configuration.factory().empty());
    }


    public GherkinDocumentAssessor(String uri, String document, Configuration workspaceConfiguration) {
        this(
    		uri,
            DEFAULT_PARSER,
            wakamiti::createHinterFor,
            Wakamiti.defaultConfiguration(),
            workspaceConfiguration,
            document
        );
    }


    public GherkinDocumentAssessor(
    	String uri,
        GherkinParser parser,
        Function<Configuration, Hinter> hinterProvider,
        Configuration globalConfiguration,
        Configuration workspaceConfiguration,
        String document
    ) {
    	this.uri = uri;
        this.parser = parser;
        this.hinterProvider = hinterProvider;
        this.globalConfiguration = globalConfiguration;
        this.workspaceConfiguration = workspaceConfiguration;
        this.diagnosticHelper = new DocumentDiagnosticHelper(this);
        this.completionHelper = new CompletionHelper(this, LOGGER);
        resetDocument(document);
    }


    public String uri() {
    	return this.uri;
    }


    public Path path() {
    	return Path.of(URI.create(uri));
    }


    public GherkinDocumentAssessor updateGlobalConfiguration(Configuration configuration) {
        this.globalConfiguration = configuration;
        return this;
    }

    public GherkinDocumentAssessor setMaxSuggestions(int maxSuggestions) {
        this.maxSuggestions = maxSuggestions;
        return this;
    }


	public GherkinDocumentAssessor setWorkspaceConfiguration(Configuration workspaceConfiguration) {
		this.workspaceConfiguration = workspaceConfiguration;
		resetDocument(documentMap.rawContent());
		return this;
	}



    public synchronized GherkinDocumentAssessor resetDocument(String document) {
    	if (!document.isBlank()) {
            try {
                this.documentConfiguration = extractDocumentConfiguration(document);
                this.parsingError = null;
            } catch (Exception e) {
                this.documentConfiguration = Configuration.factory().empty();
                this.parsingError = e;
                this.parsedDocument = null;
            }
        } else {
            this.documentConfiguration = Configuration.factory().empty();
            this.parsingError = null;
            this.parsedDocument = null;
        }

    	this.effectiveConfiguration = globalConfiguration
			.append(workspaceConfiguration)
			.append(documentConfiguration);

    	this.documentMap = new GherkinDocumentMap(document);

    	/*
         * the Gherkin parser do not include the `# language: xx` line as a comment,
         * we have to include it manually in the configuration:
         */
       	effectiveConfiguration = effectiveConfiguration.appendFromPairs(
			WakamitiConfiguration.LANGUAGE,
			documentMap.locale().toString()
		);

        this.hinter = createHinter(effectiveConfiguration);
        this.additionalInfo = new DocumentAdditionalInfo(effectiveConfiguration, parsedDocument);



        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}{}{}",DOTS,documentMap.document().rawText(),DOTS);
        }
        return this;
    }



    private Hinter createHinter(Configuration configuration) {
    	try {
    		return hinterProvider.apply(effectiveConfiguration);
    	} catch (Exception e) {
    		LOGGER.error("Cannot create hinter for configuration {}\n:{}",configuration, e);
    		throw e;
    	}
    }



    public synchronized GherkinDocumentAssessor updateDocument(TextRange range, String delta) {
    	boolean requiresParsing = documentMap.replace(range,delta);
        //if (parsingError != null || requiresParsing) {
        	resetDocument(documentMap.rawContent());
       // }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}{}{}",DOTS,documentMap.document().rawText(),DOTS);
        }
        return this;
    }



    private Configuration extractDocumentConfiguration(String document) {
        // TODO: parsing is an intensive operation, it should work if we just
        //       take the lines starting with # until the first keyword appears
        //       Parsing should be delayed as long as possible
        this.parsedDocument = parser.parse(new StringReader(document));
        Feature feature = parsedDocument.getFeature();
        if (feature == null) {
            return Configuration.factory().empty();
        }
        return extractConfigurationFromComments(feature.getComments());
    }



    private Configuration extractConfigurationFromComments(List<Comment> comments) {
        if (comments == null) {
            return Configuration.factory().empty();
        }
        return Configuration.factory().fromMap(
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
        return completionHelper.collectCompletions(lineNumber, rowPosition);
    }


    public DocumentDiagnostics collectDiagnostics() {
    	return new DocumentDiagnostics(uri, diagnosticHelper.collectDiagnostics());
    }


    public List<CodeAction> retrieveQuickFixes(Diagnostic errorDiagnostic) {
        return diagnosticHelper.retrieveQuickFixes(errorDiagnostic);
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


    public String peekContent() {
    	return this.documentMap.rawContent();
    }


    boolean isDefinition() {
    	return this.additionalInfo.hasRedefinitionDefinitionTag;
    }


    boolean isImplementation() {
    	return this.additionalInfo.hasRedefinitionImplementationTag;
    }


    String definitionTag() {
    	return this.additionalInfo.redefinitionDefinitionTag;
    }


    String implementationTag() {
    	return this.additionalInfo.redefinitionImplementationTag;
    }


    Stream<DocumentSegment> retriveIdTagSegment() {
    	return documentMap.document()
			.extractSegments(additionalInfo.idTagPattern, 1)
			.stream()
			.map(segment -> new DocumentSegment(uri, segment.range().toLspRange(), segment.content()));
    }



	public Optional<TextSegment> obtainIdAt(Position position) {
		for (int lineNumber = position.getLine(); lineNumber >= 0; lineNumber--) {
			var idTags = documentMap.document().extractSegments(lineNumber, additionalInfo.idTagPattern, 1);
			if (!idTags.isEmpty()) {
				return Optional.of(idTags.get(0));
			}
		}
		return Optional.empty();

	}


	public List<TextSegment> obtainIdTags() {
		return documentMap.document().extractSegments(additionalInfo.idTagPattern, 1);

	}


	public Optional<ScenarioDefinition> obtainScenarioById(String id) {
		for (var scenario : parsedDocument.getFeature().getChildren()) {
			List<Tag> tags;
			if (scenario instanceof Scenario) {
				tags = ((Scenario) scenario).getTags();
			} else if (scenario instanceof ScenarioOutline) {
				tags = ((ScenarioOutline) scenario).getTags();
			} else {
				continue;
			}
			if (tags.stream().anyMatch(tag -> tag.getName().equals("@"+id))) {
				return Optional.of(scenario);
			}
		}
		return Optional.empty();
	}



	public List<DocumentSymbol> collectSymbols() {
		if (parsedDocument == null || parsedDocument.getFeature() == null) {
			return List.of();
		}
		return List.of(new SymbolCollector(this).collectSymbols(parsedDocument.getFeature()));
	}




	public int numberOfLines() {
		return documentMap.document().numberOfLines();
	}











}
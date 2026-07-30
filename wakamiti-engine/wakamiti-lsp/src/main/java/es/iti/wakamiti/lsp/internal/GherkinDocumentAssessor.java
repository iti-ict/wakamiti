/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.wakamiti.api.Hinter;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.Feature;
import es.iti.wakamiti.core.gherkin.parser.GherkinDocument;
import es.iti.wakamiti.core.gherkin.parser.GherkinParser;
import es.iti.wakamiti.core.gherkin.parser.Scenario;
import es.iti.wakamiti.core.gherkin.parser.ScenarioDefinition;
import es.iti.wakamiti.core.gherkin.parser.ScenarioOutline;
import es.iti.wakamiti.core.gherkin.parser.Tag;


/**
 * Provides the Gherkin Document Assessor functionality used by Wakamiti.
 */
public class GherkinDocumentAssessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("document.synchronization");
    private static final String DOTS = "---------------------------";
    private static final GherkinParser DEFAULT_PARSER = new GherkinParser();
    private static final Wakamiti WAKAMITI = Wakamiti.instance();
    private static final int DEFAULT_MAX_SUGGESTIONS = 20;

    private final String uri;
    private final GherkinParser parser;
    private final Function<Configuration, Hinter> hinterProvider;

    Configuration globalConfiguration;
    Configuration workspaceConfiguration;
    Configuration documentConfiguration;
    Configuration effectiveConfiguration;
    int maxSuggestions = DEFAULT_MAX_SUGGESTIONS;
    Hinter hinter;
    GherkinDocumentMap documentMap;
    DocumentAdditionalInfo additionalInfo;
    GherkinDocument parsedDocument;
    Exception parsingError;
    DocumentDiagnosticHelper diagnosticHelper;
    CompletionHelper completionHelper;

    /**
     * Creates an assessor for source without an associated URI, using the
     * default parser, Wakamiti configuration and hinter factory.
     *
     * @param document complete Gherkin source
     */
    public GherkinDocumentAssessor(
            String document
    ) {
        this("", document);
    }

    /**
     * Creates an assessor with an empty workspace configuration.
     *
     * @param uri document URI used in diagnostics and workspace edits
     * @param document complete Gherkin source
     */
    public GherkinDocumentAssessor(
            String uri,
            String document
    ) {
        this(uri, document, Configuration.factory().empty());
    }

    /**
     * Creates an assessor with the supplied workspace configuration and the
     * default parser, global configuration and hinter factory.
     *
     * @param uri document URI used in diagnostics and workspace edits
     * @param document complete Gherkin source
     * @param workspaceConfiguration configuration shared by workspace documents
     */
    public GherkinDocumentAssessor(
            String uri,
            String document,
            Configuration workspaceConfiguration
    ) {
        this(
                uri,
                DEFAULT_PARSER,
                WAKAMITI::createHinterFor,
                Wakamiti.defaultConfiguration(),
                workspaceConfiguration,
                document
        );
    }

    /**
     * Creates a fully customized document assessor.
     * <p>
     * Configuration is resolved with document values taking precedence over
     * workspace values, which in turn take precedence over global values. The
     * source is parsed immediately so diagnostics, completions and symbols are
     * available after construction.
     *
     * @param uri document URI used in diagnostics and workspace edits
     * @param parser parser used to build the Gherkin syntax model
     * @param hinterProvider factory for completion and validation hints
     * @param globalConfiguration process-wide base configuration
     * @param workspaceConfiguration workspace-level configuration overrides
     * @param document complete Gherkin source
     */
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

    /**
     * Returns the identifier associated with this document.
     *
     * @return document URI, possibly empty for standalone assessment
     */
    public String uri() {
        return this.uri;
    }

    /**
     * Converts the document URI to a local path.
     *
     * @return path represented by {@link #uri()}
     * @throws IllegalArgumentException if the URI cannot be converted to a path
     */
    public Path path() {
        return Path.of(URI.create(uri));
    }

    /**
     * Replaces the global configuration used during the next document reset.
     * <p>
     * This setter does not reparse the current source; call
     * {@link #resetDocument(String)} when the new configuration must take
     * effect immediately.
     *
     * @param configuration new process-wide base configuration
     * @return this assessor
     */
    public GherkinDocumentAssessor updateGlobalConfiguration(
            Configuration configuration
    ) {
        this.globalConfiguration = configuration;
        return this;
    }

    /**
     * Sets the maximum expanded suggestions returned for steps and quick fixes.
     *
     * @param maxSuggestions maximum number of detailed candidates before a
     *                       compact representation is preferred
     * @return this assessor
     */
    public GherkinDocumentAssessor setMaxSuggestions(
            int maxSuggestions
    ) {
        this.maxSuggestions = maxSuggestions;
        return this;
    }

    /**
     * Replaces the workspace configuration and immediately rebuilds the
     * current document state.
     *
     * @param workspaceConfiguration new workspace-level overrides
     * @return this assessor
     */
    public GherkinDocumentAssessor setWorkspaceConfiguration(
            Configuration workspaceConfiguration
    ) {
        this.workspaceConfiguration = workspaceConfiguration;
        resetDocument(documentMap.rawContent());
        return this;
    }

    /**
     * Replaces and reparses the complete document.
     * <p>
     * The method extracts configuration comments, merges all configuration
     * levels, creates a matching hinter and refreshes derived metadata. Parser
     * failures are retained as diagnostic state rather than propagated, so a
     * caller can still request diagnostics for invalid source.
     *
     * @param document complete replacement source
     * @return this assessor
     */
    public synchronized GherkinDocumentAssessor resetDocument(
            String document
    ) {
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
            LOGGER.debug("{}{}{}", DOTS, documentMap.document().rawText(), DOTS);
        }
        return this;
    }

    private Hinter createHinter(
            Configuration configuration
    ) {
        try {
            return hinterProvider.apply(effectiveConfiguration);
        } catch (Exception e) {
            LOGGER.error("Cannot create hinter for configuration {}\n:{}", configuration, e);
            throw e;
        }
    }

    /**
     * Applies an incremental source edit and refreshes all derived state.
     *
     * @param range half-open, zero-based range to replace
     * @param delta replacement text
     * @return this assessor
     */
    public synchronized GherkinDocumentAssessor updateDocument(
            TextRange range,
            String delta
    ) {
        boolean requiresParsing = documentMap.replace(range, delta);
        //if (parsingError != null || requiresParsing) {
        resetDocument(documentMap.rawContent());
        // }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}{}{}", DOTS, documentMap.document().rawText(), DOTS);
        }
        return this;
    }

    private Configuration extractDocumentConfiguration(
            String document
    ) {
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

    private Configuration extractConfigurationFromComments(
            List<Comment> comments
    ) {
        if (comments == null) {
            return Configuration.factory().empty();
        }
        return Configuration.factory().fromMap(
                comments.stream()
                        .map(Comment::getText)
                        .filter(s -> s.contains(":"))
                        .map(s -> s.replace("#", ""))
                        .map(s -> new Pair<>(
                                s.substring(0, s.indexOf(':')).strip(),
                                s.substring(s.indexOf(':') + 1).strip())
                        )
                        .collect(Pair.toMap())
        );
    }

    /**
     * Collects configuration, keyword or step completions at a position.
     *
     * @param lineNumber zero-based cursor line
     * @param rowPosition zero-based character offset in the line
     * @return matching completion items
     */
    public List<CompletionItem> collectCompletions(
            int lineNumber,
            int rowPosition
    ) {
        return completionHelper.collectCompletions(lineNumber, rowPosition);
    }

    /**
     * Recomputes diagnostics for the current parsed and configured state.
     *
     * @return diagnostics grouped with this assessor's URI
     */
    public DocumentDiagnostics collectDiagnostics() {
        return new DocumentDiagnostics(uri, diagnosticHelper.collectDiagnostics());
    }

    /**
     * Retrieves fixes calculated for a diagnostic during the latest assessment.
     *
     * @param errorDiagnostic diagnostic whose source range identifies the fixes
     * @return applicable quick-fix actions, possibly empty
     */
    public List<CodeAction> retrieveQuickFixes(
            Diagnostic errorDiagnostic
    ) {
        return diagnosticHelper.retrieveQuickFixes(errorDiagnostic);
    }

    /**
     * Returns the current complete document source.
     *
     * @return current Gherkin text
     */
    public String content() {
        return documentMap.document().rawText();
    }

    Configuration globalConfiguration() {
        return this.globalConfiguration;
    }

    Configuration documentConfiguration() {
        return this.documentConfiguration;
    }

    /**
     * Returns the current raw source without triggering parsing or assessment.
     *
     * @return current Gherkin text
     */
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

    /**
     * Finds the nearest scenario ID tag at or before a position.
     * <p>
     * The search walks upward line by line and returns the first captured ID
     * value; the leading {@code @} is not part of the segment.
     *
     * @param position position from which to search upward
     * @return nearest ID segment, or an empty optional
     */
    public Optional<TextSegment> obtainIdAt(
            Position position
    ) {
        for (int lineNumber = position.getLine(); lineNumber >= 0; lineNumber--) {
            var idTags = documentMap.document().extractSegments(lineNumber, additionalInfo.idTagPattern, 1);
            if (!idTags.isEmpty()) {
                return Optional.of(idTags.get(0));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all scenario ID tags matching the configured pattern.
     *
     * @return captured ID segments without the leading {@code @}
     */
    public List<TextSegment> obtainIdTags() {
        return documentMap.document().extractSegments(additionalInfo.idTagPattern, 1);
    }

    /**
     * Finds a scenario or scenario outline carrying a given ID tag.
     *
     * @param id identifier without the leading {@code @}
     * @return matching scenario definition, or an empty optional
     */
    public Optional<ScenarioDefinition> obtainScenarioById(
            String id
    ) {
        for (var scenario : parsedDocument.getFeature().getChildren()) {
            List<Tag> tags;
            if (scenario instanceof Scenario) {
                tags = ((Scenario) scenario).getTags();
            } else if (scenario instanceof ScenarioOutline) {
                tags = ((ScenarioOutline) scenario).getTags();
            } else {
                continue;
            }
            if (tags.stream().anyMatch(tag -> tag.getName().equals("@" + id))) {
                return Optional.of(scenario);
            }
        }
        return Optional.empty();
    }

    /**
     * Builds the hierarchical symbols used by an editor document outline.
     *
     * @return a feature-rooted symbol tree, or an empty list when no feature
     *         could be parsed
     */
    public List<DocumentSymbol> collectSymbols() {
        if (parsedDocument == null || parsedDocument.getFeature() == null) {
            return List.of();
        }
        return List.of(new SymbolCollector(this).collectSymbols(parsedDocument.getFeature()));
    }

    /**
     * Returns the number of logical lines in the current source.
     *
     * @return document line count
     */
    public int numberOfLines() {
        return documentMap.document().numberOfLines();
    }

}

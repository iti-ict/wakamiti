package iti.kukumo.lsp.internal;

import java.util.regex.Pattern;

import com.github.curiousoddman.rgxgen.RgxGen;

import iti.commons.configurer.Configuration;
import iti.commons.gherkin.*;
import iti.kukumo.api.KukumoConfiguration;

public class DocumentAdditionalInfo {

	public final boolean redefinitionEnabled;
	public final String redefinitionDefinitionTag;
	public final String redefinitionImplementationTag;
	public final boolean hasRedefinitionDefinitionTag;
	public final boolean hasRedefinitionImplementationTag;
	public final Pattern idTagPattern;
	public final RgxGen idTagGenerator;

	public DocumentAdditionalInfo(
		Configuration effectiveConfiguration,
		GherkinDocument parsedDocument
	) {
		this.redefinitionEnabled = effectiveConfiguration
			.get(KukumoConfiguration.REDEFINITION_ENABLED, Boolean.class)
			.orElse(Boolean.TRUE);

		this.idTagPattern = effectiveConfiguration
			.get(KukumoConfiguration.ID_TAG_PATTERN, String.class)
			.map(pattern -> "@"+pattern)
			.map(Pattern::compile)
			.orElseThrow();

		this.idTagGenerator = new RgxGen(idTagPattern.pattern().replace("*", "{5}"));


		if (this.redefinitionEnabled) {

			this.redefinitionDefinitionTag = effectiveConfiguration
				.get(KukumoConfiguration.REDEFINITION_DEFINITION_TAG, String.class)
				.orElse("");

			this.redefinitionImplementationTag = effectiveConfiguration
				.get(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG, String.class)
				.orElse("");

			if (parsedDocument != null && parsedDocument.getFeature() != null) {
				this.hasRedefinitionDefinitionTag =	parsedDocument
					.getFeature()
					.getTags()
					.stream()
					.map(Tag::getName)
					.anyMatch(tag -> tag.equals("@"+redefinitionDefinitionTag));

				this.hasRedefinitionImplementationTag =	parsedDocument
					.getFeature()
					.getTags()
					.stream()
					.map(Tag::getName)
					.anyMatch(tag -> tag.equals("@"+redefinitionImplementationTag));
			} else {
				this.hasRedefinitionDefinitionTag = false;
				this.hasRedefinitionImplementationTag = false;
			}

		} else {
			this.redefinitionDefinitionTag = null;
			this.redefinitionImplementationTag = null;
			this.hasRedefinitionDefinitionTag = false;
			this.hasRedefinitionImplementationTag = false;
		}


	}



}

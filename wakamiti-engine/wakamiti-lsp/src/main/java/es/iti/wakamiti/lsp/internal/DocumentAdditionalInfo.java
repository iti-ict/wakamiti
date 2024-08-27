/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp.internal;

import java.util.regex.Pattern;

import com.github.curiousoddman.rgxgen.RgxGen;

import es.iti.wakamiti.core.gherkin.parser.GherkinDocument;
import es.iti.wakamiti.core.gherkin.parser.Tag;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.WakamitiConfiguration;

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
			.get(WakamitiConfiguration.REDEFINITION_ENABLED, Boolean.class)
			.orElse(Boolean.TRUE);

		this.idTagPattern = effectiveConfiguration
			.get(WakamitiConfiguration.ID_TAG_PATTERN, String.class)
			.map(pattern -> "@("+pattern+")")
			.map(Pattern::compile)
			.orElseThrow();

		this.idTagGenerator = new RgxGen(idTagPattern.pattern().replace("*", "{5}"));


		if (this.redefinitionEnabled) {

			this.redefinitionDefinitionTag = effectiveConfiguration
				.get(WakamitiConfiguration.REDEFINITION_DEFINITION_TAG, String.class)
				.orElse("");

			this.redefinitionImplementationTag = effectiveConfiguration
				.get(WakamitiConfiguration.REDEFINITION_IMPLEMENTATION_TAG, String.class)
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
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp.internal;

import static java.util.stream.Collectors.joining;

import es.iti.wakamiti.core.gherkin.parser.ScenarioDefinition;
import es.iti.wakamiti.core.gherkin.parser.ScenarioOutline;
import es.iti.wakamiti.core.gherkin.parser.Step;
import es.iti.wakamiti.core.gherkin.parser.*;

public class Snippets {


	public static String implementationScenarioSnippet(
		String id,
		GherkinDocumentAssessor definition,
		GherkinDocumentAssessor implementation
	) {

		String template =
			"\n\n" +
		    "{margin}@{id}\n" +
		    "{margin}# redefinition.stepMap: {map}\n" +
		    "{margin}{keyword}: {name}\n" +
		    "{margin}# Replace the following steps with implementation:\n" +
		    "{margin}#\n"
		    ;

		var implementationDialect = (implementation == null ?
			definition.documentMap.dialect() :
			implementation.documentMap.dialect()
		);
		ScenarioDefinition scenario = definition.obtainScenarioById(id).orElseThrow();
		boolean isOutline = (scenario instanceof ScenarioOutline);

		String margin = "    ";
		String map = "1" + "-1".repeat(scenario.getSteps().size()-1);
		var acceptedKeywords = (isOutline ?
			implementationDialect.getScenarioOutlineKeywords() :
			implementationDialect.getScenarioKeywords()
		);
		String keyword = acceptedKeywords.get(0);
		String name = scenario.getName();
		String steps = scenario.getSteps().stream()
			.map(Step::getText)
			.collect(joining("\n"+margin+"# ", margin+"# ", ""));

		return template
			.replace("{margin}", margin)
			.replace("{map}", map)
			.replace("{id}", id)
			.replace("{keyword}", keyword)
			.replace("{name}", name)
			.concat(steps);

	}



	public static final String implementationFeatureSnippet(GherkinDocumentAssessor definition) {

		var dialect = definition.documentMap.dialect();

		String template =
			"# language: {locale}\n"+
		    "\n"+
		    "@{implementationTag}\n"+
		    "{keyword}: {name}\n"+
			"Implementation corresponding to definition {definitionFile}\n"+
		    "\n"+
			"";

		return template
			.replace("{locale}", dialect.getLanguage() )
			.replace("{implementationTag}", definition.implementationTag())
			.replace("{keyword}", dialect.getFeatureKeywords().get(0))
			.replace("{name}", definition.parsedDocument.getFeature().getName())
			.replace("{definitionFile}", definition.path().getFileName().toString())
		;

	}

}
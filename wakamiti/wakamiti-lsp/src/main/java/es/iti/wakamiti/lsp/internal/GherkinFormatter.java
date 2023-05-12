/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp.internal;

import static es.iti.wakamiti.lsp.internal.GherkinFormatter.Type.*;

import java.util.stream.*;

import es.iti.commons.gherkin.GherkinDialect;

public class GherkinFormatter {

	private static final String TRIPLE_QUOTE = "\"\"\"";
	private static final String TRIPLE_BACKQUOTE = "```";


	enum Type {
		EMPTY(false,0,0),
		COMMENT(false,0,1),
		TAG(false,0,1),
		FEATURE(true,0,0),
		FEATURE_CONTENT(true,1,0),
		DESCRIPTION(false,0,-1),
		STEP(true,2,0),
		TABLE_HEADER(false,2,0),
		TABLE_ROW(false,2,0),
		DOCUMENT_START(false,2,0),
		DOCUMENT_END(false,2,0),
		DOCUMENT_CONTENT(false,2,0);

		Type(boolean isKeyword, int level, int lookupDirection) {
			this.isKeyword = isKeyword;
			this.level = level;
			this.lookupDirection = lookupDirection;
		}

		public final int level;
		public final boolean isKeyword;
		public final int lookupDirection;
	}


	public static String format(GherkinDocumentMap documentMap, int tabSize) {
		var document = documentMap.document().copy();
		Type[] lineTypes = classifyLines(documentMap);
		int[] marginLevels = analyzeMarginLevels(lineTypes);
		String margin = " ".repeat(tabSize);
		formatMargins(document, lineTypes, marginLevels, margin);
		formatTables(document, lineTypes, margin);
		return document.rawText();

	}






	private static void formatMargins(
		TextDocument document,
		Type[] lineTypes,
		int[] marginLevels,
		String margin
	) {
		String marginLevel;
		String line;
		String formattedLine;
		for (int lineNumber = 0; lineNumber < document.numberOfLines(); lineNumber++) {
			marginLevel = margin.repeat(marginLevels[lineNumber]);
			line = document.extractLine(lineNumber);
			if (lineTypes[lineNumber] == DOCUMENT_CONTENT && line.startsWith(marginLevel)) {
				formattedLine = line;
			} else {
				formattedLine = marginLevel + line.strip().replaceAll(" +", " ");
			}
			document.replaceLine(lineNumber, formattedLine);
		}
	}


	private static void formatTables(TextDocument document, Type[] lineTypes, String margin) {
		int lineNumber = 0;
		while (lineNumber < document.numberOfLines()) {
			if (lineTypes[lineNumber] == TABLE_HEADER) {
				lineNumber += formatTable(document, lineTypes, lineNumber, margin)	;
			} else {
				lineNumber ++;
			}
		}
	}


	private static int formatTable(
		TextDocument document,
		Type[] lineTypes,
		int headerLineNumber,
		String margin
	) {
		String header = document.extractLine(headerLineNumber);
		int columns = (int) header.chars().filter(c -> c == '|').count() - 1;
		int rows = 1;
		for (int lineNumber = headerLineNumber+1; lineNumber < document.numberOfLines(); lineNumber++) {
			if (lineTypes[lineNumber] == TABLE_ROW) {
				rows++;
			} else {
				break;
			}
		}
		String[][] table = new String[rows][columns];
		for (int r = 0; r < rows; r ++) {
			String[] row = document.extractLine(headerLineNumber + r).split("\\|");
			for (int c = 0; c < columns; c++ ) {
				table[r][c] = row[c+1].strip();
			}
		}
		int[] maxColumnLength = new int[columns];
		for (int c = 0; c < columns; c++ ) {
			int max = 0;
			for (int r = 0; r < rows; r++ ) {
				max = Math.max(max, table[r][c].length());
			}
			maxColumnLength[c] = max;
		}
		for (int r = 0; r < rows; r++ ) {
			for (int c = 0; c < columns; c++ ) {
				table[r][c] = padding(table[r][c], maxColumnLength[c]);
			}
			String formattedLine = Stream.of(table[r]).collect(Collectors.joining(
				" | ",
				margin.repeat(TABLE_ROW.level)+"| ",
				" |"
			));
			document.replaceLine(headerLineNumber + r, formattedLine);
		}
		return rows;
	}




	private static int[] analyzeMarginLevels(Type[] lineTypes) {

		int lastLineNumber = lineTypes.length - 1;
		int[] level = new int[lineTypes.length];
		Type type = null;

		for (int lineNumber = lastLineNumber; lineNumber >= 0; lineNumber--) {

			type = lineTypes[lineNumber];
			if (type.isKeyword) {
				level[lineNumber] = type.level;
			} else if (type.lookupDirection == 1) {
				var nextKeywordType = nextKeywordType(lineTypes, lineNumber);
				if (nextKeywordType != null) {
					level[lineNumber] = nextKeywordType.level;
				}
			} else if (type.lookupDirection == -1) {
				var previousKeywordType = previousKeywordType(lineTypes, lineNumber);
				if (previousKeywordType != null) {
					level[lineNumber] = previousKeywordType.level;
				}
			} else {
				level[lineNumber] = type.level;
			}
		}
		return level;
	}


	private static Type nextKeywordType(Type[] lineTypes, int lineNumber) {
		Type type;
		for (int i = lineNumber; i < lineTypes.length; i++) {
			type = lineTypes[i];
			if (type == FEATURE || type == FEATURE_CONTENT || type == STEP) {
				return type;
			}
		}
		return null;
	}


	private static Type previousKeywordType(Type[] lineTypes, int lineNumber) {
		Type type;
		for (int i = lineNumber; i >= 0; i--) {
			type = lineTypes[i];
			if (type == FEATURE || type == FEATURE_CONTENT || type == STEP) {
				return type;
			}
		}
		return null;
	}


	private static Type[] classifyLines(GherkinDocumentMap documentMap) {

		var document = documentMap.document();
		Type[] lineType = new Type[document.numberOfLines()];
		Type type = null;
		Type previousType = null;

		for (int lineNumber = 0; lineNumber < document.numberOfLines(); lineNumber++) {
			previousType = type;
			String line = document.extractLine(lineNumber).stripLeading();
			if (line.isBlank()) {
				type = Type.EMPTY;
			} else if (line.startsWith("#")) {
				type = Type.COMMENT;
			} else if (line.startsWith("@")) {
				type = Type.TAG;
			} else if (previousType == Type.STEP && line.startsWith("|")) {
				type = Type.TABLE_HEADER;
			} else if (previousType == Type.TABLE_HEADER && line.startsWith("|")) {
				type = Type.TABLE_ROW;
			} else if (previousType == Type.TABLE_ROW && line.startsWith("|")) {
				type = Type.TABLE_ROW;
			} else if (previousType == Type.STEP && (line.startsWith(TRIPLE_QUOTE) || line.startsWith(TRIPLE_BACKQUOTE))) {
				type = Type.DOCUMENT_START;
			} else if (previousType == Type.DOCUMENT_CONTENT && (line.startsWith(TRIPLE_QUOTE) || line.startsWith(TRIPLE_BACKQUOTE))) {
				type = Type.DOCUMENT_END;
			} else if (previousType == Type.DOCUMENT_CONTENT && !(line.startsWith(TRIPLE_QUOTE) || line.startsWith(TRIPLE_BACKQUOTE))) {
				type = Type.DOCUMENT_CONTENT;
			} else if (previousType == Type.DOCUMENT_START) {
				type = Type.DOCUMENT_CONTENT;
			} else if (documentMap.hasKeyword(lineNumber, line, GherkinDialect::getFeatureKeywords)) {
				type = Type.FEATURE;
			} else if (documentMap.hasKeyword(lineNumber, line, GherkinDialect::getFeatureContentKeywords)) {
				type = Type.FEATURE_CONTENT;
			} else if (documentMap.hasKeyword(lineNumber, line, GherkinDialect::getStepKeywords)) {
				type = Type.STEP;
			} else {
				type = Type.DESCRIPTION;
			}
			lineType[lineNumber] = type;
		}
		return lineType;

	}


	private static String padding(String string, int length) {
		return string + " ".repeat(length - string.length());
	}

/*

	public static void format(GherkinDocumentMap documentMap) {

		TextDocument document = documentMap.document();
		int level = 0;
		int previousLevel = 0;
		String padding;
		boolean inDocument = false;

		Function<GherkinDialect,List<String>> innerKeywords = GherkinDialect::getFeatureKeywords;
		Function<GherkinDialect,List<String>> outterKeywords = x -> List.of();


		for (int lineNumber = 0; lineNumber < document.numberOfLines(); lineNumber++) {

			padding = "  ".repeat(level);
			String line = document.extractLine(lineNumber);
			String replacedLine = line;

			if (line.isBlank()) {
				document.replaceLine(lineNumber, "");
				continue;
			}

			if (inDocument) {
				if (!line.startsWith(padding)) {
					replacedLine = padding + line;
				}
			} else {
				replacedLine  = padding + line.strip().replaceAll(" +", " ");
			}

			document.replaceLine(lineNumber, replacedLine);


			previousLevel = level;

			if (!inDocument) {
				if (documentMap.hasKeyword(lineNumber, replacedLine.strip(), innerKeywords)) {
					level ++;
				} else if (documentMap.hasKeyword(lineNumber, replacedLine.strip(), outterKeywords)) {
					level --;
				}
			}

			if (level != previousLevel) {
				switch (level) {
				case 0:
					innerKeywords = GherkinDialect::getFeatureKeywords;
					outterKeywords = x -> List.of();
					break;
				case 1:
					innerKeywords = GherkinDialect::getFeatureContentKeywords;
					outterKeywords = GherkinDialect::getFeatureKeywords;
					break;
				case 2:
					innerKeywords = GherkinDialect::getStepKeywords;
					outterKeywords = GherkinDialect::getFeatureContentKeywords;
					break;
				default:
					innerKeywords = x -> List.of();
					outterKeywords = x -> List.of();
				}
			}

			if (line.startsWith("\"\"\"") || line.startsWith("```")) {
				inDocument = !inDocument;
			}

		}

	}
*/
}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.lsp.internal;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.*;

import iti.commons.gherkin.*;
import iti.commons.gherkin.Location;


public class SymbolCollector {

	private final GherkinDocumentAssessor assessor;


	public SymbolCollector(GherkinDocumentAssessor assessor) {
		this.assessor = assessor;
	}


	public DocumentSymbol collectSymbols(Feature feature) {
		var symbol = new DocumentSymbol();
		symbol.setName(feature.getName());
		symbol.setDetail(feature.getKeyword());
		symbol.setKind(SymbolKind.Package);
		symbol.setRange(assessor.documentMap.document().wholeRange().toLspRange());
		symbol.setSelectionRange(nameRange(feature.getLocation(), feature.getKeyword()));

		var children = new ArrayList<DocumentSymbol>();
		for (int i=0; i<feature.getChildren().size(); i++) {
			children.add(collectSymbols(
				current(feature.getChildren(), i),
				next(feature.getChildren(), i),
				symbol.getRange()
			));
		}
		symbol.setChildren(children);
		return symbol;
	}




	public DocumentSymbol collectSymbols(
		ScenarioDefinition current,
		Optional<ScenarioDefinition> next,
		Range upperRange
	) {
		var symbol = new DocumentSymbol();
		symbol.setName(current.getName());
		symbol.setDetail(current.getKeyword());

		if (current instanceof Background) {
			symbol.setName(current.getKeyword());
			symbol.setKind(SymbolKind.Interface);
		} else if (current instanceof ScenarioOutline) {
			symbol.setKind(SymbolKind.Interface);
		} else {
	   	    symbol.setKind(SymbolKind.Class);
		}


		symbol.setRange(rangeUntilNext(current,next,upperRange));
		symbol.setSelectionRange(nameRange(current.getLocation(), current.getKeyword()));

		var children = new ArrayList<DocumentSymbol>();
		for (int i=0; i<current.getSteps().size(); i++) {
			children.add(collectSymbols(
				current(current.getSteps(), i),
				next(current.getSteps(), i),
				symbol.getRange()
			));
		}
		symbol.setChildren(children);


		return symbol;
	}



	public DocumentSymbol collectSymbols(
		Step current,
		Optional<Step> next,
		Range upperRange
	) {
		var symbol = new DocumentSymbol();
		symbol.setName(current.getKeyword()+current.getText());
		symbol.setKind(SymbolKind.Method);
		symbol.setSelectionRange(nameRange(current.getLocation(), current.getKeyword()));
		if (current.getArgument() == null) {
			symbol.setRange(lineOf(current.getLocation().getLine()-1));
			symbol.setChildren(List.of());
		} else {
			symbol.setRange(rangeUntilNext(current,next,upperRange));
			symbol.setChildren(List.of(collectSymbol(current.getArgument(), symbol.getRange())));
		}

		return symbol;
	}



	private DocumentSymbol collectSymbol(Node argument, Range upperRange) {
		var symbol = new DocumentSymbol();
		if (argument instanceof DocString) {
			var docString = (DocString) argument;
			symbol.setName(shorten(docString.getContent().stripLeading(), 20));
			if (docString.getContentType() != null) {
				symbol.setName(docString.getContentType()+" : "+symbol.getName());
			}
			symbol.setKind(SymbolKind.String);
		} else if (argument instanceof DataTable) {
			var dataTable = (DataTable) argument;
			symbol.setName(dataTable.getRows().get(0).getCells().stream().map(cell->cell.getValue()).collect(Collectors.joining(" | ",  "| ", " |")));
			symbol.setKind(SymbolKind.Struct);
		}
		symbol.setRange(rangeUntilUpper(argument.getLocation(),upperRange));
		symbol.setSelectionRange(symbol.getRange());
		return symbol;
	}






	private Range lineOf(int line) {
		return new Range(
			new Position(line,0),
			new Position(line,assessor.documentMap.document().extractLine(line).length())
		);
	}



	private <T extends CommentedNode> Range rangeUntilNext(
		T start, Optional<T> end, Range upperRange
	) {
		return new Range(
			new Position(start.getLocation().getLine()-1,0),
			end.map( it -> new Position(it.getLocation().getLine() - 2,0)).orElse(upperRange.getEnd())
		);
	}


	private Range rangeUntilUpper(Location location, Range upperRange) {
		return new Range(
			new Position(location.getLine()-1,0),
			upperRange.getEnd()
		);
	}



	private Range nameRange(iti.commons.gherkin.Location location, String keyword) {
		return assessor.documentMap.lineRangeWithoutKeyword(
			location.getLine()-1,
			keyword
		).toLspRange();
	}



	private <T> T current(List<T> list, int index) {
		return list.get(index);
	}


	private <T> Optional<T> next(List<T> list, int index) {
		return list.size() > index+1 ? Optional.of(list.get(index+1)) : Optional.empty();
	}



	private String shorten(String content, int size) {
		if (content.length() < size) {
			return content;
		} else {
			return content.substring(0,size-3)+"...";
		}
	}
}
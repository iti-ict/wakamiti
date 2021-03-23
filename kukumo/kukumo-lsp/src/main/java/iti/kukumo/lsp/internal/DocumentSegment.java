package iti.kukumo.lsp.internal;

import org.eclipse.lsp4j.Range;

public class DocumentSegment {

	private final String uri;
	private final Range range;
	private final String content;


	public DocumentSegment(String uri, Range range, String content) {
		this.uri = uri;
		this.range = range;
		this.content = content;
	}

	public String content() {
		return content;
	}

	public Range range() {
		return range;
	}

	public String uri() {
		return uri;
	}

}

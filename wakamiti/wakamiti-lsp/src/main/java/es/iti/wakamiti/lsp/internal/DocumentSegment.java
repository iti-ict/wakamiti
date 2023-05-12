/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp.internal;

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
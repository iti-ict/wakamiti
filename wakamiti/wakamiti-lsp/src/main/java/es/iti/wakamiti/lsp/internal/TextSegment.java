/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp.internal;

public class TextSegment {

	public static TextSegment of(TextRange range, String content) {
		return new TextSegment(range, content);
	}

	private TextRange range;
	private String content;


	private TextSegment(TextRange range, String content) {
		this.range = range;
		this.content = content;
	}

	public TextRange range() {
		return range;
	}

	public String content() {
		return content;
	}


	@Override
	public String toString() {
		return range + "<" + content + ">";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((range == null) ? 0 : range.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextSegment other = (TextSegment) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (range == null) {
			if (other.range != null)
				return false;
		} else if (!range.equals(other.range))
			return false;
		return true;
	}





}
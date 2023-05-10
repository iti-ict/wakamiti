/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.server.infra.app.exceptions;

import java.util.UUID;

public class ErrorResponse {

	private String id;
	private String error;

	public ErrorResponse(Exception e) {
		this.id = UUID.randomUUID().toString();
		this.error = e.getMessage();
	}

	public String getError() {
		return error;
	}

	public String getId() {
		return id;
	}

}
package iti.kukumo.server.infra.app.exceptions;

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

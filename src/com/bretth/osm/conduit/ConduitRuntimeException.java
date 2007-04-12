package com.bretth.osm.conduit;

public class ConduitRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	

	public ConduitRuntimeException() {
	}

	public ConduitRuntimeException(String message) {
		super(message);
	}

	public ConduitRuntimeException(Throwable cause) {
		super(cause);
	}

	public ConduitRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}

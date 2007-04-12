package com.bretth.osm.conduit;

public abstract class ConduitException extends Exception {
	
	private static final long serialVersionUID = 1L; 
	

	public ConduitException() {
	}

	public ConduitException(String message) {
		super(message);
	}

	public ConduitException(Throwable cause) {
		super(cause);
	}

	public ConduitException(String message, Throwable cause) {
		super(message, cause);
	}

}

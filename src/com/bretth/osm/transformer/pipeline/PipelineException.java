package com.bretth.osm.transformer.pipeline;

public abstract class PipelineException extends Exception {
	
	private static final long serialVersionUID = 1L; 
	

	public PipelineException() {
	}

	public PipelineException(String message) {
		super(message);
	}

	public PipelineException(Throwable cause) {
		super(cause);
	}

	public PipelineException(String message, Throwable cause) {
		super(message, cause);
	}

}

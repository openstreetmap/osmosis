package com.bretth.osm.transformer.pipeline;

public class PipelineRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	

	public PipelineRuntimeException() {
	}

	public PipelineRuntimeException(String message) {
		super(message);
	}

	public PipelineRuntimeException(Throwable cause) {
		super(cause);
	}

	public PipelineRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}

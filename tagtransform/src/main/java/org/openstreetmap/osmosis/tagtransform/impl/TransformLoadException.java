// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

public class TransformLoadException extends RuntimeException {
	private static final long serialVersionUID = 1L;


	public TransformLoadException(String message, Exception cause) {
		super(message, cause);
	}

}

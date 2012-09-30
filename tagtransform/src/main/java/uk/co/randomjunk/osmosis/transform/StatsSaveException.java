// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform;

import java.io.IOException;

public class StatsSaveException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public StatsSaveException(String message, IOException cause) {
		super(message, cause);
	}

}

// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import java.io.IOException;

public class StatsSaveException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public StatsSaveException(String message, IOException cause) {
		super(message, cause);
	}

}

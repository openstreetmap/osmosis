// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

/**
 * Defines possible actions to take when a task removes
 * a bound entity from the output stream.
 */
public enum BoundRemovedAction {
	/**
	 * Continue processing quietly.
	 */
	Ignore ("ignore"),
	
	/**
	 * Continue processing but emit a warning to the log.
	 */
	Warn ("warn"),
	
	/**
	 * Stop processing and emit an error message to the log.
	 */
	Fail ("fail");
	
	private final String keyword;
	
	private BoundRemovedAction(String keyword) {
		this.keyword = keyword;
	}
	
	/**
	 * Returns an action for a given string, if possible.
	 * 
	 * @param s the string to parse
	 * @return an action corresponding to a string, if it exists.
	 */
	public static BoundRemovedAction parse(String s) {
		if (s == null) {
			throw new OsmosisRuntimeException(
				"Unrecognized bound removed action value: must be one of ignore, warn, fail.");
		}
		for (BoundRemovedAction a : values()) {
			if (a.keyword.equals(s.toLowerCase())) {
				return a;
			}
		}
		throw new OsmosisRuntimeException(
			"Unrecognized bound removed action value: must be one of ignore, warn, fail.");
	}
}

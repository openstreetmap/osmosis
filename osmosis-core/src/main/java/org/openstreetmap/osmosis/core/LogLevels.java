// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core;

import java.util.logging.Level;


/**
 * Utility class for mapping integer numbers to log levels.
 * 
 * @author Brett Henderson
 */
public final class LogLevels {

	/**
	 * This class cannot be instantiated.
	 */
	private LogLevels() {
		// Not callable.
	}

	/**
	 * This is the default offset into the LOG_LEVELS array.
	 */
	public static final int DEFAULT_LEVEL_INDEX = 3;

	/**
	 * Defines the log levels supported from the command line.
	 */
	public static final Level [] LOG_LEVELS = {
		Level.OFF,
		Level.SEVERE,
		Level.WARNING,
		Level.INFO,
		Level.FINE,
		Level.FINER,
		Level.FINEST
	};


	/**
	 * Map a log level index to a log level. This will clip log levels to
	 * allowable levels if the value is outside the maximum bounds.
	 * 
	 * @param logLevelIndex
	 *            The requested log level index.
	 * @return The log level associated with the index.
	 */
	public static Level getLogLevel(int logLevelIndex) {
		// Ensure that the log level is within allowable bounds.
		if (logLevelIndex < 0) {
			logLevelIndex = 0;
		}
		if (logLevelIndex >= LOG_LEVELS.length) {
			logLevelIndex = LOG_LEVELS.length - 1;
		}

		return LOG_LEVELS[logLevelIndex];
	}
}

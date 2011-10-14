// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6.impl;

import java.io.File;
import java.util.Properties;

import org.openstreetmap.osmosis.core.util.PropertiesPersister;


/**
 * Loads and exposes the configuration properties for replication file merging.
 */
public class ReplicationFileMergerConfiguration {
	private static final String KEY_INTERVAL_LENGTH = "intervalLength";
	
	
	private Properties properties;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param configFile
	 *            The configuration file to read from.
	 */
	public ReplicationFileMergerConfiguration(File configFile) {
		properties = new PropertiesPersister(configFile).load();
	}
	
	
	/**
	 * Returns the duration of each changeset interval.
	 * 
	 * @return The interval length in milliseconds.
	 */
	public int getIntervalLength() {
		return Integer.parseInt(properties.getProperty(KEY_INTERVAL_LENGTH)) * 1000;
	}
}

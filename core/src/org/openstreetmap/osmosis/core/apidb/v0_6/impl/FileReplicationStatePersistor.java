// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.io.File;
import java.util.Properties;

import org.openstreetmap.osmosis.core.util.PropertiesPersister;


/**
 * A file-based persister for replication state.
 */
public class FileReplicationStatePersistor implements ReplicationStatePersister {
	
	private PropertiesPersister persister;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param stateFile
	 *            The location of the file containing the persisted data.
	 */
	public FileReplicationStatePersistor(File stateFile) {
		persister = new PropertiesPersister(stateFile);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ReplicationState loadState() {
		return new ReplicationState(persister.load());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void saveState(ReplicationState state) {
		Properties properties;
		
		properties = new Properties();
		state.store(properties);
		
		persister.store(properties);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean stateExists() {
		return persister.exists();
	}
}

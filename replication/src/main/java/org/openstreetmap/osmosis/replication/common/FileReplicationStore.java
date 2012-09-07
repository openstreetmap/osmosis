// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import java.io.File;

import org.openstreetmap.osmosis.core.util.PropertiesPersister;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeReader;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeWriter;


/**
 * A {@link ReplicationStore} implementation storing all data to the filesystem.
 * 
 * @author Brett Henderson
 */
public class FileReplicationStore implements ReplicationStore {
	private static final String STATE_FILE = "state.txt";

	private PropertiesPersister currentStatePersister;
	private ReplicationFileSequenceFormatter sequenceFormatter;
	private boolean saveCurrentState;


	/**
	 * Creates a new instance.
	 * 
	 * @param storeDirectory
	 *            The directory used to hold the contents of the store.
	 * @param saveCurrentState
	 *            If true, the current state will be updated by the
	 *            {@link #saveState(ReplicationState)} operation as well as the
	 *            sequenced state.
	 */
	public FileReplicationStore(File storeDirectory, boolean saveCurrentState) {
		currentStatePersister = new PropertiesPersister(new File(storeDirectory, STATE_FILE));
		sequenceFormatter = new ReplicationFileSequenceFormatter(storeDirectory);
		this.saveCurrentState = saveCurrentState;
	}


	@Override
	public ReplicationState getCurrentState() {
		ReplicationState state = new ReplicationState();
		state.load(currentStatePersister.loadMap());
		return state;
	}


	@Override
	public ReplicationState getState(long sequence) {
		File stateFile = sequenceFormatter.getFormattedName(sequence, ".state.txt");
		return new ReplicationState(new PropertiesPersister(stateFile).loadMap());
	}


	@Override
	public XmlChangeReader getData(long sequence) {
		File changeFile = sequenceFormatter.getFormattedName(sequence, ".osc.gz");
		return new XmlChangeReader(changeFile, false, CompressionMethod.GZip);
	}


	@Override
	public void saveState(ReplicationState state) {
		File stateFile = sequenceFormatter.getFormattedName(state.getSequenceNumber(), ".state.txt");
		new PropertiesPersister(stateFile).store(state.store());

		if (saveCurrentState) {
			currentStatePersister.store(state.store());
		}
	}


	@Override
	public XmlChangeWriter saveData(long sequence) {
		File changeFile = sequenceFormatter.getFormattedName(sequence, ".osc.gz");
		return new XmlChangeWriter(changeFile, CompressionMethod.GZip);
	}
}

// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.io.File;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriter;


/**
 * A file-based destination for replication data.  This writes files beginning at 1.osc.gz and increasing incrementally.
 */
public class FileReplicationDestination implements ReplicationDestination {
	
	private static final String STATE_FILE = "state.txt";
	private static final String TMP_STATE_FILE = "tmpstate.txt";
	private static final String SEQUENCE_STATE_FILE_SUFFIX = ".state.txt";
	private static final String CHANGE_FILE_SUFFIX = ".osc.gz";
	private static final CompressionMethod CHANGE_FILE_COMPRESSION = CompressionMethod.GZip;
	private static final String TMP_CHANGE_FILE = "tmpchangeset.osc.gz";
	

	private File workingDirectory;
	private File stateFile;
	private File tmpStateFile;
	private FileReplicationStatePersistor statePersistor;
	private ReplicationState state;
	private XmlChangeWriter writer;


	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory that all files will be produced in.
	 */
	public FileReplicationDestination(File workingDirectory) {
		this.workingDirectory = workingDirectory;
		
		stateFile = new File(workingDirectory, STATE_FILE);
		tmpStateFile = new File(workingDirectory, TMP_STATE_FILE);
		
		statePersistor = new FileReplicationStatePersistor(stateFile, tmpStateFile);
	}


	private void renameFile(File existingName, File newName) {
		// Make sure we have a new file.
		if (!existingName.exists()) {
			throw new OsmosisRuntimeException("Can't rename non-existent file " + existingName + ".");
		}
		
		// Delete the existing file if it exists.
		if (newName.exists()) {
			if (!newName.delete()) {
				throw new OsmosisRuntimeException("Unable to delete file " + newName + ".");
			}
		}
		
		// Rename the new file to the existing file.
		if (!existingName.renameTo(newName)) {
			throw new OsmosisRuntimeException(
					"Unable to rename file " + existingName + " to " + newName + ".");
		}
	}
	
	
	private void initializeWriter() {
		if (writer == null) {
			writer = new XmlChangeWriter(
					new File(workingDirectory, TMP_CHANGE_FILE),
					CHANGE_FILE_COMPRESSION);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ChangeContainer change) {
		initializeWriter();
		
		writer.process(change);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		// We won't write an output file if we are initializing.
		if (statePersistor.stateExists()) {
			initializeWriter();
			
			// Close the output file and rename it as a sequenced change file.
			// We must release the file completely before we attempt to rename it.
			writer.complete();
			writer.release();
			writer = null;
			renameFile(
					new File(workingDirectory, TMP_CHANGE_FILE),
					new File(workingDirectory, Long.toString(state.getSequenceNumber()) + CHANGE_FILE_SUFFIX));
		}
		
		// The final step is to save the current state. This must be done last so that if a crash
		// occurs during processing it starts from the same point as last time.
		if (state != null) {
			statePersistor.saveState(state);
			
			// Also the state to a sequence specific state file.
			new FileReplicationStatePersistor(
					new File(workingDirectory, state.getSequenceNumber()
					+ SEQUENCE_STATE_FILE_SUFFIX), tmpStateFile).saveState(state);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (writer != null) {
			writer.release();
			writer = null;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReplicationState loadState() {
		if (state == null) {
			state = statePersistor.loadState();
		}
		
		return state;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveState(ReplicationState newState) {
		// The caller will usually be passing back the state object that was initially provided by
		// this class, however when initialising a new one will need to be created externally.
		state = newState;
		
		// Don't save the state to file at this point, we will write the state out during the
		// complete call in order to simulate a transaction.
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean stateExists() {
		return statePersistor.stateExists();
	}
}

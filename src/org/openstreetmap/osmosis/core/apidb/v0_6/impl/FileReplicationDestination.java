// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.io.File;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriter;


/**
 * A file-based destination for replication data.  This writes files beginning at 1.osc.gz and increasing incrementally.
 */
public class FileReplicationDestination implements ReplicationDestination {
	
	private static final String LOCK_FILE = "replicate.lock";
	private static final String STATE_FILE = "state.txt";
	private static final String TMP_STATE_FILE = "tmpstate.txt";
	private static final String SEQUENCE_STATE_FILE_SUFFIX = ".state.txt";
	private static final String CHANGE_FILE_SUFFIX = ".osc.gz";
	private static final CompressionMethod CHANGE_FILE_COMPRESSION = CompressionMethod.GZip;
	private static final String TMP_CHANGE_FILE = "tmpchangeset.osc.gz";
	

	private File workingDirectory;
	private File stateFile;
	private File tmpStateFile;
	private FileBasedLock fileLock;
	private boolean lockObtained;
	private FileReplicationStatePersistor statePersistor;
	private ReplicationState state;
	private XmlChangeWriter writer;
	private ReplicationFileSequenceFormatter sequenceFormatter;


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
		
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));
		
		statePersistor = new FileReplicationStatePersistor(stateFile, tmpStateFile);
		
		sequenceFormatter = new ReplicationFileSequenceFormatter();
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
	
	
	private void ensureLocked() {
		if (!lockObtained) {
			fileLock.lock();
			lockObtained = true;
		}
	}
	
	
	private void initializeWriter() {
		if (writer == null) {
			ensureLocked();
			
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
		ensureLocked();
		initializeWriter();
		
		writer.process(change);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		ensureLocked();
		
		// We can't do anything if we haven't loaded state yet.
		if (state != null) {
			String formattedSequenceNumber;
			
			// Get the formatted sequence number.
			formattedSequenceNumber = sequenceFormatter.getFormattedName(state.getSequenceNumber());
			
			// We won't write an output file if we are initializing.
			if (statePersistor.stateExists()) {
				// When replicating, we will always write a file even if there is no data.
				initializeWriter();
				
				// Close the output file and rename it as a sequenced change file.
				// We must release the file completely before we attempt to rename it.
				writer.complete();
				writer.release();
				writer = null;
				renameFile(
						new File(workingDirectory, TMP_CHANGE_FILE),
						new File(workingDirectory, formattedSequenceNumber + CHANGE_FILE_SUFFIX));
			}
			
			// The final step is to save the current state. This must be done last so that if a crash
			// occurs during processing it starts from the same point as last time.
			statePersistor.saveState(state);
			
			// Also the state to a sequence specific state file.
			new FileReplicationStatePersistor(
					new File(workingDirectory, formattedSequenceNumber
					+ SEQUENCE_STATE_FILE_SUFFIX), tmpStateFile).saveState(state);
		}
		
		fileLock.unlock();
		lockObtained = false;
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
		
		fileLock.release();
		lockObtained = false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReplicationState loadState() {
		ensureLocked();
		
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
		ensureLocked();
		
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
		ensureLocked();
		
		return statePersistor.stateExists();
	}
}

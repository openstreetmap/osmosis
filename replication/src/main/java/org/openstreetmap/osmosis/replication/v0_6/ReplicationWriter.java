// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.util.PropertiesPersister;
import org.openstreetmap.osmosis.replication.common.ReplicationFileSequenceFormatter;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeWriter;


/**
 * This class receives change streams and writes them to replication files. It
 * supports the initialize and complete method being called multiple times to
 * signify multiple replication intervals and each will be written to a
 * different replication file with a unique sequence number.
 * 
 * @author Brett Henderson
 */
public class ReplicationWriter implements ChangeSink {

	private static final Logger LOG = Logger.getLogger(ReplicationWriter.class.getName());
	private static final String LOCK_FILE = "replicate.lock";
	private static final String STATE_FILE = "state.txt";

	private FileBasedLock fileLock;
	private boolean lockObtained;
	private ReplicationFileSequenceFormatter sequenceFormatter;
	private PropertiesPersister statePersistor;
	private ReplicationState state;
	private XmlChangeWriter changeWriter;


	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationWriter(File workingDirectory) {
		// Create the lock object used to ensure only a single process attempts
		// to write to the data directory.
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));
		
		// Build the sequence formatter which converts sequence numbers into a
		// filename and creates the intermediate directories as required.
		sequenceFormatter = new ReplicationFileSequenceFormatter(workingDirectory);
		
		// Create the object used to persist current state.
		statePersistor = new PropertiesPersister(new File(workingDirectory, STATE_FILE));
	}


	@Override
	public void initialize(Map<String, Object> metaData) {
		if (lockObtained) {
			throw new OsmosisRuntimeException("initialize has already been called");
		}
		
		// Lock the working directory.
		fileLock.lock();
		lockObtained = true;
		
		// Get the replication state from the upstream task.
		if (!metaData.containsKey(ReplicationState.META_DATA_KEY)) {
			throw new OsmosisRuntimeException(
					"No replication state has been provided in metadata key " + ReplicationState.META_DATA_KEY + ".");
		}
		state = (ReplicationState) metaData.get(ReplicationState.META_DATA_KEY);
		
		// Populate the state from the existing state if it exists.
		if (statePersistor.exists()) {
			state.load(statePersistor.loadMap());
			
			// The current sequence number must now be incremented.
			state.setSequenceNumber(state.getSequenceNumber() + 1);
			
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Replication sequence number is " + state.getSequenceNumber() + ".");
			}
		}
		
		// Initialize a new change writer for the current sequence number.
		if (state.getSequenceNumber() > 0) {
			File changeFile = sequenceFormatter.getFormattedName(state.getSequenceNumber(), ".osc.gz");
			changeWriter = new XmlChangeWriter(changeFile, CompressionMethod.GZip);
		}
	}


	@Override
	public void process(ChangeContainer change) {
		if (!lockObtained) {
			throw new OsmosisRuntimeException("initialize has not been called");
		}
		
		if (state.getSequenceNumber() == 0) {
			throw new OsmosisRuntimeException("No changes can be included for replication sequence 0.");
		}
		
		changeWriter.process(change);
	}


	@Override
	public void complete() {
		if (!lockObtained) {
			throw new OsmosisRuntimeException("initialize has not been called");
		}
		
		if (state.getSequenceNumber() > 0) {
			// Complete the writing of the change file.
			changeWriter.complete();
			changeWriter.release();
			changeWriter = null;
		}
		
		// Write the sequenced state file.
		File stateFile = sequenceFormatter.getFormattedName(state.getSequenceNumber(), ".state.txt");
		new PropertiesPersister(stateFile).store(state.store());
		
		// Write the global state file.
		statePersistor.store(state.store());
		state = null;
		
		// Release the lock.
		fileLock.unlock();
		lockObtained = false;
	}


	@Override
	public void release() {
		if (changeWriter != null) {
			changeWriter.release();
			changeWriter = null;
		}
		state = null;
		
		fileLock.release();
		lockObtained = false;
	}
}

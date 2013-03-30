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
import org.openstreetmap.osmosis.replication.common.ReplicationState;


/**
 * This class manages persistence of state for replication streams into a state
 * properties file. If used alone, it will store the state for a replication
 * pipeline, and will discard the output. It can be used within a larger task
 * performing processing on the replication data. It supports the initialize and
 * complete method being called multiple times to signify multiple replication
 * intervals being called within a single pipeline run.
 * 
 * @author Brett Henderson
 */
public class ReplicationStateWriter implements ChangeSink {

	private static final Logger LOG = Logger.getLogger(ReplicationStateWriter.class.getName());
	private static final String LOCK_FILE = "replicate.lock";
	private static final String STATE_FILE = "state.txt";

	private FileBasedLock fileLock;
	private boolean lockObtained;
	private PropertiesPersister statePersistor;
	private ReplicationState state;


	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationStateWriter(File workingDirectory) {
		// Create the lock object used to ensure only a single process attempts
		// to write to the data directory.
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));

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
			throw new OsmosisRuntimeException("No replication state has been provided in metadata key "
					+ ReplicationState.META_DATA_KEY + ".");
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
	}


	@Override
	public void process(ChangeContainer change) {
		if (!lockObtained) {
			throw new OsmosisRuntimeException("initialize has not been called");
		}

		if (state.getSequenceNumber() == 0) {
			throw new OsmosisRuntimeException("No changes can be included for replication sequence 0.");
		}
	}


	@Override
	public void complete() {
		if (!lockObtained) {
			throw new OsmosisRuntimeException("initialize has not been called");
		}

		// Write the global state file.
		statePersistor.store(state.store());
		state = null;

		// Release the lock.
		fileLock.unlock();
		lockObtained = false;
	}


	@Override
	public void release() {
		state = null;

		fileLock.release();
		lockObtained = false;
	}
}

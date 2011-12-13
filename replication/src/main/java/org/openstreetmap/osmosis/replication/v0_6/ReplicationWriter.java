// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
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
	private static final String STATE_FILE = "state.txt";

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
		// Build the sequence formatter which converts sequence numbers into a
		// filename and creates the intermediate directories as required.
		sequenceFormatter = new ReplicationFileSequenceFormatter(workingDirectory);
		
		// Create the object used to persist current state.
		statePersistor = new PropertiesPersister(new File(workingDirectory, STATE_FILE));
	}


	@Override
	public void initialize(Map<String, Object> metaData) {
		if (changeWriter != null) {
			throw new OsmosisRuntimeException("initialize has already been called");
		}
		
		// Get the replication state from the upstream task.
		if (!metaData.containsKey(ReplicationState.META_DATA_KEY)) {
			throw new OsmosisRuntimeException(
					"No replication state has been provided in metadata key " + ReplicationState.META_DATA_KEY + ".");
		}
		state = (ReplicationState) metaData.get(ReplicationState.META_DATA_KEY);
		
		// Verify that the provided state is consistent with the existing state.
		if (statePersistor.exists()) {
			ReplicationState existingState = new ReplicationState(statePersistor.loadMap());
			long increment = state.getSequenceNumber() - existingState.getSequenceNumber();
			if (increment < 0 || increment > 1) {
				LOG.severe("Inconsistent sequence numbers.  Existing is "
						+ existingState.getSequenceNumber() + ", new is " + state.getSequenceNumber());
				throw new OsmosisRuntimeException(
						"Sequence number must be equal to or one greater than previous sequence number");
			}
			
		} else if (state.getSequenceNumber() > 0) {
			throw new OsmosisRuntimeException("Initial sequence number must be 0");
		}
		
		// Initialize a new change writer for the current sequence number.
		if (state.getSequenceNumber() > 0) {
			File changeFile = sequenceFormatter.getFormattedName(state.getSequenceNumber(), ".osc.gz");
			changeWriter = new XmlChangeWriter(changeFile, CompressionMethod.GZip);
		}
	}


	@Override
	public void process(ChangeContainer change) {
		if (state.getSequenceNumber() == 0) {
			throw new OsmosisRuntimeException("No changes can be included for replication sequence 0.");
		}
		
		changeWriter.process(change);
	}


	@Override
	public void complete() {
		if (state == null) {
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
	}


	@Override
	public void release() {
		if (changeWriter != null) {
			changeWriter.release();
			changeWriter = null;
		}
		state = null;
	}
}

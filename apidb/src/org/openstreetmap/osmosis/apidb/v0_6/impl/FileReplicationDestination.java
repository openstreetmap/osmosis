// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.io.File;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.replication.ReplicationFileSequenceFormatter;
import org.openstreetmap.osmosis.core.util.AtomicFileCreator;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriter;


/**
 * A file-based destination for replication data.  This writes files beginning at 1.osc.gz and increasing incrementally.
 */
public class FileReplicationDestination implements ReplicationDestination {
	
	private static final String LOCK_FILE = "replicate.lock";
	private static final String STATE_FILE = "state.txt";
	private static final String SEQUENCE_STATE_FILE_SUFFIX = ".state.txt";
	private static final String CHANGE_FILE_SUFFIX = ".osc.gz";
	private static final CompressionMethod CHANGE_FILE_COMPRESSION = CompressionMethod.GZip;
	

	private File stateFile;
	private FileBasedLock fileLock;
	private boolean lockObtained;
	private FileReplicationStatePersistor statePersistor;
	private ReplicationState state;
	private AtomicFileCreator atomicXmlFile;
	private XmlChangeWriter writer;
	private ReplicationFileSequenceFormatter sequenceFormatter;


	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory that all files will be produced in.
	 */
	public FileReplicationDestination(File workingDirectory) {
		stateFile = new File(workingDirectory, STATE_FILE);
		
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));
		
		statePersistor = new FileReplicationStatePersistor(stateFile);
		
		sequenceFormatter = new ReplicationFileSequenceFormatter(workingDirectory);
	}
	
	
	private void ensureLocked() {
		if (!lockObtained) {
			fileLock.lock();
			lockObtained = true;
		}
	}
	
	
	private File generateFormattedSequenceFile(String fileNameSuffix) {
		File formattedSequenceNumber;
		
		// Generate the formatted sequence number.
		formattedSequenceNumber = sequenceFormatter.getFormattedName(state.getSequenceNumber(), fileNameSuffix);
		
		return formattedSequenceNumber;
	}
	
	
	private void initializeWriter() {
		if (writer == null) {
			ensureLocked();
			
			// Create an atomic file creator for the xml file.
			atomicXmlFile = new AtomicFileCreator(generateFormattedSequenceFile(CHANGE_FILE_SUFFIX));
			
			// Create a writer writing to a new temporary file.
			writer = new XmlChangeWriter(
					atomicXmlFile.getTmpFile(),
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
			// We won't write an output file if we are initializing.
			if (statePersistor.stateExists()) {
				// When replicating, we will always write a file even if there is no data.
				initializeWriter();
				
				// Close the output file and rename it to the final file.
				writer.complete();
				writer.release();
				writer = null;
				atomicXmlFile.renameTmpFileToCurrent();
			}
			
			// The final step is to save the current state. This must be done last so that if a crash
			// occurs during processing it starts from the same point as last time.
			statePersistor.saveState(state);
			
			// Also the state to a sequence specific state file.
			new FileReplicationStatePersistor(generateFormattedSequenceFile(SEQUENCE_STATE_FILE_SUFFIX))
					.saveState(state);
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

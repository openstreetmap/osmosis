// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6;

import java.io.File;
import java.util.Date;

import org.openstreetmap.osmosis.core.apidb.v0_6.impl.FileReplicationStatePersistor;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationFileSequenceFormatter;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationState;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationStatePersister;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.merge.v0_6.impl.ReplicationDownloaderConfiguration;
import org.openstreetmap.osmosis.core.merge.v0_6.impl.ReplicationFileMergerConfiguration;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeReader;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeWriter;


/**
 * Consumes the files in a replication directory and combines them into larger replication files
 * grouped by a time interval. This allows replication files created at regular intervals to be
 * combined into larger files for more efficient consumption where latency is less of an issue.
 */
public class ReplicationFileMerger extends BaseReplicationDownloader {
	private static final String DATA_DIRECTORY = "data";
	private static final String CONFIG_FILE = "configuration.txt";
	private static final String DATA_STATE_FILE = "state.txt";
	
	
	private boolean writerActive;
	private XmlChangeWriter xmlWriter;
	private ReplicationState currentDataState;
	private ReplicationStatePersister dataStatePersister;
	private ReplicationFileSequenceFormatter replicationFileSequenceFormatter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationFileMerger(File workingDirectory) {
		super(workingDirectory);
		
		dataStatePersister = new FileReplicationStatePersistor(
				new File(getDataDirectory(), DATA_STATE_FILE),
				new File(getDataDirectory(), "tmp" + DATA_STATE_FILE));
		
		replicationFileSequenceFormatter = new ReplicationFileSequenceFormatter();
		
		writerActive = false;
	}
	
	
	private File getDataDirectory() {
		return new File(getWorkingDirectory(), DATA_DIRECTORY);
	}
	
	
	private Date alignDateToIntervalBoundary(Date requestedDate, long intervalLength) {
		long remainder;
		
		remainder = requestedDate.getTime() % intervalLength;
		
		if (remainder > 0) {
			return new Date(requestedDate.getTime() - remainder);
		} else {
			return requestedDate;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Date calculateMaximumTimestamp(ReplicationDownloaderConfiguration configuration, Date serverTimestamp,
			Date localTimestamp) {
		Date maximumTimestamp;
		long maxInterval;
		
		// Get the default maximum timestamp according to base calculations.
		maximumTimestamp = super.calculateMaximumTimestamp(configuration, serverTimestamp, localTimestamp);
		
		// Align the maximum timestamp to an interval boundary.
		maxInterval = configuration.getMaxInterval();
		if (maxInterval > 0) {
			maximumTimestamp = alignDateToIntervalBoundary(maximumTimestamp, maxInterval);
		}
		
		return maximumTimestamp;
	}
	
	
	private XmlChangeWriter buildResultFileWriter(long sequenceNumber) {
		File resultFile;
		
		resultFile = new File(
				getWorkingDirectory(),
				replicationFileSequenceFormatter.getFormattedName(currentDataState.getSequenceNumber()));
		
		return new XmlChangeWriter(
				resultFile,
				CompressionMethod.GZip);
	}
	
	
	private void persistSequencedCurrentState() {
		long sequenceNumber;
		FileReplicationStatePersistor statePersistor;
		File stateFile;
		File tmpStateFile;
		String stateFileName;
		
		sequenceNumber = currentDataState.getSequenceNumber();
		stateFileName = replicationFileSequenceFormatter.getFormattedName(sequenceNumber) + ".txt";
		
		stateFile = new File(getWorkingDirectory(), stateFileName);
		tmpStateFile = new File(getWorkingDirectory(), "tmp" + stateFileName);
		
		statePersistor = new FileReplicationStatePersistor(stateFile, tmpStateFile);
		
		statePersistor.saveState(currentDataState);
	}
	
	
	private void writeChangeset(XmlChangeReader xmlReader) {
		final ChangeSink localChangeSink = xmlWriter;
		
		xmlReader.setChangeSink(new ChangeSink() {
			ChangeSink suppressedWriter = localChangeSink;
			
			@Override
			public void process(ChangeContainer change) {
				suppressedWriter.process(change);
			}
			@Override
			public void complete() {
				// Suppress the call.
			}
			@Override
			public void release() {
				// Suppress the call.
			}});
		
		xmlReader.run();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processChangeset(XmlChangeReader xmlReader, ReplicationState replicationState) {
		int intervalLength;
		ReplicationFileMergerConfiguration configuration;
		
		configuration = new ReplicationFileMergerConfiguration(new File(getWorkingDirectory(), CONFIG_FILE));
		
		// Get the configured interval length.
		intervalLength = configuration.getIntervalLength();
		
		// If this is the first time through, initialise a writer for the next sequence number.
		if (!writerActive) {
			// Read the current persisted state.
			currentDataState = dataStatePersister.loadState();
			
			// Increment the current sequence number.
			currentDataState.setSequenceNumber(currentDataState.getSequenceNumber() + 1);
			
			// Initialise an output file for the new sequence number.
			xmlWriter = buildResultFileWriter(currentDataState.getSequenceNumber());
		}
		
		// Write the changeset to the writer. We write before checking if we've crossed a timestamp
		// boundary. A replication timestamp is guaranteed to contain all data up to that point in
		// time, but may contain data past that point in time. For that reason we apply replication
		// files until we have reached or passed the next interval, and to do this we apply a change
		// then check *afterwards* if the point in time has been reached.
		writeChangeset(xmlReader);
		
		if (intervalLength > 0) {
			// If this is the first time through, align the timestamp at the next boundary.
			if (!writerActive) {
				Date intervalEnd;
				
				intervalEnd = new Date(currentDataState.getTimestamp().getTime() + intervalLength);
				intervalEnd = alignDateToIntervalBoundary(intervalEnd, intervalLength);
				currentDataState.setTimestamp(intervalEnd);
			}
			
			// If the replication state has moved us past the current interval end point we need to
			// open a new interval. This may occur many times if the current replication state moves
			// us past several intervals.
			while (replicationState.getTimestamp().compareTo(currentDataState.getTimestamp()) > 0) {
				// If we have an open changeset writer, close it and save the current state.
				xmlWriter.complete();
				xmlWriter.release();
				
				persistSequencedCurrentState();
				dataStatePersister.saveState(currentDataState);
				
				// Update the state to match the next interval.
				currentDataState.setSequenceNumber(currentDataState.getSequenceNumber() + 1);
				currentDataState.setTimestamp(
						new Date(currentDataState.getTimestamp().getTime() + configuration.getIntervalLength()));
				
				// Begin a new interval.
				xmlWriter = buildResultFileWriter(currentDataState.getSequenceNumber());
			}
			
		} else {
			// There is no maximum interval set, so simply update the current state based on the
			// current replication state.
			currentDataState.setTimestamp(replicationState.getTimestamp());
		}
		
		// We are guaranteed to have an active writer at this point.
		writerActive = true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processComplete() {
		if (writerActive) {
			xmlWriter.complete();
			persistSequencedCurrentState();
			dataStatePersister.saveState(currentDataState);
			
			xmlWriter.release();
			xmlWriter = null;
			
			writerActive = false;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRelease() {
		if (writerActive) {
			xmlWriter.release();
			writerActive = false;
		}
	}
}

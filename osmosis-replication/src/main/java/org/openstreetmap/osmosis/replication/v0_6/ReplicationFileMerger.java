// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeForStreamableApplierComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeSorter;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.util.PropertiesPersister;
import org.openstreetmap.osmosis.replication.common.FileReplicationStore;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.replication.common.ReplicationStore;
import org.openstreetmap.osmosis.replication.v0_6.impl.ReplicationDownloaderConfiguration;
import org.openstreetmap.osmosis.replication.v0_6.impl.ReplicationFileMergerConfiguration;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeReader;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeWriter;


/**
 * Consumes the files in a replication directory and combines them into larger
 * replication files grouped by a time interval. This allows replication files
 * created at regular intervals to be combined into larger files for more
 * efficient consumption where latency is less of an issue.
 */
public class ReplicationFileMerger extends BaseReplicationDownloader {
	private static final Logger LOG = Logger.getLogger(ReplicationFileMerger.class.getName());

	private static final String DATA_DIRECTORY = "data";
	private static final String CONFIG_FILE = "configuration.txt";

	private boolean sinkActive;
	private ChangeSink changeSink;
	private ReplicationState currentDataState;
	private PropertiesPersister dataStatePersister;
	private ReplicationStore replicationStore;


	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationFileMerger(File workingDirectory) {
		super(workingDirectory);

		replicationStore = new FileReplicationStore(new File(getWorkingDirectory(), DATA_DIRECTORY), true);

		sinkActive = false;
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
		long intervalLength;

		// Read the current persisted state.
		currentDataState = new ReplicationState(dataStatePersister.loadMap());

		// Get the default maximum timestamp according to base calculations.
		maximumTimestamp = super.calculateMaximumTimestamp(configuration, serverTimestamp, localTimestamp);

		// Align the maximum timestamp to an interval boundary.
		intervalLength = getConfiguration().getIntervalLength();
		if (intervalLength > 0) {
			maximumTimestamp = alignDateToIntervalBoundary(maximumTimestamp, intervalLength);

			// For the first sequence file, we make sure we make sure that the
			// maximum timestamp is
			// ahead of the data timestamp. If it isn't, we move the maximum
			// timestamp backwards by
			// one interval to address the case where the local timestamp is
			// behind the data
			// timestamp causing some data to be downloaded and processed.
			if (currentDataState.getSequenceNumber() == 0) {
				if (maximumTimestamp.compareTo(currentDataState.getTimestamp()) <= 0) {
					maximumTimestamp = new Date(maximumTimestamp.getTime() - intervalLength);
				}
			}
		}

		// If the maximum timestamp exceeds the current local timestamp, but
		// does not exceed the current data timestamp then we shouldn't perform
		// any processing. If we download data we'll be forced to open a new
		// data file for the next interval which will not be populated fully
		// if the maximum timestamp is not high enough. To stop processing, we
		// simply set the maximum timestamp to equal the current local
		// timestamp.
		if ((maximumTimestamp.compareTo(localTimestamp) > 0)
				&& (maximumTimestamp.compareTo(currentDataState.getTimestamp()) <= 0)) {
			maximumTimestamp = localTimestamp;
		}
		
		LOG.finer("Maximum timestamp is " + maximumTimestamp);

		return maximumTimestamp;
	}


	private ChangeSink buildResultWriter(long sequenceNumber) {
		XmlChangeWriter xmlChangeWriter;
		ChangeSorter changeSorter;

		xmlChangeWriter = replicationStore.saveData(sequenceNumber);

		changeSorter = new ChangeSorter(new ChangeForStreamableApplierComparator());
		changeSorter.setChangeSink(xmlChangeWriter);

		return changeSorter;
	}


	private void writeChangeset(XmlChangeReader xmlReader) {
		final ChangeSink localChangeSink = changeSink;

		xmlReader.setChangeSink(new ChangeSink() {
			private ChangeSink suppressedWriter = localChangeSink;


			@Override
			public void initialize(Map<String, Object> metaData) {
				// Suppress the call.
			}


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
			}
		});

		xmlReader.run();
	}


	private ReplicationFileMergerConfiguration getConfiguration() {
		return new ReplicationFileMergerConfiguration(new File(getWorkingDirectory(), CONFIG_FILE));
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processInitialize(Map<String, Object> metaData) {
		// Do nothing.
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processInitializeState(ReplicationState initialState) {
		Date initialDate;
		Date alignedDate;
		long intervalLength;

		intervalLength = getConfiguration().getIntervalLength();

		initialDate = initialState.getTimestamp();

		// Align the date to an interval boundary.
		alignedDate = alignDateToIntervalBoundary(initialDate, intervalLength);

		// If the date has been moved, then advance it to the next interval. We
		// do this because
		// during replication we never claim to have covered a time period that
		// we haven't received
		// data for. We may include extra data from a previous interval. By
		// advancing the stated
		// initial timestamp to the next interval our first replication will
		// include some data from
		// the previous interval.
		if (alignedDate.compareTo(initialDate) < 0) {
			alignedDate = new Date(alignedDate.getTime() + intervalLength);
		}

		// Create an initial replication state object.
		currentDataState = new ReplicationState(alignedDate, 0);

		// Write out the initial "0" state file.
		replicationStore.saveState(currentDataState);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processChangeset(XmlChangeReader xmlReader, ReplicationState replicationState) {
		int intervalLength;
		ReplicationFileMergerConfiguration configuration;

		configuration = getConfiguration();

		// Get the configured interval length.
		intervalLength = configuration.getIntervalLength();

		// If this is the first time through, initialise a writer for the next
		// sequence number.
		if (!sinkActive) {
			// Increment the current sequence number.
			currentDataState.setSequenceNumber(currentDataState.getSequenceNumber() + 1);

			// Initialise an output file for the new sequence number.
			LOG.finer("Opening change sink for interval with sequence number " + currentDataState.getSequenceNumber());
			changeSink = buildResultWriter(currentDataState.getSequenceNumber());
		}

		if (intervalLength > 0) {
			// If this is the first time through, align the timestamp at the
			// next boundary.
			if (!sinkActive) {
				Date intervalEnd;

				intervalEnd = new Date(currentDataState.getTimestamp().getTime() + intervalLength);
				intervalEnd = alignDateToIntervalBoundary(intervalEnd, intervalLength);
				currentDataState.setTimestamp(intervalEnd);
				LOG.finer("End of current interval is " + intervalEnd);
			}

			// If the replication state has moved us past the current interval
			// end point we need to
			// open a new interval. This may occur many times if the current
			// replication state moves
			// us past several intervals.
			while (replicationState.getTimestamp().compareTo(currentDataState.getTimestamp()) > 0) {

				// If we have an open changeset writer, close it and save the
				// current state.
				LOG.finer("Closing change sink for interval with sequence number "
						+ currentDataState.getSequenceNumber());
				changeSink.complete();
				changeSink.release();

				replicationStore.saveState(currentDataState);

				// Update the state to match the next interval.
				currentDataState.setSequenceNumber(currentDataState.getSequenceNumber() + 1);
				currentDataState.setTimestamp(new Date(currentDataState.getTimestamp().getTime()
						+ configuration.getIntervalLength()));

				// Begin a new interval.
				LOG.finer("Opening change sink for interval with sequence number "
						+ currentDataState.getSequenceNumber());
				changeSink = buildResultWriter(currentDataState.getSequenceNumber());
			}

		} else {
			// There is no maximum interval set, so simply update the current
			// state based on the
			// current replication state.
			LOG.finer("End of current interval is " + replicationState.getTimestamp());
			currentDataState.setTimestamp(replicationState.getTimestamp());
		}

		// Write the changeset to the writer.
		writeChangeset(xmlReader);

		// We are guaranteed to have an active writer at this point.
		sinkActive = true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processComplete() {
		if (sinkActive) {
			LOG.finer("Closing change sink for interval with sequence number " + currentDataState.getSequenceNumber());
			changeSink.complete();
			replicationStore.saveState(currentDataState);

			changeSink.release();
			changeSink = null;

			sinkActive = false;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRelease() {
		if (sinkActive) {
			changeSink.release();
			sinkActive = false;
		}
	}
}

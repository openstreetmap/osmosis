// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

import java.io.File;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeForStreamableApplierComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeSorter;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.openstreetmap.osmosis.replication.common.ReplicationState;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeReader;


/**
 * Downloads a set of replication files from a HTTP server, and merges them into a
 * single output stream. It tracks the intervals covered by the current files
 * and stores the current timestamp between invocations forming the basis of a
 * replication mechanism.
 * 
 * @author Brett Henderson
 */
public class ReplicationDownloader extends BaseReplicationDownloader implements RunnableChangeSource {
	
	private ChangeSorter changeSorter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationDownloader(File workingDirectory) {
		super(workingDirectory);
		
		// We will sort all contents prior to sending to the sink. This adds overhead that may not
		// always be required, but provides consistent behaviour.
		changeSorter = new ChangeSorter(new ChangeForStreamableApplierComparator());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		changeSorter.setChangeSink(changeSink);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processInitialize(Map<String, Object> metaData) {
		changeSorter.initialize(metaData);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processInitializeState(ReplicationState initialState) {
		// Nothing to do.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processChangeset(XmlChangeReader xmlReader, ReplicationState replicationState) {
		final ChangeSink localChangeSink = changeSorter;
		
		xmlReader.setChangeSink(new ChangeSink() {
			private ChangeSink suppressedChangeSink = localChangeSink;

			@Override
			public void initialize(Map<String, Object> metaData) {
				// Suppress the call.
			}
			@Override
			public void process(ChangeContainer change) {
				suppressedChangeSink.process(change);
			}
			@Override
			public void complete() {
				// Suppress the call.
			}
			@Override
			public void release() {
				// Suppress the call.
			} });
		
		xmlReader.run();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processComplete() {
		changeSorter.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRelease() {
		changeSorter.release();
	}
}

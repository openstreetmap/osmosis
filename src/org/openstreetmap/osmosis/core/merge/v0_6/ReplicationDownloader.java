// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.apidb.v0_6.impl.ReplicationState;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.openstreetmap.osmosis.core.xml.v0_6.XmlChangeReader;


/**
 * Downloads a set of replication files from a HTTP server, and merges them into a
 * single output stream. It tracks the intervals covered by the current files
 * and stores the current timestamp between invocations forming the basis of a
 * replication mechanism.
 * 
 * @author Brett Henderson
 */
public class ReplicationDownloader extends BaseReplicationDownloader implements RunnableChangeSource {
	
	private ChangeSink changeSink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ReplicationDownloader(File workingDirectory) {
		super(workingDirectory);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processChangeset(XmlChangeReader xmlReader, ReplicationState replicationState) {
		final ChangeSink localChangeSink = changeSink;
		
		xmlReader.setChangeSink(new ChangeSink() {
			ChangeSink suppressedChangeSink = localChangeSink;
			
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
			}});
		
		xmlReader.run();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processComplete() {
		changeSink.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processRelease() {
		changeSink.release();
	}
}

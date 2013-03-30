// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.io.File;


/**
 * Builds Netty channel pipelines for new client connections.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataServerChannelPipelineFactory extends SequenceServerChannelPipelineFactory {
	
	private File dataDirectory;


	/**
	 * Creates a new instance.
	 * 
	 * @param dataDirectory
	 *            The location of the replication data files.
	 */
	public ReplicationDataServerChannelPipelineFactory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}


	@Override
	protected SequenceServerHandler createHandler(SequenceServerControl control) {
		return new ReplicationDataServerHandler(control, dataDirectory);
	}
}

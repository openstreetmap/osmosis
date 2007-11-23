package com.bretth.osmosis.core.pgsql.common.v0_5;

import java.io.File;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.pgsql.common.v0_5.impl.AllocatingUserIdManager;
import com.bretth.osmosis.core.pgsql.common.v0_5.impl.NodeCopyFileWriter;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * An OSM data sink for storing all data to a database dump file. This task is
 * intended for populating an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDumpWriter implements Sink, EntityProcessor {
	
	private static final String NODE_SUFFIX = "TblNode.txt";
	private static final String NODE_TAG_SUFFIX = "TblNodeTag.txt";
	
	
	private AllocatingUserIdManager userIdManager;
	private NodeCopyFileWriter nodeWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param filePrefix
	 *            The prefix to prepend to all generated file names.
	 */
	public PostgreSqlDumpWriter(File filePrefix) {
		userIdManager = new AllocatingUserIdManager();
		
		nodeWriter = new NodeCopyFileWriter(
			userIdManager,
			new File(filePrefix.getPath() + NODE_SUFFIX),
			new File(filePrefix.getPath() + NODE_TAG_SUFFIX)
		);
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		nodeWriter.complete();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		nodeWriter.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		nodeWriter.writeRecord(nodeContainer.getEntity());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		// Do nothing.
	}
}

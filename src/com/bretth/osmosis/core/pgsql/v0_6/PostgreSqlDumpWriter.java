// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6;

import java.io.File;
import java.util.Map.Entry;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.pgsql.v0_6.impl.AllocatingUserIdManager;
import com.bretth.osmosis.core.pgsql.v0_6.impl.NodeCopyFileWriter;
import com.bretth.osmosis.core.pgsql.v0_6.impl.UserFileWriter;
import com.bretth.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to a database dump file. This task is
 * intended for populating an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDumpWriter implements Sink, EntityProcessor {
	
	private static final String NODE_SUFFIX = "TblNode.txt";
	private static final String NODE_TAG_SUFFIX = "TblNodeTag.txt";
	private static final String USER_SUFFIX = "TblUser.txt";
	
	
	private AllocatingUserIdManager userIdManager;
	private NodeCopyFileWriter nodeWriter;
	private UserFileWriter userWriter;
	
	
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
		userWriter = new UserFileWriter(new File(filePrefix.getPath() + USER_SUFFIX));
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
	public void process(BoundContainer boundContainer) {
		// Do nothing.
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
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		nodeWriter.complete();
		
		// Write out the user information.
		for (Entry<String, Long> userEntry : userIdManager.getUserIdMap().entrySet()) {
			userWriter.writeRecord(userEntry.getValue().longValue(), userEntry.getKey());
		}
		userWriter.complete();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		nodeWriter.release();
		userWriter.release();
	}
}

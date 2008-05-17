// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.io.File;

import org.postgresql.geometric.PGpoint;

import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.pgsql.common.CopyFileWriter;


/**
 * Writes node data to a file in a format suitable for populating a node table
 * in the database using a COPY statement.
 * 
 * @author Brett Henderson
 */
public class NodeCopyFileWriter {
	
	private AllocatingUserIdManager userIdManager;
	private CopyFileWriter nodeWriter;
	private CopyFileWriter nodeTagWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param userIdManager
	 *            Provides user ids for user names.
	 * @param nodeFile
	 *            The file to write node data to.
	 * @param nodeTagFile
	 *            The file to write node tag data to.
	 */
	public NodeCopyFileWriter(AllocatingUserIdManager userIdManager, File nodeFile, File nodeTagFile) {
		this.userIdManager = userIdManager;
		
		nodeWriter = new CopyFileWriter(nodeFile);
		nodeTagWriter = new CopyFileWriter(nodeTagFile);
	}
	
	
	/**
	 * Writes the specified record to the database.
	 * 
	 * @param node
	 *            The node record to be written.
	 */
	public void writeRecord(Node node) {
		long id;
		
		id = node.getId();
		
		// Id
		nodeWriter.writeField(id);
		// Version
		nodeWriter.writeField(1);
		// Timestamp
		nodeWriter.writeField(node.getTimestamp());
		// Visible
		nodeWriter.writeField(true);
		// User Id
		nodeWriter.writeField(userIdManager.getUserId(node.getUser()));
		// Location
		nodeWriter.writeField(new PGpoint(node.getLongitude(), node.getLatitude()));
		
		nodeWriter.endRecord();
		
		for (Tag tag : node.getTagList()) {
			writeTagRecord(id, tag);
		}
	}
	
	
	/**
	 * Writes the specified tag record to the database.
	 * 
	 * @param tag
	 */
	private void writeTagRecord(long nodeId, Tag tag) {
		// Node Id
		nodeTagWriter.writeField(nodeId);
		// Node Version
		nodeTagWriter.writeField(1);
		// Key
		nodeTagWriter.writeField(tag.getKey());
		// Value
		nodeTagWriter.writeField(tag.getValue());
		
		nodeTagWriter.endRecord();
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		nodeWriter.complete();
	}
	
	
	/**
	 * Releases all resources.
	 */
	public void release() {
		nodeWriter.release();
	}
}

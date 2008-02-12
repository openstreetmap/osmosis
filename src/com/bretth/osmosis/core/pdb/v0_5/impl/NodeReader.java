// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Reads all nodes from a database ordered by their identifier. It combines the
 * output of the node table readers to produce fully configured node objects.
 * 
 * @author Brett Henderson
 */
public class NodeReader implements ReleasableIterator<Node> {
	
	private ReleasableIterator<Node> nodeReader;
	private PeekableIterator<DBEntityTag> nodeTagReader;
	private Node nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public NodeReader(DatabaseContext dbCtx) {
		nodeReader = new NodeTableReader(dbCtx);
		nodeTagReader = new PeekableIterator<DBEntityTag>(
			new EntityTagTableReader(dbCtx, "node_tag", "node_id")
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && nodeReader.hasNext()) {
			long nodeId;
			Node node;
			
			node = nodeReader.next();
			nodeId = node.getId();
			
			// Skip all node tags that are from a lower node.
			while (nodeTagReader.hasNext()) {
				DBEntityTag nodeTag;
				
				nodeTag = nodeTagReader.next();
				
				if (nodeTag.getEntityId() < nodeId) {
					nodeTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags matching this version of the node.
			while (nodeTagReader.hasNext() && nodeTagReader.peekNext().getEntityId() == nodeId) {
				node.addTag(nodeTagReader.next().getTag());
			}
			
			nextValue = node;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Node next() {
		Node result;
		
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		result = nextValue;
		nextValueLoaded = false;
		
		return result;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		nodeReader.release();
		nodeTagReader.release();
	}
}

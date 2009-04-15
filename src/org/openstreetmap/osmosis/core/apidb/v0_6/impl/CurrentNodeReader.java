// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads current node from a database ordered by their identifier. It combines the
 * output of the node table readers to produce fully configured node objects.
 * 
 * @author Brett Henderson
 */
public class CurrentNodeReader implements ReleasableIterator<Node> {
	
	private ReleasableIterator<Node> nodeReader;
	private PeekableIterator<DbFeature<Tag>> nodeTagReader;
	private Node nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public CurrentNodeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		nodeReader = new PersistentIterator<Node>(
			new SingleClassObjectSerializationFactory(Node.class),
			new CurrentNodeTableReader(loginCredentials, readAllUsers),
			"nod",
			true
		);
		nodeTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new CurrentEntityTagTableReader(loginCredentials, "current_node_tags"),
				"nodtag",
				true
			)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && nodeReader.hasNext()) {
			Node node;
			long nodeId;
			
			node = nodeReader.next();
			
			nodeId = node.getId();
			
			// Skip all node tags that are from lower id node.
			while (nodeTagReader.hasNext()) {
				DbFeature<Tag> nodeTag;
				
				nodeTag = nodeTagReader.peekNext();
				
				if (nodeTag.getEntityId() < nodeId) {
					nodeTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags for this node.
			while (nodeTagReader.hasNext() && nodeTagReader.peekNext().getEntityId() == nodeId) {
				node.getTags().add(nodeTagReader.next().getFeature());
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

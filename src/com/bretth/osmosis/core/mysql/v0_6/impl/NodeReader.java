// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads all nodes from a database ordered by their identifier. It combines the
 * output of the node table readers to produce fully configured node objects.
 * 
 * @author Brett Henderson
 */
public class NodeReader implements ReleasableIterator<EntityHistory<Node>> {
	
	private ReleasableIterator<EntityHistory<Node>> nodeReader;
	private PeekableIterator<DbFeatureHistory<DbFeature<Tag>>> nodeTagReader;
	private EntityHistory<Node> nextValue;
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
	public NodeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		nodeReader = new PersistentIterator<EntityHistory<Node>>(
			new SingleClassObjectSerializationFactory(EntityHistory.class),
			new NodeTableReader(loginCredentials, readAllUsers),
			"nod",
			true
		);
		nodeTagReader = new PeekableIterator<DbFeatureHistory<DbFeature<Tag>>>(
			new PersistentIterator<DbFeatureHistory<DbFeature<Tag>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new EntityTagTableReader(loginCredentials, "node_tags"),
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
			EntityHistory<Node> nodeHistory;
			long nodeId;
			int nodeVersion;
			Node node;
			
			nodeHistory = nodeReader.next();
			
			node = nodeHistory.getEntity();
			nodeId = node.getId();
			nodeVersion = node.getVersion();
			
			// Skip all node tags that are from lower id or lower version of the same id.
			while (nodeTagReader.hasNext()) {
				DbFeatureHistory<DbFeature<Tag>> nodeTagHistory;
				DbFeature<Tag> nodeTag;
				
				nodeTagHistory = nodeTagReader.peekNext();
				nodeTag = nodeTagHistory.getDbFeature();
				
				if (nodeTag.getEntityId() < nodeId) {
					nodeTagReader.next();
				} else if (nodeTag.getEntityId() == nodeId) {
					if (nodeTagHistory.getVersion() < nodeVersion) {
						nodeTagReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			// Load all tags matching this version of the node.
			while (nodeTagReader.hasNext() && nodeTagReader.peekNext().getDbFeature().getEntityId() == nodeId && nodeTagReader.peekNext().getVersion() == nodeVersion) {
				node.addTag(nodeTagReader.next().getDbFeature().getFeature());
			}
			
			nextValue = nodeHistory;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public EntityHistory<Node> next() {
		EntityHistory<Node> result;
		
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

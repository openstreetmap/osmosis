// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads current ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class CurrentWayReader implements ReleasableIterator<Way> {
	
	private ReleasableIterator<Way> wayReader;
	private PeekableIterator<DbFeature<Tag>> wayTagReader;
	private PeekableIterator<DbOrderedFeature<WayNode>> wayNodeReader;
	private Way nextValue;
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
	public CurrentWayReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		wayReader = new PersistentIterator<Way>(
			new SingleClassObjectSerializationFactory(Way.class),
			new CurrentWayTableReader(loginCredentials, readAllUsers),
			"way",
			true
		);
		wayTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new CurrentEntityTagTableReader(loginCredentials, "current_way_tags"),
				"waytag",
				true
			)
		);
		wayNodeReader = new PeekableIterator<DbOrderedFeature<WayNode>>(
			new PersistentIterator<DbOrderedFeature<WayNode>>(
				new SingleClassObjectSerializationFactory(DbOrderedFeature.class),
				new CurrentWayNodeTableReader(loginCredentials),
				"waynod",
				true
			)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && wayReader.hasNext()) {
			Way way;
			long wayId;
			List<DbOrderedFeature<WayNode>> wayNodes;
			
			way = wayReader.next();
			
			wayId = way.getId();
			
			// Skip all way tags that are from lower id way.
			while (wayTagReader.hasNext()) {
				DbFeature<Tag> wayTag;
				
				wayTag = wayTagReader.peekNext();
				
				if (wayTag.getEntityId() < wayId) {
					wayTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags for this way.
			while (wayTagReader.hasNext() && wayTagReader.peekNext().getEntityId() == wayId) {
				way.addTag(wayTagReader.next().getFeature());
			}
			
			// Skip all way nodes that are from lower id way.
			while (wayNodeReader.hasNext()) {
				DbOrderedFeature<WayNode> wayNode;
				
				wayNode = wayNodeReader.peekNext();
				
				if (wayNode.getEntityId() < wayId) {
					wayNodeReader.next();
				} else {
					break;
				}
			}
			
			// Load all nodes matching this way.
			wayNodes = new ArrayList<DbOrderedFeature<WayNode>>();
			while (wayNodeReader.hasNext() && wayNodeReader.peekNext().getEntityId() == wayId) {
				wayNodes.add(wayNodeReader.next());
			}
			// The underlying query sorts node references by way id but not
			// by their sequence number.
			Collections.sort(wayNodes, new DbOrderedFeatureComparator<WayNode>());
			for (DbOrderedFeature<WayNode> dbWayNode : wayNodes) {
				way.addWayNode(dbWayNode.getFeature());
			}
			
			nextValue = way;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Way next() {
		Way result;
		
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
		wayReader.release();
		wayTagReader.release();
		wayNodeReader.release();
	}
}

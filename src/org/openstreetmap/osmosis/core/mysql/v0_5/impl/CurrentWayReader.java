// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads current ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class CurrentWayReader implements ReleasableIterator<Way> {
	
	private ReleasableIterator<Way> wayReader;
	private PeekableIterator<DBEntityTag> wayTagReader;
	private PeekableIterator<DBWayNode> wayNodeReader;
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
		wayTagReader = new PeekableIterator<DBEntityTag>(
			new PersistentIterator<DBEntityTag>(
				new SingleClassObjectSerializationFactory(DBEntityTag.class),
				new CurrentEntityTagTableReader(loginCredentials, "current_way_tags"),
				"waytag",
				true
			)
		);
		wayNodeReader = new PeekableIterator<DBWayNode>(
			new PersistentIterator<DBWayNode>(
				new SingleClassObjectSerializationFactory(DBWayNode.class),
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
			List<DBWayNode> wayNodes;
			
			way = wayReader.next();
			
			wayId = way.getId();
			
			// Skip all way tags that are from lower id way.
			while (wayTagReader.hasNext()) {
				DBEntityTag wayTag;
				
				wayTag = wayTagReader.peekNext();
				
				if (wayTag.getEntityId() < wayId) {
					wayTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags for this way.
			while (wayTagReader.hasNext() && wayTagReader.peekNext().getEntityId() == wayId) {
				way.addTag(wayTagReader.next().getTag());
			}
			
			// Skip all way nodes that are from lower id or lower version of the same id.
			while (wayNodeReader.hasNext()) {
				DBWayNode wayNode;
				
				wayNode = wayNodeReader.peekNext();
				
				if (wayNode.getWayId() < wayId) {
					wayNodeReader.next();
				} else {
					break;
				}
			}
			
			// Load all nodes matching this way.
			wayNodes = new ArrayList<DBWayNode>();
			while (wayNodeReader.hasNext() && wayNodeReader.peekNext().getWayId() == wayId) {
				wayNodes.add(wayNodeReader.next());
			}
			// The underlying query sorts node references by way id but not
			// by their sequence number.
			Collections.sort(wayNodes, new WayNodeComparator());
			for (DBWayNode dbWayNode : wayNodes) {
				way.addWayNode(dbWayNode.getWayNode());
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

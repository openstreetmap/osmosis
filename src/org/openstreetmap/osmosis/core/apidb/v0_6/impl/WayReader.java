// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads all ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class WayReader implements ReleasableIterator<EntityHistory<Way>> {
	
	private ReleasableIterator<EntityHistory<Way>> wayReader;
	private PeekableIterator<DbFeatureHistory<DbFeature<Tag>>> wayTagReader;
	private PeekableIterator<DbFeatureHistory<DbOrderedFeature<WayNode>>> wayNodeReader;
	private EntityHistory<Way> nextValue;
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
	public WayReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		wayReader = new PersistentIterator<EntityHistory<Way>>(
			new SingleClassObjectSerializationFactory(EntityHistory.class),
			new WayTableReader(loginCredentials, readAllUsers),
			"way",
			true
		);
		wayTagReader = new PeekableIterator<DbFeatureHistory<DbFeature<Tag>>>(
			new PersistentIterator<DbFeatureHistory<DbFeature<Tag>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new EntityTagTableReader(loginCredentials, "way_tags"),
				"waytag",
				true
			)
		);
		wayNodeReader = new PeekableIterator<DbFeatureHistory<DbOrderedFeature<WayNode>>>(
			new PersistentIterator<DbFeatureHistory<DbOrderedFeature<WayNode>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new WayNodeTableReader(loginCredentials),
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
			EntityHistory<Way> wayHistory;
			long wayId;
			int wayVersion;
			Way way;
			List<DbOrderedFeature<WayNode>> wayNodes;
			
			wayHistory = wayReader.next();
			
			way = wayHistory.getEntity();
			wayId = way.getId();
			wayVersion = way.getVersion();
			
			// Skip all way tags that are from lower id or lower version of the same id.
			while (wayTagReader.hasNext()) {
				DbFeatureHistory<DbFeature<Tag>> wayTagHistory;
				DbFeature<Tag> wayTag;
				
				wayTagHistory = wayTagReader.peekNext();
				wayTag = wayTagHistory.getDbFeature();
				
				if (wayTag.getEntityId() < wayId) {
					wayTagReader.next();
				} else if (wayTag.getEntityId() == wayId) {
					if (wayTagHistory.getVersion() < wayVersion) {
						wayTagReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			// Load all tags matching this version of the way.
			while (
					wayTagReader.hasNext()
					&& wayTagReader.peekNext().getDbFeature().getEntityId() == wayId
					&& wayTagReader.peekNext().getVersion() == wayVersion) {
				way.getTags().add(wayTagReader.next().getDbFeature().getFeature());
			}
			
			// Skip all way nodes that are from lower id or lower version of the same id.
			while (wayNodeReader.hasNext()) {
				DbFeatureHistory<DbOrderedFeature<WayNode>> wayNodeHistory;
				DbOrderedFeature<WayNode> wayNode;
				
				wayNodeHistory = wayNodeReader.peekNext();
				wayNode = wayNodeHistory.getDbFeature();
				
				if (wayNode.getEntityId() < wayId) {
					wayNodeReader.next();
				} else if (wayNode.getEntityId() == wayId) {
					if (wayNodeHistory.getVersion() < wayVersion) {
						wayNodeReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			// Load all nodes matching this version of the way.
			wayNodes = new ArrayList<DbOrderedFeature<WayNode>>();
			while (
					wayNodeReader.hasNext()
					&& wayNodeReader.peekNext().getDbFeature().getEntityId() == wayId
					&& wayNodeReader.peekNext().getVersion() == wayVersion) {
				wayNodes.add(wayNodeReader.next().getDbFeature());
			}
			// The underlying query sorts node references by way id but not
			// by their sequence number.
			Collections.sort(wayNodes, new DbOrderedFeatureComparator<WayNode>());
			for (DbOrderedFeature<WayNode> dbWayNode : wayNodes) {
				way.getWayNodes().add(dbWayNode.getFeature());
			}
			
			nextValue = wayHistory;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public EntityHistory<Way> next() {
		EntityHistory<Way> result;
		
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

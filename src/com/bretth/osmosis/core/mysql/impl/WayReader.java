package com.bretth.osmosis.core.mysql.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.data.Way;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Reads all ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class WayReader implements ReleasableIterator<EntityHistory<Way>> {
	
	private WayTableReader wayReader;
	private PeekableIterator<EntityHistory<WayTag>> wayTagReader;
	private PeekableIterator<EntityHistory<WaySegment>> waySegmentReader;
	private EntityHistory<Way> nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 */
	public WayReader(String host, String database, String user, String password) {
		wayReader = new WayTableReader(host, database, user, password);
		wayTagReader = new PeekableIterator<EntityHistory<WayTag>>(
			new WayTagTableReader(host, database, user, password)
		);
		waySegmentReader = new PeekableIterator<EntityHistory<WaySegment>>(
			new WaySegmentTableReader(host, database, user, password)
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
			
			wayHistory = wayReader.next();
			
			way = wayHistory.getEntity();
			wayId = way.getId();
			wayVersion = wayHistory.getVersion();
			
			// Skip all way tags that are from lower id or lower version of the same id.
			while (wayTagReader.hasNext()) {
				EntityHistory<WayTag> wayTagHistory;
				WayTag wayTag;
				
				wayTagHistory = wayTagReader.peekNext();
				wayTag = wayTagHistory.getEntity();
				
				if (wayTag.getWayId() < wayId) {
					wayTagReader.next();
				} else if (wayTag.getWayId() == wayId) {
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
			while (wayTagReader.hasNext() && wayTagReader.peekNext().getEntity().getWayId() == wayId && wayTagReader.peekNext().getVersion() == wayVersion) {
				way.addTag(wayTagReader.next().getEntity());
			}
			
			// Skip all way segments that are from lower id or lower version of the same id.
			while (waySegmentReader.hasNext()) {
				EntityHistory<WaySegment> waySegmentHistory;
				WaySegment waySegment;
				
				waySegmentHistory = waySegmentReader.peekNext();
				waySegment = waySegmentHistory.getEntity();
				
				if (waySegment.getWayId() < wayId) {
					waySegmentReader.next();
				} else if (waySegment.getWayId() == wayId) {
					if (waySegmentHistory.getVersion() < wayVersion) {
						waySegmentReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			// Load all segments matching this version of the way.
			while (waySegmentReader.hasNext() && waySegmentReader.peekNext().getEntity().getWayId() == wayId && waySegmentReader.peekNext().getVersion() == wayVersion) {
				way.addSegmentReference(waySegmentReader.next().getEntity());
			}
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
		waySegmentReader.release();
	}
}

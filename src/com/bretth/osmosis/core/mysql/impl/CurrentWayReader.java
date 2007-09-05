package com.bretth.osmosis.core.mysql.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.domain.v0_4.SegmentReference;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Reads current ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class CurrentWayReader implements ReleasableIterator<Way> {
	
	private ReleasableIterator<Way> wayReader;
	private PeekableIterator<WayTag> wayTagReader;
	private PeekableIterator<WaySegment> waySegmentReader;
	private Way nextValue;
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
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public CurrentWayReader(String host, String database, String user, String password, boolean readAllUsers) {
		wayReader = new PersistentIterator<Way>(
			new CurrentWayTableReader(host, database, user, password, readAllUsers),
			"way",
			true
		);
		wayTagReader = new PeekableIterator<WayTag>(
			new PersistentIterator<WayTag>(
				new CurrentWayTagTableReader(host, database, user, password),
				"waytag",
				true
			)
		);
		waySegmentReader = new PeekableIterator<WaySegment>(
			new PersistentIterator<WaySegment>(
				new CurrentWaySegmentTableReader(host, database, user, password),
				"wayseg",
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
			List<WaySegment> waySegments;
			
			way = wayReader.next();
			
			wayId = way.getId();
			
			// Skip all way tags that are from lower id way.
			while (wayTagReader.hasNext()) {
				WayTag wayTag;
				
				wayTag = wayTagReader.peekNext();
				
				if (wayTag.getWayId() < wayId) {
					wayTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags for this way.
			while (wayTagReader.hasNext() && wayTagReader.peekNext().getWayId() == wayId) {
				way.addTag(wayTagReader.next());
			}
			
			// Skip all way segments that are from lower id or lower version of the same id.
			while (waySegmentReader.hasNext()) {
				WaySegment waySegment;
				
				waySegment = waySegmentReader.peekNext();
				
				if (waySegment.getWayId() < wayId) {
					waySegmentReader.next();
				} else {
					break;
				}
			}
			
			// Load all segments matching this version of the way.
			waySegments = new ArrayList<WaySegment>();
			while (waySegmentReader.hasNext() && waySegmentReader.peekNext().getWayId() == wayId) {
				waySegments.add(waySegmentReader.next());
			}
			// The underlying query sorts segment references by way id but not
			// by their sequence number.
			Collections.sort(waySegments, new WaySegmentComparator());
			for (SegmentReference segmentReference : waySegments) {
				way.addSegmentReference(segmentReference);
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
		waySegmentReader.release();
	}
}

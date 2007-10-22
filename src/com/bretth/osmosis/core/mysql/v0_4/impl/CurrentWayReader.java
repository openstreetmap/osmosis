package com.bretth.osmosis.core.mysql.v0_4.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
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
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public CurrentWayReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		wayReader = new PersistentIterator<Way>(
			new CurrentWayTableReader(loginCredentials, readAllUsers),
			"way",
			true
		);
		wayTagReader = new PeekableIterator<WayTag>(
			new PersistentIterator<WayTag>(
				new CurrentWayTagTableReader(loginCredentials),
				"waytag",
				true
			)
		);
		waySegmentReader = new PeekableIterator<WaySegment>(
			new PersistentIterator<WaySegment>(
				new CurrentWaySegmentTableReader(loginCredentials),
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
			for (WaySegment waySegment : waySegments) {
				way.addSegmentReference(waySegment.getSegmentReference());
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

package com.bretth.osmosis.core.mysql.impl;

import java.util.Date;

import com.bretth.osmosis.core.container.ChangeContainer;
import com.bretth.osmosis.core.container.WayContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.Way;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.task.ChangeAction;


/**
 * Reads the set of way changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class WayChangeReader {
	
	private PeekableIterator<EntityHistory<Way>> wayHistoryReader;
	private PeekableIterator<EntityHistory<WaySegment>> waySegmentHistoryReader;
	private PeekableIterator<EntityHistory<WayTag>> wayTagHistoryReader;
	private ChangeContainer nextValue;
	
	
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
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public WayChangeReader(String host, String database, String user, String password, Date intervalBegin, Date intervalEnd) {
		wayHistoryReader =
			new PeekableIterator<EntityHistory<Way>>(
				new PersistentIterator<EntityHistory<Way>>(
					new WayHistoryReader(host, database, user, password, intervalBegin, intervalEnd),
					"way",
					true
				)
			);
		waySegmentHistoryReader =
			new PeekableIterator<EntityHistory<WaySegment>>(
				new PersistentIterator<EntityHistory<WaySegment>>(
					new WaySegmentHistoryReader(host, database, user, password, intervalBegin, intervalEnd),
					"wayseg",
					true
				)
			);
		wayTagHistoryReader =
			new PeekableIterator<EntityHistory<WayTag>>(
				new PersistentIterator<EntityHistory<WayTag>>(
					new WayTagHistoryReader(host, database, user, password, intervalBegin, intervalEnd),
					"waytag",
					true
				)
			);
	}
	
	
	/**
	 * Consolides the output of all history readers so that ways are fully
	 * populated.
	 * 
	 * @return A way history record where the way is fully populated with
	 *         segments and tags.
	 */
	private EntityHistory<Way> readNextWayHistory() {
		EntityHistory<Way> wayHistory;
		Way way;
		
		wayHistory = wayHistoryReader.next();
		way = wayHistory.getEntity();

		// Add all applicable segment references to the way.
		while (waySegmentHistoryReader.hasNext() &&
				waySegmentHistoryReader.peekNext().getEntity().getWayId() == way.getId() &&
				waySegmentHistoryReader.peekNext().getVersion() == wayHistory.getVersion()) {
			way.addSegmentReference(waySegmentHistoryReader.next().getEntity());
		}
		
		// Add all applicable tags to the way.
		while (wayTagHistoryReader.hasNext() &&
				wayTagHistoryReader.peekNext().getEntity().getWayId() == way.getId() &&
				wayTagHistoryReader.peekNext().getVersion() == wayHistory.getVersion()) {
			way.addTag(wayTagHistoryReader.next().getEntity());
		}
		
		return wayHistory;
	}
	
	
	/**
	 * Reads the history of the next entity and builds a change object.
	 */
	private ChangeContainer readChange() {
		boolean createdPreviously;
		EntityHistory<Way> mostRecentHistory;
		WayContainer wayContainer;
		
		// Check the first way, if it has a version greater than 1 the way
		// existed prior to the interval beginning and therefore cannot be a
		// create.
		mostRecentHistory = readNextWayHistory();
		createdPreviously = (mostRecentHistory.getVersion() > 1);
		
		while (wayHistoryReader.hasNext() &&
				(wayHistoryReader.peekNext().getEntity().getId() == mostRecentHistory.getEntity().getId())) {
			mostRecentHistory = readNextWayHistory();
		}
		
		// The way in the result must be wrapped in a container.
		wayContainer = new WayContainer(mostRecentHistory.getEntity());
		
		// The entity has been modified if it is visible and was created previously.
		// It is a create if it is visible and was NOT created previously.
		// It is a delete if it is NOT visible and was created previously.
		// No action if it is NOT visible and was NOT created previously.
		if (mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(wayContainer, ChangeAction.Modify);
		} else if (mostRecentHistory.isVisible() && !createdPreviously) {
			return new ChangeContainer(wayContainer, ChangeAction.Create);
		} else if (!mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(wayContainer, ChangeAction.Delete);
		} else {
			return null;
		}
	}
	
	
	/**
	 * Indicates if there is any more data available to be read.
	 * 
	 * @return True if more data is available, false otherwise.
	 */
	public boolean hasNext() {
		while (nextValue == null && wayHistoryReader.hasNext()) {
			nextValue = readChange();
		}
		
		return (nextValue != null);
	}
	
	
	/**
	 * Returns the next available entity and advances to the next record.
	 * 
	 * @return The next available entity.
	 */
	public ChangeContainer next() {
		ChangeContainer result;
		
		if (!hasNext()) {
			throw new OsmosisRuntimeException("No records are available, call hasNext first.");
		}
		
		result = nextValue;
		nextValue = null;
		
		return result;
	}
	
	
	/**
	 * Releases all database resources. This method is guaranteed not to throw
	 * transactions and should always be called in a finally block whenever this
	 * class is used.
	 */
	public void release() {
		nextValue = null;
		
		wayHistoryReader.release();
		waySegmentHistoryReader.release();
		wayTagHistoryReader.release();
	}
}

package com.bretth.osmosis.mysql.impl;

import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.container.ChangeContainer;
import com.bretth.osmosis.container.SegmentContainer;
import com.bretth.osmosis.task.ChangeAction;


/**
 * Reads the set of segment changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class SegmentChangeReader {
	
	private ModifiedSegmentIdReader segmentIdReader;
	private SegmentHistoryReader segmentHistoryReader;
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
	public SegmentChangeReader(String host, String database, String user, String password, Date intervalBegin, Date intervalEnd) {
		segmentIdReader = new ModifiedSegmentIdReader(host, database, user, password, intervalBegin, intervalEnd);
		
		segmentHistoryReader = new SegmentHistoryReader(host, database, user, password);
		segmentHistoryReader.setIntervalEnd(intervalEnd);
	}
	
	
	/**
	 * Reads the history of the specified entity and builds a change object.
	 * 
	 * @param segmentId
	 *            The segment to examine.
	 * @return The change.
	 */
	private ChangeContainer readChange(long segmentId) {
		int recordCount = 0;
		SegmentHistory mostRecentHistory = null;
		SegmentContainer segmentContainer;
		
		segmentHistoryReader.reset();
		segmentHistoryReader.setSegmentId(segmentId);
		
		// Read the entire segment history, we need to know how many records there
		// are and the details of the most recent change.
		while (segmentHistoryReader.hasNext()) {
			SegmentHistory nextHistory = segmentHistoryReader.next();
			
			recordCount++;
			mostRecentHistory = nextHistory;
		}
		
		// We must have at least one record, we shouldn't have identified the
		// segment if no history elements exist.
		if (recordCount <= 0) {
			throw new OsmosisRuntimeException("No history elements exist for segment with id=" + segmentId + ".");
		}
		
		// The segment in the result must be wrapped in a container.
		segmentContainer = new SegmentContainer(mostRecentHistory.getSegment());
		
		// If only one history element exists, it must be a create.
		// Else, if the most recent change leaves it visible it is a modify.
		// Else, it is a delete.
		if (recordCount == 1) {
			// By definition, a create must be visible but we'll double check to be sure.
			if (!mostRecentHistory.isVisible()) {
				throw new OsmosisRuntimeException("Segment with id=" + segmentId + " only has one history element but it is not visible.");
			}
			
			return new ChangeContainer(segmentContainer, ChangeAction.Create);
			
		} else if (mostRecentHistory.isVisible()) {
			return new ChangeContainer(segmentContainer, ChangeAction.Modify);
			
		} else {
			return new ChangeContainer(segmentContainer, ChangeAction.Delete);
		}
	}
	
	
	/**
	 * Indicates if there is any more data available to be read.
	 * 
	 * @return True if more data is available, false otherwise.
	 */
	public boolean hasNext() {
		if (nextValue == null) {
			if (segmentIdReader.hasNext()) {
				long segmentId;
				
				segmentId = segmentIdReader.next().longValue();
				
				nextValue = readChange(segmentId);
			}
		}
		
		return (nextValue != null);
	}
	
	
	/**
	 * Returns the next available entity and advances to the next record.
	 * 
	 * @return The next available entity.
	 */
	public ChangeContainer next() {
		if (!hasNext()) {
			throw new OsmosisRuntimeException("No records are available, call hasNext first.");
		}
		
		return nextValue;
	}
	
	
	/**
	 * Releases all database resources. This method is guaranteed not to throw
	 * transactions and should always be called in a finally block whenever this
	 * class is used.
	 */
	public void release() {
		nextValue = null;
		
		segmentIdReader.release();
		segmentHistoryReader.release();
	}
}

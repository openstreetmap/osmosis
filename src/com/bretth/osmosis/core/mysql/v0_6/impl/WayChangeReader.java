// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.mysql.common.EntityHistory;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Reads the set of way changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class WayChangeReader {
	
	private PeekableIterator<EntityHistory<Way>> wayHistoryReader;
	private PeekableIterator<EntityHistory<DBWayNode>> wayNodeHistoryReader;
	private PeekableIterator<EntityHistory<DBEntityTag>> wayTagHistoryReader;
	private ChangeContainer nextValue;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public WayChangeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin, Date intervalEnd) {
		wayHistoryReader =
			new PeekableIterator<EntityHistory<Way>>(
				new PersistentIterator<EntityHistory<Way>>(
					new SingleClassObjectSerializationFactory(EntityHistory.class),
					new WayHistoryReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd),
					"way",
					true
				)
			);
		wayNodeHistoryReader =
			new PeekableIterator<EntityHistory<DBWayNode>>(
				new PersistentIterator<EntityHistory<DBWayNode>>(
					new SingleClassObjectSerializationFactory(EntityHistory.class),
					new WayNodeHistoryReader(loginCredentials, intervalBegin, intervalEnd),
					"waynod",
					true
				)
			);
		wayTagHistoryReader =
			new PeekableIterator<EntityHistory<DBEntityTag>>(
				new PersistentIterator<EntityHistory<DBEntityTag>>(
					new SingleClassObjectSerializationFactory(EntityHistory.class),
					new EntityTagHistoryReader(loginCredentials, "ways", "way_tags", intervalBegin, intervalEnd),
					"waytag",
					true
				)
			);
	}
	
	
	/**
	 * Consolides the output of all history readers so that ways are fully
	 * populated.
	 * 
	 * @return A way history record where the way is fully populated with nodes
	 *         and tags.
	 */
	private EntityHistory<Way> readNextWayHistory() {
		EntityHistory<Way> wayHistory;
		Way way;
		List<DBWayNode> wayNodes;
		
		wayHistory = wayHistoryReader.next();
		way = wayHistory.getEntity();

		// Add all applicable node references to the way.
		wayNodes = new ArrayList<DBWayNode>();
		while (wayNodeHistoryReader.hasNext() &&
				wayNodeHistoryReader.peekNext().getEntity().getWayId() == way.getId() &&
				wayNodeHistoryReader.peekNext().getVersion() == wayHistory.getVersion()) {
			wayNodes.add(wayNodeHistoryReader.next().getEntity());
		}
		// The underlying query sorts node references by way id but not
		// by their sequence number.
		Collections.sort(wayNodes, new WayNodeComparator());
		for (DBWayNode dbWayNode : wayNodes) {
			way.addWayNode(dbWayNode.getWayNode());
		}
		
		// Add all applicable tags to the way.
		while (wayTagHistoryReader.hasNext() &&
				wayTagHistoryReader.peekNext().getEntity().getEntityId() == way.getId() &&
				wayTagHistoryReader.peekNext().getVersion() == wayHistory.getVersion()) {
			way.addTag(wayTagHistoryReader.next().getEntity().getTag());
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
		wayNodeHistoryReader.release();
		wayTagHistoryReader.release();
	}
}

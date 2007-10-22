package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.ChangeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.mysql.common.EntityHistory;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Reads the set of relation changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class RelationChangeReader {
	
	private PeekableIterator<EntityHistory<Relation>> relationHistoryReader;
	private PeekableIterator<EntityHistory<DBRelationMember>> relationMemberHistoryReader;
	private PeekableIterator<EntityHistory<DBEntityTag>> relationTagHistoryReader;
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
	public RelationChangeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin, Date intervalEnd) {
		relationHistoryReader =
			new PeekableIterator<EntityHistory<Relation>>(
				new PersistentIterator<EntityHistory<Relation>>(
					new RelationHistoryReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd),
					"rel",
					true
				)
			);
		relationMemberHistoryReader =
			new PeekableIterator<EntityHistory<DBRelationMember>>(
				new PersistentIterator<EntityHistory<DBRelationMember>>(
					new RelationMemberHistoryReader(loginCredentials, intervalBegin, intervalEnd),
					"relmbr",
					true
				)
			);
		relationTagHistoryReader =
			new PeekableIterator<EntityHistory<DBEntityTag>>(
				new PersistentIterator<EntityHistory<DBEntityTag>>(
					new EntityTagHistoryReader(loginCredentials, "relations", "relation_tags", intervalBegin, intervalEnd),
					"reltag",
					true
				)
			);
	}
	
	
	/**
	 * Consolidates the output of all history readers so that relations are
	 * fully populated.
	 * 
	 * @return A relation history record where the relation is fully populated
	 *         with members and tags.
	 */
	private EntityHistory<Relation> readNextRelationHistory() {
		EntityHistory<Relation> relationHistory;
		Relation relation;
		
		relationHistory = relationHistoryReader.next();
		relation = relationHistory.getEntity();

		// Add all applicable member references to the relation.
		while (relationMemberHistoryReader.hasNext() &&
				relationMemberHistoryReader.peekNext().getEntity().getRelationId() == relation.getId() &&
				relationMemberHistoryReader.peekNext().getVersion() == relationHistory.getVersion()) {
			relation.addMember(relationMemberHistoryReader.next().getEntity().getRelationMember());
		}
		
		// Add all applicable tags to the relation.
		while (relationTagHistoryReader.hasNext() &&
				relationTagHistoryReader.peekNext().getEntity().getEntityId() == relation.getId() &&
				relationTagHistoryReader.peekNext().getVersion() == relationHistory.getVersion()) {
			relation.addTag(relationTagHistoryReader.next().getEntity().getTag());
		}
		
		return relationHistory;
	}
	
	
	/**
	 * Reads the history of the next entity and builds a change object.
	 */
	private ChangeContainer readChange() {
		boolean createdPreviously;
		EntityHistory<Relation> mostRecentHistory;
		RelationContainer relationContainer;
		
		// Check the first relation, if it has a version greater than 1 the
		// relation existed prior to the interval beginning and therefore cannot
		// be a create.
		mostRecentHistory = readNextRelationHistory();
		createdPreviously = (mostRecentHistory.getVersion() > 1);
		
		while (relationHistoryReader.hasNext() &&
				(relationHistoryReader.peekNext().getEntity().getId() == mostRecentHistory.getEntity().getId())) {
			mostRecentHistory = readNextRelationHistory();
		}
		
		// The relation in the result must be wrapped in a container.
		relationContainer = new RelationContainer(mostRecentHistory.getEntity());
		
		// The entity has been modified if it is visible and was created previously.
		// It is a create if it is visible and was NOT created previously.
		// It is a delete if it is NOT visible and was created previously.
		// No action if it is NOT visible and was NOT created previously.
		if (mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(relationContainer, ChangeAction.Modify);
		} else if (mostRecentHistory.isVisible() && !createdPreviously) {
			return new ChangeContainer(relationContainer, ChangeAction.Create);
		} else if (!mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(relationContainer, ChangeAction.Delete);
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
		while (nextValue == null && relationHistoryReader.hasNext()) {
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
		
		relationHistoryReader.release();
		relationMemberHistoryReader.release();
		relationTagHistoryReader.release();
	}
}

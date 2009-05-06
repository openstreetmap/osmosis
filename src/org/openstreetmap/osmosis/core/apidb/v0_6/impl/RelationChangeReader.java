// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;


/**
 * Reads the set of relation changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class RelationChangeReader {

	private boolean fullHistory;
	private PeekableIterator<EntityHistory<Relation>> relationHistoryReader;
	private PeekableIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>> relationMemberHistoryReader;
	private PeekableIterator<DbFeatureHistory<DbFeature<Tag>>> relationTagHistoryReader;
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
	 * @param fullHistory
	 *            Specifies if full version history should be returned, or just
	 *            a single change per entity for the interval.
	 */
	public RelationChangeReader(
			DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin,
			Date intervalEnd, boolean fullHistory) {
		this.fullHistory = fullHistory;
		
		relationHistoryReader =
			new PeekableIterator<EntityHistory<Relation>>(
				new PersistentIterator<EntityHistory<Relation>>(
					new SingleClassObjectSerializationFactory(EntityHistory.class),
					new RelationHistoryReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd),
					"rel",
					true
				)
			);
		relationMemberHistoryReader =
			new PeekableIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(
				new PersistentIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(
					new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
					new RelationMemberHistoryReader(loginCredentials, intervalBegin, intervalEnd),
					"relmbr",
					true
				)
			);
		relationTagHistoryReader =
			new PeekableIterator<DbFeatureHistory<DbFeature<Tag>>>(
				new PersistentIterator<DbFeatureHistory<DbFeature<Tag>>>(
					new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
					new EntityTagHistoryReader(
							loginCredentials,
							"relations",
							"relation_tags",
							intervalBegin,
							intervalEnd),
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
		List<DbOrderedFeature<RelationMember>> relationMembers;
		
		relationHistory = relationHistoryReader.next();
		relation = relationHistory.getEntity();

		// Add all applicable members to the relation.
		relationMembers = new ArrayList<DbOrderedFeature<RelationMember>>();
		while (relationMemberHistoryReader.hasNext()
				&& relationMemberHistoryReader.peekNext().getDbFeature().getEntityId() == relation.getId()
				&& relationMemberHistoryReader.peekNext().getVersion() == relation.getVersion()) {
			relationMembers.add(relationMemberHistoryReader.next().getDbFeature());
		}
		// The underlying query sorts member references by relation id but not
		// by their sequence number.
		Collections.sort(relationMembers, new DbOrderedFeatureComparator<RelationMember>());
		for (DbOrderedFeature<RelationMember> dbRelationMember : relationMembers) {
			relation.getMembers().add(dbRelationMember.getFeature());
		}
		
		// Add all applicable tags to the relation.
		while (relationTagHistoryReader.hasNext()
				&& relationTagHistoryReader.peekNext().getDbFeature().getEntityId() == relation.getId()
				&& relationTagHistoryReader.peekNext().getVersion() == relation.getVersion()) {
			relation.getTags().add(relationTagHistoryReader.next().getDbFeature().getFeature());
		}
		
		return relationHistory;
	}
	
	
	/**
	 * Reads the history of the next entity and builds a change object.
	 */
	private ChangeContainer readChange() {
		boolean createdPreviously;
		EntityHistory<Relation> mostRecentHistory;
		Relation relation;
		RelationContainer relationContainer;
		
		// Check the first relation, if it has a version greater than 1 the
		// relation existed prior to the interval beginning and therefore cannot
		// be a create.
		mostRecentHistory = readNextRelationHistory();
		relation = mostRecentHistory.getEntity();
		createdPreviously = (relation.getVersion() > 1);

		// Skip over intermediate objects unless full history is required.
		if (!fullHistory) {
			while (relationHistoryReader.hasNext()
					&& (relationHistoryReader.peekNext().getEntity().getId() == relation.getId())) {
				mostRecentHistory = readNextRelationHistory();
				relation = mostRecentHistory.getEntity();
			}
		}
		
		// The relation in the result must be wrapped in a container.
		relationContainer = new RelationContainer(relation);
		
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

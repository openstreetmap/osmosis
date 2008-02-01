// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.LongLongIndexElement;
import com.bretth.osmosis.core.bdb.common.NoSuchDatabaseEntryException;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;


/**
 * Performs all relation-specific db operations.
 * 
 * @author Brett Henderson
 */
public class RelationDao {
	private Transaction txn;
	private Database dbRelation;
	private Database dbNodeRelation;
	private Database dbWayRelation;
	private Database dbChildRelationParentRelation;
	private TupleBinding idBinding;
	private StoreableTupleBinding<Relation> relationBinding;
	private StoreableTupleBinding<LongLongIndexElement> longLongBinding;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The active transaction.
	 * @param dbRelation
	 *            The Relation database.
	 * @param dbNodeRelation
	 *            The node to relation index database.
	 * @param dbWayRelation
	 *            The way to relation index database.
	 * @param dbChildRelationParentRelation
	 *            The child relation to parent relation index database.
	 */
	public RelationDao(Transaction transaction, Database dbRelation, Database dbNodeRelation, Database dbWayRelation, Database dbChildRelationParentRelation) {
		this.txn = transaction;
		this.dbRelation = dbRelation;
		this.dbNodeRelation = dbNodeRelation;
		this.dbWayRelation = dbWayRelation;
		this.dbChildRelationParentRelation = dbChildRelationParentRelation;
		
		idBinding = TupleBinding.getPrimitiveBinding(Long.class);
		relationBinding = new StoreableTupleBinding<Relation>(Relation.class);
		longLongBinding = new StoreableTupleBinding<LongLongIndexElement>(LongLongIndexElement.class);
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
	}
	
	
	/**
	 * Stores the relation in the relation database.
	 * 
	 * @param relation
	 *            The relation to be stored.
	 */
	public void putRelation(Relation relation) {
		// Write the relation object to the relation database.
		idBinding.objectToEntry(relation.getId(), keyEntry);
		relationBinding.objectToEntry(relation, dataEntry);
		
		try {
			dbRelation.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write relation " + relation.getId() + ".", e);
		}
		
		// Write the member to relation index elements for the relation.
		for (RelationMember member : relation.getMemberList()) {
			EntityType memberType;
			
			longLongBinding.objectToEntry(new LongLongIndexElement(member.getMemberId(), relation.getId()), keyEntry);
			dataEntry.setSize(0);
			
			memberType = member.getMemberType();
			
			try {
				if (EntityType.Node.equals(memberType)) {
					dbNodeRelation.put(txn, keyEntry, dataEntry);
				} else if (EntityType.Way.equals(memberType)) {
					dbWayRelation.put(txn, keyEntry, dataEntry);
				} else if (EntityType.Relation.equals(memberType)) {
					dbChildRelationParentRelation.put(txn, keyEntry, dataEntry);
				} else {
					throw new OsmosisRuntimeException("Member type " + memberType + " is not recognised.");
				}
			} catch (DatabaseException e) {
				throw new OsmosisRuntimeException("Unable to write member relation for relation " + relation.getId() + ".", e);
			}
		}
	}
	
	
	/**
	 * Gets the specified relation from the relation database.
	 * 
	 * @param relationId
	 *            The id of the relation to be retrieved.
	 * @return The requested relation.
	 */
	public Relation getRelation(long relationId) {
		idBinding.objectToEntry(relationId, keyEntry);
		
		try {
			if (!OperationStatus.SUCCESS.equals(dbRelation.get(txn, keyEntry, dataEntry, null))) {
				throw new NoSuchDatabaseEntryException("Relation " + relationId + " does not exist in the database.");
			}
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to retrieve relation " + relationId + " from the database.", e);
		}
		
		return (Relation) relationBinding.entryToObject(dataEntry);
	}
	
	
	/**
	 * Provides access to all relations in the database. The iterator must be
	 * released after use.
	 * 
	 * @return An iterator pointing at the first way.
	 */
	public ReleasableIterator<Relation> iterate() {
		return new DatabaseIterator<Relation>(dbRelation, txn, relationBinding);
	}
	
	
	/**
	 * Returns an iterator for the ids of all relations containing the specified
	 * node.
	 * 
	 * @param nodeId
	 *            The id of the node to search on.
	 * @return The ids of the matching relations.
	 */
	public ReleasableIterator<Long> getRelationIdsOwningNode(long nodeId) {
		return new DatabaseRelationIterator(dbNodeRelation, txn, nodeId);
	}
	
	
	/**
	 * Returns an iterator for the ids of all relations containing the specified
	 * way.
	 * 
	 * @param wayId
	 *            The id of the way to search on.
	 * @return The ids of the matching relations.
	 */
	public ReleasableIterator<Long> getRelationIdsOwningWay(long wayId) {
		return new DatabaseRelationIterator(dbWayRelation, txn, wayId);
	}
	
	
	/**
	 * Returns an iterator for the ids of all relations containing the specified
	 * relation.
	 * 
	 * @param relationId
	 *            The id of the relation to search on.
	 * @return The ids of the matching relations.
	 */
	public ReleasableIterator<Long> getRelationIdsOwningRelation(long relationId) {
		return new DatabaseRelationIterator(dbChildRelationParentRelation, txn, relationId);
	}
}

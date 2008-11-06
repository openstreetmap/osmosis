// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads all relations from a database ordered by their identifier. It combines the
 * output of the relation table readers to produce fully configured relation objects.
 * 
 * @author Brett Henderson
 */
public class RelationReader implements ReleasableIterator<EntityHistory<Relation>> {
	
	private ReleasableIterator<EntityHistory<Relation>> relationReader;
	private PeekableIterator<EntityHistory<DBEntityTag>> relationTagReader;
	private PeekableIterator<EntityHistory<DBRelationMember>> relationMemberReader;
	private EntityHistory<Relation> nextValue;
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
	public RelationReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		relationReader = new PersistentIterator<EntityHistory<Relation>>(
			new SingleClassObjectSerializationFactory(EntityHistory.class),
			new RelationTableReader(loginCredentials, readAllUsers),
			"rel",
			true
		);
		relationTagReader = new PeekableIterator<EntityHistory<DBEntityTag>>(
			new PersistentIterator<EntityHistory<DBEntityTag>>(
				new SingleClassObjectSerializationFactory(EntityHistory.class),
				new EntityTagTableReader(loginCredentials, "relation_tags"),
				"reltag",
				true
			)
		);
		relationMemberReader = new PeekableIterator<EntityHistory<DBRelationMember>>(
			new PersistentIterator<EntityHistory<DBRelationMember>>(
				new SingleClassObjectSerializationFactory(EntityHistory.class),
				new RelationMemberTableReader(loginCredentials),
				"relmbr",
				true
			)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && relationReader.hasNext()) {
			EntityHistory<Relation> relationHistory;
			long relationId;
			int relationVersion;
			Relation relation;
			
			relationHistory = relationReader.next();
			
			relation = relationHistory.getEntity();
			relationId = relation.getId();
			relationVersion = relationHistory.getVersion();
			
			// Skip all relation tags that are from lower id or lower version of the same id.
			while (relationTagReader.hasNext()) {
				EntityHistory<DBEntityTag> relationTagHistory;
				DBEntityTag relationTag;
				
				relationTagHistory = relationTagReader.peekNext();
				relationTag = relationTagHistory.getEntity();
				
				if (relationTag.getEntityId() < relationId) {
					relationTagReader.next();
				} else if (relationTag.getEntityId() == relationId) {
					if (relationTagHistory.getVersion() < relationVersion) {
						relationTagReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			// Load all tags matching this version of the relation.
			while (relationTagReader.hasNext() && relationTagReader.peekNext().getEntity().getEntityId() == relationId && relationTagReader.peekNext().getVersion() == relationVersion) {
				relation.addTag(relationTagReader.next().getEntity().getTag());
			}
			
			// Skip all relation members that are from lower id or lower version of the same id.
			while (relationMemberReader.hasNext()) {
				EntityHistory<DBRelationMember> relationMemberHistory;
				DBRelationMember relationMember;
				
				relationMemberHistory = relationMemberReader.peekNext();
				relationMember = relationMemberHistory.getEntity();
				
				if (relationMember.getRelationId() < relationId) {
					relationMemberReader.next();
				} else if (relationMember.getRelationId() == relationId) {
					if (relationMemberHistory.getVersion() < relationVersion) {
						relationMemberReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			// Load all members matching this version of the relation.
			while (relationMemberReader.hasNext() && relationMemberReader.peekNext().getEntity().getRelationId() == relationId && relationMemberReader.peekNext().getVersion() == relationVersion) {
				relation.addMember(relationMemberReader.next().getEntity().getRelationMember());
			}
			
			nextValue = relationHistory;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public EntityHistory<Relation> next() {
		EntityHistory<Relation> result;
		
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
		relationReader.release();
		relationTagReader.release();
		relationMemberReader.release();
	}
}

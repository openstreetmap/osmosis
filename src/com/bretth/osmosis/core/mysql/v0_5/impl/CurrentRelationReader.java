// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads current relations from a database ordered by their identifier. It
 * combines the output of the relation table readers to produce fully configured
 * relation objects.
 * 
 * @author Brett Henderson
 */
public class CurrentRelationReader implements ReleasableIterator<Relation> {
	
	private ReleasableIterator<Relation> relationReader;
	private PeekableIterator<DBEntityTag> relationTagReader;
	private PeekableIterator<DBRelationMember> relationMemberReader;
	private Relation nextValue;
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
	public CurrentRelationReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		relationReader = new PersistentIterator<Relation>(
			new SingleClassObjectSerializationFactory(Relation.class),
			new CurrentRelationTableReader(loginCredentials, readAllUsers),
			"rel",
			true
		);
		relationTagReader = new PeekableIterator<DBEntityTag>(
			new PersistentIterator<DBEntityTag>(
				new SingleClassObjectSerializationFactory(DBEntityTag.class),
				new CurrentEntityTagTableReader(loginCredentials, "current_relation_tags"),
				"reltag",
				true
			)
		);
		relationMemberReader = new PeekableIterator<DBRelationMember>(
			new PersistentIterator<DBRelationMember>(
				new SingleClassObjectSerializationFactory(DBRelationMember.class),
				new CurrentRelationMemberTableReader(loginCredentials),
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
			Relation relation;
			long relationId;
			
			relation = relationReader.next();
			
			relationId = relation.getId();
			
			// Skip all relation tags that are from lower id relation.
			while (relationTagReader.hasNext()) {
				DBEntityTag relationTag;
				
				relationTag = relationTagReader.peekNext();
				
				if (relationTag.getEntityId() < relationId) {
					relationTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags for this relation.
			while (relationTagReader.hasNext() && relationTagReader.peekNext().getEntityId() == relationId) {
				relation.addTag(relationTagReader.next().getTag());
			}
			
			// Skip all relation members that are from lower id or lower version of the same id.
			while (relationMemberReader.hasNext()) {
				DBRelationMember relationMember;
				
				relationMember = relationMemberReader.peekNext();
				
				if (relationMember.getRelationId() < relationId) {
					relationMemberReader.next();
				} else {
					break;
				}
			}
			
			// Load all members matching this relation.
			while (relationMemberReader.hasNext() && relationMemberReader.peekNext().getRelationId() == relationId) {
				relation.addMember(relationMemberReader.next().getRelationMember());
			}
			
			nextValue = relation;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Relation next() {
		Relation result;
		
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

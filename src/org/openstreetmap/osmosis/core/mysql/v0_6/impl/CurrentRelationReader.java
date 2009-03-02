// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads current relations from a database ordered by their identifier. It
 * combines the output of the relation table readers to produce fully configured
 * relation objects.
 * 
 * @author Brett Henderson
 */
public class CurrentRelationReader implements ReleasableIterator<Relation> {
	
	private ReleasableIterator<Relation> relationReader;
	private PeekableIterator<DbFeature<Tag>> relationTagReader;
	private PeekableIterator<DbOrderedFeature<RelationMember>> relationMemberReader;
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
		relationTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new CurrentEntityTagTableReader(loginCredentials, "current_relation_tags"),
				"reltag",
				true
			)
		);
		relationMemberReader = new PeekableIterator<DbOrderedFeature<RelationMember>>(
			new PersistentIterator<DbOrderedFeature<RelationMember>>(
				new SingleClassObjectSerializationFactory(DbOrderedFeature.class),
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
			List<DbOrderedFeature<RelationMember>> relationMembers;
			
			relation = relationReader.next();
			
			relationId = relation.getId();
			
			// Skip all relation tags that are from lower id relation.
			while (relationTagReader.hasNext()) {
				DbFeature<Tag> relationTag;
				
				relationTag = relationTagReader.peekNext();
				
				if (relationTag.getEntityId() < relationId) {
					relationTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags for this relation.
			while (relationTagReader.hasNext() && relationTagReader.peekNext().getEntityId() == relationId) {
				relation.getTags().add(relationTagReader.next().getFeature());
			}
			
			// Skip all relation members that are from lower id relation.
			while (relationMemberReader.hasNext()) {
				DbFeature<RelationMember> relationMember;
				
				relationMember = relationMemberReader.peekNext();
				
				if (relationMember.getEntityId() < relationId) {
					relationMemberReader.next();
				} else {
					break;
				}
			}
			
			// Load all members matching this relation.
			relationMembers = new ArrayList<DbOrderedFeature<RelationMember>>();
			while (relationMemberReader.hasNext() && relationMemberReader.peekNext().getEntityId() == relationId) {
				relationMembers.add(relationMemberReader.next());
			}
			// The underlying query sorts member references by relation id but not
			// by their sequence number.
			Collections.sort(relationMembers, new DbOrderedFeatureComparator<RelationMember>());
			for (DbOrderedFeature<RelationMember> dbRelationMember : relationMembers) {
				relation.getMembers().add(dbRelationMember.getFeature());
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

// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads all relations from a database ordered by their identifier. It combines the
 * output of the relation table readers to produce fully configured relation objects.
 * 
 * @author Brett Henderson
 */
public class RelationReader implements ReleasableIterator<EntityHistory<RelationBuilder>> {
	
	private ReleasableIterator<EntityHistory<RelationBuilder>> relationReader;
	private PeekableIterator<DbFeatureHistory<DbFeature<Tag>>> relationTagReader;
	private PeekableIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>> relationMemberReader;
	private EntityHistory<RelationBuilder> nextValue;
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
		relationReader = new PersistentIterator<EntityHistory<RelationBuilder>>(
			new SingleClassObjectSerializationFactory(EntityHistory.class),
			new RelationTableReader(loginCredentials, readAllUsers),
			"rel",
			true
		);
		relationTagReader = new PeekableIterator<DbFeatureHistory<DbFeature<Tag>>>(
			new PersistentIterator<DbFeatureHistory<DbFeature<Tag>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new EntityTagTableReader(loginCredentials, "relation_tags"),
				"reltag",
				true
			)
		);
		relationMemberReader = new PeekableIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(
			new PersistentIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
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
			EntityHistory<RelationBuilder> relationHistory;
			long relationId;
			int relationVersion;
			RelationBuilder relation;
			List<DbOrderedFeature<RelationMember>> relationMembers;
			
			relationHistory = relationReader.next();
			
			relation = relationHistory.getEntity();
			relationId = relation.getId();
			relationVersion = relation.getVersion();
			
			// Skip all relation tags that are from lower id or lower version of the same id.
			while (relationTagReader.hasNext()) {
				DbFeatureHistory<DbFeature<Tag>> relationTagHistory;
				DbFeature<Tag> relationTag;
				
				relationTagHistory = relationTagReader.peekNext();
				relationTag = relationTagHistory.getDbFeature();
				
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
			while (relationTagReader.hasNext() && relationTagReader.peekNext().getDbFeature().getEntityId() == relationId && relationTagReader.peekNext().getVersion() == relationVersion) {
				relation.addTag(relationTagReader.next().getDbFeature().getFeature());
			}
			
			// Skip all relation members that are from lower id or lower version of the same id.
			while (relationMemberReader.hasNext()) {
				DbFeatureHistory<DbOrderedFeature<RelationMember>> relationMemberHistory;
				DbFeature<RelationMember> relationMember;
				
				relationMemberHistory = relationMemberReader.peekNext();
				relationMember = relationMemberHistory.getDbFeature();
				
				if (relationMember.getEntityId() < relationId) {
					relationMemberReader.next();
				} else if (relationMember.getEntityId() == relationId) {
					if (relationMemberHistory.getVersion() < relationVersion) {
						relationMemberReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}

			// Load all nodes matching this version of the way.
			relationMembers = new ArrayList<DbOrderedFeature<RelationMember>>();
			while (relationMemberReader.hasNext() && relationMemberReader.peekNext().getDbFeature().getEntityId() == relationId && relationMemberReader.peekNext().getVersion() == relationVersion) {
				relationMembers.add(relationMemberReader.next().getDbFeature());
			}
			// The underlying query sorts node references by way id but not
			// by their sequence number.
			Collections.sort(relationMembers, new DbOrderedFeatureComparator<RelationMember>());
			for (DbOrderedFeature<RelationMember> dbRelationMember : relationMembers) {
				relation.addMember(dbRelationMember.getFeature());
			}
			
			nextValue = relationHistory;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public EntityHistory<RelationBuilder> next() {
		EntityHistory<RelationBuilder> result;
		
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

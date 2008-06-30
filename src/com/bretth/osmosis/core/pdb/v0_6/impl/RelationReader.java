// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_6.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityTag;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBRelationMember;
import com.bretth.osmosis.core.pdb.common.DatabaseContext;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads all relations from a database ordered by their identifier. It combines the
 * output of the relation table readers to produce fully configured relation objects.
 * 
 * @author Brett Henderson
 */
public class RelationReader implements ReleasableIterator<Relation> {
	
	private ReleasableIterator<Relation> relationReader;
	private PeekableIterator<DBEntityTag> relationTagReader;
	private PeekableIterator<DBRelationMember> relationMemberReader;
	private Relation nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public RelationReader(DatabaseContext dbCtx) {
		relationReader = new PersistentIterator<Relation>(
			new SingleClassObjectSerializationFactory(Relation.class),
			new RelationTableReader(dbCtx),
			"rel",
			true
		);
		relationTagReader = new PeekableIterator<DBEntityTag>(
			new PersistentIterator<DBEntityTag>(
				new SingleClassObjectSerializationFactory(DBEntityTag.class),
				new EntityTagTableReader(dbCtx, "relation_tags", "relation_id"),
				"reltag",
				true
			)
		);
		relationMemberReader = new PeekableIterator<DBRelationMember>(
			new RelationMemberTableReader(dbCtx)
		);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public RelationReader(DatabaseContext dbCtx, String constraintTable) {
		relationReader = new PersistentIterator<Relation>(
			new SingleClassObjectSerializationFactory(Relation.class),
			new RelationTableReader(dbCtx, constraintTable),
			"rel",
			true
		);
		relationTagReader = new PeekableIterator<DBEntityTag>(
			new PersistentIterator<DBEntityTag>(
				new SingleClassObjectSerializationFactory(DBEntityTag.class),
				new EntityTagTableReader(dbCtx, "relation_tags", "relation_id", constraintTable),
				"reltag",
				true
			)
		);
		relationMemberReader = new PeekableIterator<DBRelationMember>(
			new RelationMemberTableReader(dbCtx, constraintTable)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && relationReader.hasNext()) {
			long relationId;
			Relation relation;
			
			relation = relationReader.next();
			relationId = relation.getId();
			
			// Skip all relation tags that are from a lower relation.
			while (relationTagReader.hasNext()) {
				DBEntityTag relationTag;
				
				relationTag = relationTagReader.peekNext();
				
				if (relationTag.getEntityId() < relationId) {
					relationTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags matching this version of the relation.
			while (relationTagReader.hasNext() && relationTagReader.peekNext().getEntityId() == relationId) {
				relation.addTag(relationTagReader.next().getTag());
			}
			
			// Skip all relation nodes that are from a lower relation.
			while (relationMemberReader.hasNext()) {
				DBRelationMember relationNode;
				
				relationNode = relationMemberReader.peekNext();
				
				if (relationNode.getRelationId() < relationId) {
					relationMemberReader.next();
				} else {
					break;
				}
			}
			
			// Load all members matching this version of the relation.
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

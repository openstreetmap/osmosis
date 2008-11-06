// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.domain.v0_6.Entity;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.mysql.v0_6.impl.DbFeature;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads instances of an entity type from a database ordered by the identifier.
 * It combines the output of the entity feature table readers to produce fully
 * configured entity objects.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public class EntityReader<T extends Entity> implements ReleasableIterator<T> {
	
	private ReleasableIterator<T> entityReader;
	private PeekableIterator<DbFeature<Tag>> entityTagReader;
	private T nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param entityBuilder
	 *            The database mapper for the entity type.
	 */
	public EntityReader(DatabaseContext dbCtx, EntityBuilder<T> entityBuilder) {
		// The postgres jdbc driver doesn't appear to allow concurrent result
		// sets on the same connection so only the last opened result set may be
		// streamed. The rest of the result sets must be persisted first.
		entityReader = new PersistentIterator<T>(
			new SingleClassObjectSerializationFactory(entityBuilder.getEntityClass()),
			new EntityTableReader<T>(dbCtx, entityBuilder),
			"ent",
			true
		);
		entityTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new EntityFeatureTableReader<Tag, DbFeature<Tag>>(dbCtx, new TagBuilder(entityBuilder.getEntityName())),
				"enttag",
				true
			)
		);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param entityBuilder
	 *            The database mapper for the entity type.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public EntityReader(DatabaseContext dbCtx, EntityBuilder<T> entityBuilder, String constraintTable) {
		// The postgres jdbc driver doesn't appear to allow concurrent result
		// sets on the same connection so only the last opened result set may be
		// streamed. The rest of the result sets must be persisted first.
		entityReader = new PersistentIterator<T>(
			new SingleClassObjectSerializationFactory(entityBuilder.getEntityClass()),
			new EntityTableReader<T>(dbCtx, entityBuilder, constraintTable),
			"nod",
			true
		);
		entityTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new EntityFeatureTableReader<Tag, DbFeature<Tag>>(dbCtx, new TagBuilder(entityBuilder.getEntityName()), constraintTable),
				"enttag",
				true
			)
		);
	}
	
	
	/**
	 * Populates the entity with the its features. These features will be read
	 * from related tables.
	 * 
	 * @param entity
	 *            The entity to be populated.
	 */
	protected void populateEntityFeatures(T entity) {
		long entityId;
		
		entityId = entity.getId();
		
		// Skip all tags that are from a lower entity.
		while (entityTagReader.hasNext()) {
			DbFeature<Tag> dbTag;
			
			dbTag = entityTagReader.peekNext();
			
			if (dbTag.getEntityId() < entityId) {
				entityTagReader.next();
			} else {
				break;
			}
		}
		
		// Load all tags matching this version of the node.
		while (entityTagReader.hasNext() && entityTagReader.peekNext().getEntityId() == entityId) {
			entity.addTag(entityTagReader.next().getFeature());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && entityReader.hasNext()) {
			T entity;
			
			entity = entityReader.next();
			
			populateEntityFeatures(entity);
			
			nextValue = entity;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public T next() {
		T result;
		
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
		entityReader.release();
		entityTagReader.release();
	}
}

// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


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
	 * @param entityMapper
	 *            The database mapper for the entity type.
	 */
	public EntityReader(DatabaseContext dbCtx, EntityMapper<T> entityMapper) {
		// The postgres jdbc driver doesn't appear to allow concurrent result
		// sets on the same connection so only the last opened result set may be
		// streamed. The rest of the result sets must be persisted first.
		entityReader = new PersistentIterator<T>(
			new SingleClassObjectSerializationFactory(entityMapper.getEntityClass()),
			new EntityTableReader<T>(dbCtx, entityMapper),
			"ent",
			true
		);
		entityTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new EntityFeatureTableReader<Tag, DbFeature<Tag>>(dbCtx, new TagMapper(entityMapper.getEntityName())),
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
	 * @param entityMapper
	 *            The database mapper for the entity type.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public EntityReader(DatabaseContext dbCtx, EntityMapper<T> entityMapper, String constraintTable) {
		// The postgres jdbc driver doesn't appear to allow concurrent result
		// sets on the same connection so only the last opened result set may be
		// streamed. The rest of the result sets must be persisted first.
		entityReader = new PersistentIterator<T>(
			new SingleClassObjectSerializationFactory(entityMapper.getEntityClass()),
			new EntityTableReader<T>(dbCtx, entityMapper, constraintTable),
			"nod",
			true
		);
		entityTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new EntityFeatureTableReader<Tag, DbFeature<Tag>>(
						dbCtx, new TagMapper(entityMapper.getEntityName()), constraintTable),
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
			entity.getTags().add(entityTagReader.next().getFeature());
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

// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.util.NoSuchElementException;

import com.bretth.osmosis.core.domain.v0_6.Entity;
import com.bretth.osmosis.core.domain.v0_6.EntityBuilder;
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
 * @param <Te>
 *            The entity type to be supported.
 * @param <Tb>
 *            The builder type for the entity.
 */
public class EntityReader<Te extends Entity,  Tb extends EntityBuilder<Te>> implements ReleasableIterator<Te> {
	
	private ReleasableIterator<Tb> entityReader;
	private PeekableIterator<DbFeature<Tag>> entityTagReader;
	private Te nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param entityMapper
	 *            The database mapper for the entity type.
	 */
	public EntityReader(DatabaseContext dbCtx, EntityMapper<Te, Tb> entityMapper) {
		// The postgres jdbc driver doesn't appear to allow concurrent result
		// sets on the same connection so only the last opened result set may be
		// streamed. The rest of the result sets must be persisted first.
		entityReader = new PersistentIterator<Tb>(
			new SingleClassObjectSerializationFactory(entityMapper.getBuilderClass()),
			new EntityTableReader<Te, Tb>(dbCtx, entityMapper),
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
	public EntityReader(DatabaseContext dbCtx, EntityMapper<Te, Tb> entityMapper, String constraintTable) {
		// The postgres jdbc driver doesn't appear to allow concurrent result
		// sets on the same connection so only the last opened result set may be
		// streamed. The rest of the result sets must be persisted first.
		entityReader = new PersistentIterator<Tb>(
			new SingleClassObjectSerializationFactory(entityMapper.getBuilderClass()),
			new EntityTableReader<Te, Tb>(dbCtx, entityMapper, constraintTable),
			"nod",
			true
		);
		entityTagReader = new PeekableIterator<DbFeature<Tag>>(
			new PersistentIterator<DbFeature<Tag>>(
				new SingleClassObjectSerializationFactory(DbFeature.class),
				new EntityFeatureTableReader<Tag, DbFeature<Tag>>(dbCtx, new TagMapper(entityMapper.getEntityName()), constraintTable),
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
	protected void populateEntityFeatures(Tb entity) {
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
			Tb entityBuilder;
			
			entityBuilder = entityReader.next();
			
			populateEntityFeatures(entityBuilder);
			
			nextValue = entityBuilder.buildEntity();
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Te next() {
		Te result;
		
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

package com.bretth.osmosis.core.migrate.impl;

import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;


/**
 * Provides a conversion between 0.5 and 0.6 entity containers.
 *  
 * @author Brett Henderson
 */
public class EntityContainerMigrater implements EntityProcessor {
	private EntityMigrater migrater;
	private ThreadLocal<EntityContainer> resultContainer;
	
	
	public EntityContainerMigrater() {
		resultContainer = new ThreadLocal<EntityContainer>();
	}
	
	
	/**
	 * Migrates an entity container from 0.5 to 0.6 format.
	 * 
	 * @param entityContainer
	 *            The entity container to migrate.
	 * @return The entity container in 0.6 format.
	 */
	public EntityContainer migrate(com.bretth.osmosis.core.container.v0_5.EntityContainer entityContainer) {
		entityContainer.process(this);
		
		return resultContainer.get();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(com.bretth.osmosis.core.container.v0_5.BoundContainer entityContainer) {
		resultContainer.set(
			new BoundContainer(migrater.migrate(entityContainer.getEntity()))
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(com.bretth.osmosis.core.container.v0_5.NodeContainer entityContainer) {
		resultContainer.set(
			new NodeContainer(migrater.migrate(entityContainer.getEntity()))
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(com.bretth.osmosis.core.container.v0_5.WayContainer entityContainer) {
		resultContainer.set(
			new WayContainer(migrater.migrate(entityContainer.getEntity()))
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(com.bretth.osmosis.core.container.v0_5.RelationContainer entityContainer) {
		resultContainer.set(
			new RelationContainer(migrater.migrate(entityContainer.getEntity()))
		);
	}
}

package com.bretth.osmosis.core.container;

import com.bretth.osmosis.core.task.ChangeAction;


/**
 * Holds an EntityContainer and an associated action.
 * 
 * @author Brett Henderson
 */
public class ChangeContainer {
	private EntityContainer entityContainer;
	private ChangeAction action;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityContainer
	 *            The entity to store.
	 * @param action
	 *            The action to store.
	 */
	public ChangeContainer(EntityContainer entityContainer, ChangeAction action) {
		this.entityContainer = entityContainer;
		this.action = action;
	}
	
	
	/**
	 * Returns the contained entity.
	 * 
	 * @return The entity.
	 */
	public EntityContainer getEntityContainer() {
		return entityContainer;
	}
	
	
	/**
	 * Returns the contained action.
	 * 
	 * @return The action.
	 */
	public ChangeAction getAction() {
		return action;
	}
}

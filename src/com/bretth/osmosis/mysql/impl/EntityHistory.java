package com.bretth.osmosis.mysql.impl;


/**
 * A data class representing a node history record.
 * 
 * @author Brett Henderson
 */

/**
 * A data class representing a history record.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity that this class stores history for.
 */
public class EntityHistory<T> {
	
	private T entity;
	private int version;
	private boolean visible;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entity
	 *            The contained entity.
	 * @param version
	 *            The version of the history element.
	 * @param visible
	 *            The visible field.
	 */
	public EntityHistory(T entity, int version, boolean visible) {
		this.entity = entity;
		this.version = version;
		this.visible = visible;
	}
	
	
	/**
	 * Gets the contained entity.
	 * 
	 * @return The entity.
	 */
	public T getEntity() {
		return entity;
	}
	
	
	/**
	 * Gets the version.
	 * 
	 * @return The version.
	 */
	public int getVersion() {
		return version;
	}
	
	
	/**
	 * Gets the visible flag.
	 * 
	 * @return The visible flag.
	 */
	public boolean isVisible() {
		return visible;
	}
}

// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.common;


/**
 * Represents all the actions that can be performed on a data type when
 * performing a change.
 * 
 * @author Brett Henderson
 */
public enum ChangeAction {
	/**
	 * Represents the creation of a new entity.
	 */
	Create,
	
	/**
	 * Represents the modification of an existing entity.
	 */
	Modify,
	
	/**
	 * Represents the deletion of an existing entity.
	 */
	Delete
}

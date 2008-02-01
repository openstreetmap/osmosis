// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.common;


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

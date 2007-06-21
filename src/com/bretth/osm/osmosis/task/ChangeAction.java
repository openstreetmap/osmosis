package com.bretth.osm.osmosis.task;


/**
 * Represents all the actions that can be performed on a data type when
 * performing a change.
 * 
 * @author Brett Henderson
 */
public enum ChangeAction {
	/**
	 * Represents the creation of a new data element.
	 */
	Create,
	
	/**
	 * Represents the modification of an existing data element.
	 */
	Modify,
	
	/**
	 * Represents the deletion of an existing data element.
	 */
	Delete
}

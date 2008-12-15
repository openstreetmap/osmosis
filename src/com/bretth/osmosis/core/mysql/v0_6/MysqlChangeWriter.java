// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.mysql.v0_6.impl.SchemaVersionValidator;
import com.bretth.osmosis.core.mysql.v0_6.impl.ActionChangeWriter;
import com.bretth.osmosis.core.mysql.v0_6.impl.ChangeWriter;
import com.bretth.osmosis.core.task.common.ChangeAction;
import com.bretth.osmosis.core.task.v0_6.ChangeSink;


/**
 * A change sink writing to database tables. This aims to be suitable for
 * running at regular intervals with database overhead proportional to changeset
 * size.
 * 
 * @author Brett Henderson
 */
public class MysqlChangeWriter implements ChangeSink {
	
	private ChangeWriter changeWriter;
	private Map<ChangeAction, ActionChangeWriter> actionWriterMap;
	private SchemaVersionValidator schemaVersionValidator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param populateCurrentTables
	 *            If true, the current tables will be populated as well as
	 *            history tables.
	 */
	public MysqlChangeWriter(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, boolean populateCurrentTables) {
		changeWriter = new ChangeWriter(loginCredentials, populateCurrentTables);
		actionWriterMap = new HashMap<ChangeAction, ActionChangeWriter>();
		actionWriterMap.put(ChangeAction.Create, new ActionChangeWriter(changeWriter, ChangeAction.Create));
		actionWriterMap.put(ChangeAction.Modify, new ActionChangeWriter(changeWriter, ChangeAction.Modify));
		actionWriterMap.put(ChangeAction.Delete, new ActionChangeWriter(changeWriter, ChangeAction.Delete));
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials, preferences);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
		ChangeAction action;
		
		// Verify that the schema version is supported.
		schemaVersionValidator.validateVersion(MySqlVersionConstants.SCHEMA_MIGRATIONS);
		
		action = change.getAction();
		
		if (!actionWriterMap.containsKey(action)) {
			throw new OsmosisRuntimeException("The action " + action + " is unrecognized.");
		}
		
		// Process the entity using the action writer appropriate for the change
		// action.
		change.getEntityContainer().process(actionWriterMap.get(action));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		changeWriter.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		changeWriter.release();
	}
}

package com.bretth.osmosis.core.mysql.v0_5;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.ChangeContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.mysql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.mysql.v0_5.impl.ActionChangeWriter;
import com.bretth.osmosis.core.mysql.v0_5.impl.ChangeWriter;
import com.bretth.osmosis.core.task.common.ChangeAction;
import com.bretth.osmosis.core.task.v0_5.ChangeSink;


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
	private DatabasePreferences preferences;
	private SchemaVersionValidator schemaVersionValidator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public MysqlChangeWriter(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.preferences = preferences;
		
		changeWriter = new ChangeWriter(loginCredentials);
		actionWriterMap = new HashMap<ChangeAction, ActionChangeWriter>();
		actionWriterMap.put(ChangeAction.Create, new ActionChangeWriter(changeWriter, ChangeAction.Create));
		actionWriterMap.put(ChangeAction.Modify, new ActionChangeWriter(changeWriter, ChangeAction.Modify));
		actionWriterMap.put(ChangeAction.Delete, new ActionChangeWriter(changeWriter, ChangeAction.Delete));
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
		ChangeAction action;
		
		// Verify that the schema version is supported.
		if (preferences.getValidateSchemaVersion()) {
			schemaVersionValidator.validateVersion(MySqlVersionConstants.SCHEMA_VERSION);
		}
		
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

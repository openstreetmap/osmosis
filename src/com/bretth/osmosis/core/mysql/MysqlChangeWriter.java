package com.bretth.osmosis.core.mysql;

import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.container.v0_4.ChangeContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.mysql.impl.ActionChangeWriter;
import com.bretth.osmosis.core.mysql.impl.ChangeWriter;
import com.bretth.osmosis.core.task.common.ChangeAction;
import com.bretth.osmosis.core.task.v0_4.ChangeSink;


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
	
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 */
	public MysqlChangeWriter(String host, String database, String user, String password) {
		changeWriter = new ChangeWriter(host, database, user, password);
		actionWriterMap = new HashMap<ChangeAction, ActionChangeWriter>();
		actionWriterMap.put(ChangeAction.Create, new ActionChangeWriter(changeWriter, ChangeAction.Create));
		actionWriterMap.put(ChangeAction.Modify, new ActionChangeWriter(changeWriter, ChangeAction.Modify));
		actionWriterMap.put(ChangeAction.Delete, new ActionChangeWriter(changeWriter, ChangeAction.Delete));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
		ChangeAction action;
		
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

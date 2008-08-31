// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.database;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.store.Releasable;


/**
 * A container for database statement objects that must be freed. This
 * implementation simplifies the management of many statements that must be
 * released as a group.
 * 
 * @author Brett Henderson
 */
public class ReleasableStatementContainer implements Releasable {
	private List<Statement> objects;
	
	
	/**
	 * Creates a new instance.
	 */
	public ReleasableStatementContainer() {
		objects = new ArrayList<Statement>();
	}
	
	
	/**
	 * Adds a new object to be managed. The object is returned to allow method
	 * chaining.
	 * 
	 * @param <T>
	 *            The type of statement being stored.
	 * @param statement
	 *            The statement to be stored.
	 * @return The statement that was stored.
	 */
	public <T extends Statement> T add(T statement) {
		objects.add(statement);
		
		return statement;
	}
	
	
	/**
	 * Removes all objects. They will no longer be released.
	 */
	public void clear() {
		objects.clear();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (Statement statement : objects) {
			try {
				statement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
		}
	}
}

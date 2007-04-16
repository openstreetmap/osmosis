package com.bretth.osm.conduit.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osm.conduit.ConduitRuntimeException;


public abstract class EntityReader<E> {
	
	private DatabaseContext dbCtx;
	private ResultSet resultSet;
	private E nextValue;
	
	
	public EntityReader(String host, String database, String user, String password) {
		dbCtx = new DatabaseContext(host, database, user, password);
	}
	
	
	protected abstract String getQuerySql();
	
	
	protected abstract E createNextValue(ResultSet resultSet);
	
	
	private void readNextValue() {
		if (resultSet == null) {
			resultSet = dbCtx.executeStreamingQuery(getQuerySql());
		}
		
		try {
			if (resultSet.next()) {
				nextValue = createNextValue(resultSet);
			} else {
				nextValue = null;
			}
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to move to next record.", e);
		}
	}
	
	
	public boolean hasNext() {
		if (resultSet == null) {
			readNextValue();
		}
		
		return (nextValue != null);
	}
	
	
	public E peekNext() {
		if (resultSet == null) {
			throw new ConduitRuntimeException("hasNext must be called first.");
		}
		if (nextValue == null) {
			throw new ConduitRuntimeException("No value is available.");
		}
		
		return nextValue;
	}
	
	
	public E next() {
		E result;
		
		result = peekNext();
		
		readNextValue();
		
		return result;
	}
	
	
	public void release() {
		nextValue = null;
		resultSet = null;
		
		dbCtx.release();
	}
}

package com.bretth.osm.conduit.mysql.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osm.conduit.pipeline.PipelineRuntimeException;


public abstract class EntityReader<E> {
	
	private DatabaseContext dbCtx;
	private ResultSet resultSet;
	private E nextValue;
	
	
	public EntityReader() {
		dbCtx = new DatabaseContext();
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
			throw new PipelineRuntimeException("Unable to move to next record.", e);
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
			throw new PipelineRuntimeException("hasNext must be called first.");
		}
		if (nextValue == null) {
			throw new PipelineRuntimeException("No value is available.");
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

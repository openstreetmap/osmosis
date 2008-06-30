// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_5;

import com.bretth.osmosis.core.container.v0_5.Dataset;
import com.bretth.osmosis.core.container.v0_5.DatasetReader;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.pgsql.v0_5.impl.PostgreSqlDatasetReader;
import com.bretth.osmosis.core.task.v0_5.DatasetSink;
import com.bretth.osmosis.core.task.v0_5.RunnableDatasetSource;


/**
 * An OSM dataset source exposing read-only access to a custom PostgreSQL database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlReader implements RunnableDatasetSource, Dataset {
	private DatasetSink datasetSink;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDatasetSink(DatasetSink datasetSink) {
		this.datasetSink = datasetSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			datasetSink.process(this);
			
		} finally {
			datasetSink.release();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DatasetReader createReader() {
		return new PostgreSqlDatasetReader(loginCredentials, preferences);
	}
}

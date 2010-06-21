// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.common;

import org.apache.commons.dbcp.BasicDataSource;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;


/**
 * Produces data sources based on a set of database credentials.
 */
public final class DataSourceFactory {
	
	/**
	 * This class cannot be instantiated.
	 */
	private DataSourceFactory() {
	}
	
	
	/**
	 * Creates a new data source based on the specified credentials.
	 * 
	 * @param credentials
	 *            The database credentials.
	 * 
	 * @return The data source.
	 */
	public static BasicDataSource createDataSource(DatabaseLoginCredentials credentials) {
		BasicDataSource dataSource;
		
		dataSource = new BasicDataSource();
		
        switch (credentials.getDbType()) {
        case POSTGRESQL:
        	dataSource.setDriverClassName("org.postgresql.Driver");
        	dataSource.setUrl("jdbc:postgresql://" + credentials.getHost() + "/" + credentials.getDatabase()
        			/*+ "?loglevel=2"*/);
        	break;
        case MYSQL:
        	dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        	dataSource.setUrl("jdbc:mysql://" + credentials.getHost() + "/" + credentials.getDatabase());
            break;
        default:
            throw new OsmosisRuntimeException("Unknown database type " + credentials.getDbType() + ".");
        }
        
        dataSource.setUsername(credentials.getUser());
        dataSource.setPassword(credentials.getPassword());
        
        return dataSource;
	}
}

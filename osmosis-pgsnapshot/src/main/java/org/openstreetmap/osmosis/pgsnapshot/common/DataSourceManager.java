// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Creates or obtains a datasource and manages its lifecycle based on the login
 * credentials provided.
 * 
 * @author Brett Henderson
 */
public final class DataSourceManager implements Releasable {
	
	private static final Logger LOG = Logger.getLogger(DataSourceManager.class.getName());
	
	private DataSource dataSource;
	private BasicDataSource localDataSource;
	private DatabaseLoginCredentials credentials;


	/**
	 * Creates a new instance.
	 * 
	 * @param credentials
	 *            Contains all information required to connect to the database.
	 */
	public DataSourceManager(DatabaseLoginCredentials credentials) {
		this.credentials = credentials;
	}
	
	
	private void createDataSource() {
		localDataSource = new BasicDataSource();
		
		localDataSource.setDriverClassName("org.postgresql.Driver");
		localDataSource.setUrl("jdbc:postgresql://" + credentials.getHost() + "/" + credentials.getDatabase()
    			/*+ "?loglevel=2"*/);
        
		localDataSource.setUsername(credentials.getUser());
		localDataSource.setPassword(credentials.getPassword());
        
        dataSource = localDataSource;
	}
	
	
	private void loadDatasource() {
		InitialContext cxt;
		String jndiLocation;
		
		jndiLocation = credentials.getDatasourceJndiLocation();
		
		try {
			cxt = new InitialContext();
		} catch (NamingException e) {
			throw new OsmosisRuntimeException("Unable to create an initial JNDI context.", e);
		}
		
		try {
			dataSource = (DataSource) cxt.lookup(jndiLocation);
		} catch (NamingException e) {
			throw new OsmosisRuntimeException("Unable to locate the datasource (" + jndiLocation + ")", e);
		}
	}
	
	
	private Connection createConnectionFromDriverManager() {
		try {
			// Register the database driver.
			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				throw new OsmosisRuntimeException("Unable to find database driver.", e);
			}
			
			return DriverManager.getConnection(
				"jdbc:postgresql://" + credentials.getHost() + "/"
				+ credentials.getDatabase(),
		    	// + "?logLevel=2"
				credentials.getUser(),
				credentials.getPassword()
		    );
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to establish a new database connection.", e);
		}
	}
	
	
	/**
	 * Obtains a data source.
	 * 
	 * @return The database source.
	 */
	public DataSource getDataSource() {
		if (dataSource == null) {
			String jndiLocation;
			
			jndiLocation = credentials.getDatasourceJndiLocation();
			
			if (jndiLocation != null) {
				LOG.finer("Retrieving a data source from JNDI.");
				
				loadDatasource();
				
			} else {
				LOG.finer("Creating a new locally managed data source.");
				
				createDataSource();
			}
		}
		
		return dataSource;
	}
	
	
	/**
	 * Obtains a single connection.
	 * 
	 * @return The connection.
	 */
	public Connection getConnection() {
		String jndiLocation;
		Connection connection;
		
		jndiLocation = credentials.getDatasourceJndiLocation();
		
		if (dataSource == null && jndiLocation != null) {
			LOG.finer("Retrieving a data source from JNDI.");
			loadDatasource();
		}
		
		if (dataSource == null) {
			LOG.finer("Creating a new database connection from DriverManager.");
			connection = createConnectionFromDriverManager();
		} else {
			try {
				connection = dataSource.getConnection();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to obtain a connection from the datasource.", e);
			}
		}
		
		return connection;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (localDataSource != null) {
			try {
				localDataSource.close();
			} catch (SQLException e) {
				LOG.log(Level.WARNING, "Unable to cleanup the database connection pool.", e);
			}
			
			localDataSource = null;
			dataSource = null;
		}
	}
}

// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.EnvironmentContext;
import com.bretth.osmosis.core.store.Completable;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;


/**
 * Maintains the state associated with a single transaction interaction with a
 * dataset schema allowing operations to be performed on the schema.
 * 
 * @author Brett Henderson
 */
public class TransactionContext implements Completable {
	private static Logger log = Logger.getLogger(TransactionContext.class.getName());
	
	private static final String DB_NODE = "node";
	private static final String DB_WAY = "way";
	private static final String DB_RELATION = "relation";
	private static final String DB_TILE_NODE = "tile_node";
	private static final String[] DB_TILE_WAY = {"tile_way_0", "tile_way_4", "tile_way_8", "tile_way_16", "tile_way_24", "tile_way_32"};
	private static final String DB_NODE_RELATION = "node_relation";
	private static final String DB_WAY_RELATION = "way_relation";
	private static final String DB_CHILD_RELATION_PARENT_RELATION = "child_relation_parent_relation";
	
	private EnvironmentContext envCtx;
	private boolean initialized;
	private Transaction txn;
	private Database dbNode;
	private Database dbWay;
	private Database dbRelation;
	private Database dbTileNode;
	private Database dbTileWay[];
	private Database dbNodeRelation;
	private Database dbWayRelation;
	private Database dbChildRelationParentRelation;
	private boolean committed;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param environmentContext
	 *            The database environment to utilise for all operations.
	 */
	public TransactionContext(EnvironmentContext environmentContext) {
		this.envCtx = environmentContext;
		
		initialized = false;
		committed = false;
	}
	
	
	/**
	 * Ensures all databases are opened and ready for use.
	 */
	private void initialize() {
		if (dbNode == null) {
			dbNode = envCtx.openDatabase(DB_NODE);
		}
		if (dbWay == null) {
			dbWay = envCtx.openDatabase(DB_WAY);
		}
		if (dbRelation == null) {
			dbRelation = envCtx.openDatabase(DB_RELATION);
		}
		if (dbTileNode == null) {
			dbTileNode = envCtx.openDatabase(DB_TILE_NODE);
		}
		if (dbTileWay == null) {
			dbTileWay = new Database[DB_TILE_WAY.length];
			
			for (int i = 0; i < DB_TILE_WAY.length; i++) {
				dbTileWay[i] = envCtx.openDatabase(DB_TILE_WAY[i]);
			}
		}
		if (dbNodeRelation == null) {
			dbNodeRelation = envCtx.openDatabase(DB_NODE_RELATION);
		}
		if (dbWayRelation == null) {
			dbWayRelation = envCtx.openDatabase(DB_WAY_RELATION);
		}
		if (dbChildRelationParentRelation == null) {
			dbChildRelationParentRelation = envCtx.openDatabase(DB_CHILD_RELATION_PARENT_RELATION);
		}
		
		txn = envCtx.createTransaction();
		
		initialized = true;
	}
	
	
	/**
	 * Returns the node dao associated with this transaction.
	 * 
	 * @return The node dao.
	 */
	public NodeDao getNodeDao() {
		if (!initialized) {
			initialize();
		}
		
		return new NodeDao(txn, dbNode, dbTileNode);
	}
	
	
	/**
	 * Returns the way dao associated with this transaction.
	 * 
	 * @return The way dao.
	 */
	public WayDao getWayDao() {
		if (!initialized) {
			initialize();
		}
		
		return new WayDao(txn, dbWay, dbTileWay, getNodeDao());
	}
	
	
	/**
	 * Returns the relation dao associated with this transaction.
	 * 
	 * @return The relation dao.
	 */
	public RelationDao getRelationDao() {
		if (!initialized) {
			initialize();
		}
		
		return new RelationDao(txn, dbRelation, dbNodeRelation, dbWayRelation, dbChildRelationParentRelation);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		if (!initialized) {
			initialize();
		}
		
		try {
			txn.commit();
			
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to commit the transaction.", e);
		}
		
		committed = true;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (!committed) {
			try {
				txn.abort();
			} catch (DatabaseException e) {
				// Don't rethrow, just log the exception.
				log.log(Level.SEVERE, "Unable to abort the transaction", e);
			}
		}
	}
}

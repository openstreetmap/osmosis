// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.NoSuchDatabaseEntryException;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.bdb.common.UnsignedIntegerLongIndexElement;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.UnsignedIntegerComparator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;


/**
 * Performs all way-specific db operations.
 * 
 * @author Brett Henderson
 */
public class WayDao {
	
	private static final Logger log = Logger.getLogger(WayDao.class.getName());
	
	private static final int[] tileMasks = {0xFFFFFFFF, 0xFFFFFFF0, 0xFFFFFF00, 0xFFFF0000, 0xFF000000, 0x00000000};
	
	private Transaction txn;
	private Database dbWay;
	private Database dbTileWay[];
	private NodeDao nodeDao;
	private TupleBinding idBinding;
	private StoreableTupleBinding<Way> wayBinding;
	private StoreableTupleBinding<UnsignedIntegerLongIndexElement> uintLongBinding;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	private TileCalculator tileCalculator;
	private Comparator<Integer> uintComparator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The active transaction.
	 * @param dbWay
	 *            The way database.
	 * @param dbTileWay
	 *            The tile-way databases.
	 * @param nodeDao
	 *            The node DAO.
	 */
	public WayDao(Transaction transaction, Database dbWay, Database[] dbTileWay, NodeDao nodeDao) {
		this.txn = transaction;
		this.dbWay = dbWay;
		this.dbTileWay = dbTileWay;
		this.nodeDao = nodeDao;
		
		idBinding = TupleBinding.getPrimitiveBinding(Long.class);
		wayBinding = new StoreableTupleBinding<Way>(Way.class);
		uintLongBinding = new StoreableTupleBinding<UnsignedIntegerLongIndexElement>(UnsignedIntegerLongIndexElement.class);
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
		
		tileCalculator = new TileCalculator();
		uintComparator = new UnsignedIntegerComparator();
	}
	
	
	/**
	 * Calculates and writes the tile way index value for the way.
	 * 
	 * @param way
	 *            The way requiring a tile index.
	 */
	private void createTileWayIndex(Way way) {
		int minimumTile;
		int maximumTile;
		boolean tilesFound;
		
		// Calculate the minimum and maximum tile indexes for the way.
		tilesFound = false;
		minimumTile = 0;
		maximumTile = 0;
		for (WayNode wayNode : way.getWayNodeList()) {
			long nodeId;
			Node node;
			int tile;
			
			nodeId = wayNode.getNodeId();
			
			try {
				node = nodeDao.getNode(nodeId);
				
				tile = (int) tileCalculator.calculateTile(node.getLatitude(), node.getLongitude());
				
				if (tilesFound) {
					if (uintComparator.compare(tile, minimumTile) < 0) {
						minimumTile = tile;
					}
					if (uintComparator.compare(maximumTile, tile) < 0) {
						maximumTile = tile;
					}
					
				} else {
					minimumTile = tile;
					maximumTile = tile;
					
					tilesFound = true;
				}
				
			} catch (NoSuchDatabaseEntryException e) {
				// Ignore any referential integrity problems.
				if (log.isLoggable(Level.FINER)) {
					log.finest(
						"Ignoring referential integrity problem where way " + way.getId() +
						" refers to non-existent node " + nodeId + "."
					);
				}
			}
		}
		
		// Write the tile to way index element to the tile-way database matching
		// the granularity of the way, but only if tiles were found.
		if (tilesFound) {
			for (int i = 0; i < tileMasks.length; i++) {
				int mask;
				int maskedMinimum;
				int maskedMaximum;
				
				mask = tileMasks[i];
				maskedMinimum = mask & minimumTile;
				maskedMaximum = mask & maximumTile;
				
				// Write the element to the current index if the index tile
				// granularity allows the way to fit within a single tile value.
				if ((maskedMinimum) == (maskedMaximum)) {
					uintLongBinding.objectToEntry(
						new UnsignedIntegerLongIndexElement(maskedMinimum, way.getId()), keyEntry
					);
					dataEntry.setSize(0);
					
					try {
						dbTileWay[i].put(txn, keyEntry, dataEntry);
					} catch (DatabaseException e) {
						throw new OsmosisRuntimeException("Unable to write tile-way " + way.getId() + ".", e);
					}
					
					// Stop once one index has received the way.
					break;
				}
			}
		}
	}
	
	
	/**
	 * Stores the way in the way database.
	 * 
	 * @param way 
	 *            The way to be stored.
	 */
	public void putWay(Way way) {
		// Write the way object to the way database.
		idBinding.objectToEntry(way.getId(), keyEntry);
		wayBinding.objectToEntry(way, dataEntry);
		
		try {
			dbWay.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write way " + way.getId() + ".", e);
		}
		
		// Write the tile to way index element to the relevant tile-way database.
		createTileWayIndex(way);
	}
	
	
	/**
	 * Gets the specified way from the way database.
	 * 
	 * @param wayId
	 *            The id of the way to be retrieved.
	 * @return The requested way.
	 */
	public Way getWay(long wayId) {
		idBinding.objectToEntry(wayId, keyEntry);
		
		try {
			if (!OperationStatus.SUCCESS.equals(dbWay.get(txn, keyEntry, dataEntry, null))) {
				throw new NoSuchDatabaseEntryException("Way " + wayId + " does not exist in the database.");
			}
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to retrieve way " + wayId + " from the database.", e);
		}
		
		return (Way) wayBinding.entryToObject(dataEntry);
	}
	
	
	/**
	 * Provides access to all ways in the database. The iterator must be
	 * released after use.
	 * 
	 * @return An iterator pointing at the first way.
	 */
	public ReleasableIterator<Way> iterate() {
		return new DatabaseIterator<Way>(dbWay, txn, wayBinding);
	}
}

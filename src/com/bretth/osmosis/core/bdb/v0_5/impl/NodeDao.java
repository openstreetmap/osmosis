// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;


/**
 * Performs all node-specific db operations.
 * 
 * @author Brett Henderson
 */
public class NodeDao {
	private Transaction txn;
	private Database dbNode;
	private TupleBinding idBinding;
	private StoreableTupleBinding<Node> nodeBinding;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The active transaction.
	 * @param dbNode
	 *            The node database.
	 */
	public NodeDao(Transaction transaction, Database dbNode) {
		this.txn = transaction;
		this.dbNode = dbNode;
		
		idBinding = TupleBinding.getPrimitiveBinding(Long.class);
		nodeBinding = new StoreableTupleBinding<Node>();
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
	}
	
	
	/**
	 * Writes the node to the node database.
	 * 
	 * @param node
	 *            The node to be written.
	 */
	public void putNode(Node node) {
		idBinding.objectToEntry(node.getId(), keyEntry);
		nodeBinding.objectToEntry(node, dataEntry);
		
		try {
			dbNode.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write node " + node.getId() + ".", e);
		}
	}
}

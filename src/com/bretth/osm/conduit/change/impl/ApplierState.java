package com.bretth.osm.conduit.change.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;
import com.bretth.osm.conduit.task.Sink;


/**
 * Maintains the shared state between change application input threads.
 * 
 * @author Brett Henderson
 */
public class ApplierState {
	/**
	 * The lock used for synchronisation between two data source threads.
	 */
	public Lock lock;
	
	/**
	 * The condition used to allow data source threads to release the lock and
	 * wait for the other source to notify of an update.
	 */
	public Condition lockCondition;
	
	/**
	 * The sink for receiving all produced data.
	 */
	public Sink sink;
	
	/**
	 * The current processing status of the "base" data source.
	 */
	public InputStatus baseStatus;
	
	/**
	 * The current processing status of the "change" data source.
	 */
	public InputStatus changeStatus;
	
	/**
	 * A flag indicating that the "base" data source has terminated and sent the
	 * release instruction.
	 */
	public boolean baseReleased;
	
	/**
	 * A flag indicating that the "change" data source has terminated and sent the
	 * release instruction.
	 */
	public boolean changeReleased;
	
	/**
	 * The last node received from the "base" source.
	 */
	public Node lastBaseNode;
	
	/**
	 * The last node received from the "change" source.
	 */
	public Node lastChangeNode;
	
	/**
	 * The last segment received from the "base" source.
	 */
	public Segment lastBaseSegment;
	
	/**
	 * The last segment received from the "change" source.
	 */
	public Segment lastChangeSegment;
	
	/**
	 * The last way received from the "base" source.
	 */
	public Way lastBaseWay;
	
	/**
	 * The last way received from the "change" source.
	 */
	public Way lastChangeWay;
	
	
	/**
	 * The action related to the last "change" data element.
	 */
	public ChangeAction lastChangeAction;


	/**
	 * Creates a new instance.
	 */
	public ApplierState() {
		lock = new ReentrantLock();
		lockCondition = lock.newCondition();
		baseStatus = InputStatus.NOT_STARTED;
		changeStatus = InputStatus.NOT_STARTED;
	}


	/**
	 * Sets the sink to send data to.
	 * 
	 * @param sink
	 *            The sink for receiving all produced data.
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}

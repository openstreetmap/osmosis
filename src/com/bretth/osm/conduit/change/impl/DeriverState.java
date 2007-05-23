package com.bretth.osm.conduit.change.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeSink;


/**
 * Maintains the shared state between comparison data input threads.
 * 
 * @author Brett Henderson
 */
public class DeriverState {
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
	 * The sink for receiving all change notifications.
	 */
	public ChangeSink changeSink;
	
	/**
	 * The current processing status of the "from" data source.
	 */
	public InputStatus fromStatus;
	
	/**
	 * The current processing status of the "to" data source.
	 */
	public InputStatus toStatus;
	
	/**
	 * A flag indicating that the "from" data source has terminated and sent the
	 * release instruction.
	 */
	public boolean fromReleased;
	
	/**
	 * A flag indicating that the "to" data source has terminated and sent the
	 * release instruction.
	 */
	public boolean toReleased;
	
	/**
	 * The last node received from the "from" source.
	 */
	public Node lastFromNode;
	
	/**
	 * The last node received from the "to" source.
	 */
	public Node lastToNode;
	
	/**
	 * The last segment received from the "from" source.
	 */
	public Segment lastFromSegment;
	
	/**
	 * The last segment received from the "to" source.
	 */
	public Segment lastToSegment;
	
	/**
	 * The last way received from the "from" source.
	 */
	public Way lastFromWay;
	
	/**
	 * The last way received from the "to" source.
	 */
	public Way lastToWay;


	/**
	 * Creates a new instance.
	 */
	public DeriverState() {
		lock = new ReentrantLock();
		lockCondition = lock.newCondition();
		fromStatus = InputStatus.NOT_STARTED;
		toStatus = InputStatus.NOT_STARTED;
	}


	/**
	 * Sets the change sink to send data to.
	 * 
	 * @param changeSink
	 *            The sink for receiving all produced data.
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
}

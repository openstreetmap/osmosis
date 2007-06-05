package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;


/**
 * Receives input data for the "from" input to the comparison.
 * 
 * @author Brett Henderson
 */
public class DeriverFromInput extends DeriverInput {

	/**
	 * Creates a new instance.
	 * 
	 * @param comparisonState
	 *            The shared state between input sources.
	 */
	public DeriverFromInput(DeriverState comparisonState) {
		super(comparisonState);
	}

	
	/**
	 * Performs analysis of the specified node.
	 * 
	 * @param node
	 *            The node to examine.
	 */
	public void processNode(Node node) {
		sharedInputState.lock.lock();

		try {
			ComparisonOutcome comparisonOutcome;
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.fromStatus,
				InputStatus.NODE_STAGE,
				sharedInputState.lastFromNode,
				node
			);
			
			// Update the state to match the new data.
			sharedInputState.fromStatus = InputStatus.NODE_STAGE;
			sharedInputState.lastFromNode = node;
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				new InputState() {
					public InputStatus getThisSourceStatus() {
						return sharedInputState.fromStatus;
					}				
					public OsmElement getThisSourceElement() {
						return sharedInputState.lastFromNode;
					}
					public InputStatus getComparisonSourceStatus() {
						return sharedInputState.toStatus;
					}
					public OsmElement getComparisonSourceElement() {
						return sharedInputState.lastToNode;
					}
					public void checkForErrors() {
						validateNoErrors();
					}
				},
				true
			);
			
			// The "from" source only cares about elements that don't exist in
			// the other source, the "to" source performs content comparison for
			// equal elements.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "to" source therefore it
				// has been deleted.
				sharedInputState.changeSink.processNode(
					sharedInputState.lastFromNode,
					ChangeAction.Delete
				);
			}
			
		} finally {
			sharedInputState.lock.unlock();
		}
	}


	/**
	 * Performs analysis of the specified segment.
	 * 
	 * @param segment
	 *            The segment to examine.
	 */
	public void processSegment(Segment segment) {
		sharedInputState.lock.lock();

		try {
			ComparisonOutcome comparisonOutcome;
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.fromStatus,
				InputStatus.SEGMENT_STAGE,
				sharedInputState.lastFromSegment,
				segment
			);
			
			// Update the state to match the new data.
			sharedInputState.fromStatus = InputStatus.SEGMENT_STAGE;
			sharedInputState.lastFromSegment = segment;
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				new InputState() {
					public InputStatus getThisSourceStatus() {
						return sharedInputState.fromStatus;
					}				
					public OsmElement getThisSourceElement() {
						return sharedInputState.lastFromSegment;
					}
					public InputStatus getComparisonSourceStatus() {
						return sharedInputState.toStatus;
					}
					public OsmElement getComparisonSourceElement() {
						return sharedInputState.lastToSegment;
					}
					public void checkForErrors() {
						validateNoErrors();
					}
				},
				true
			);
			
			// The "from" source only cares about elements that don't exist in
			// the other source, the "to" source performs content comparison for
			// equal elements.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "to" source therefore it
				// has been deleted.
				sharedInputState.changeSink.processSegment(
					sharedInputState.lastFromSegment,
					ChangeAction.Delete
				);
			}
			
		} finally {
			sharedInputState.lock.unlock();
		}
	}


	/**
	 * Performs analysis of the specified way.
	 * 
	 * @param way
	 *            The way to examine.
	 */
	public void processWay(Way way) {
		sharedInputState.lock.lock();

		try {
			ComparisonOutcome comparisonOutcome;
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.fromStatus,
				InputStatus.WAY_STAGE,
				sharedInputState.lastFromWay,
				way
			);
			
			// Update the state to match the new data.
			sharedInputState.fromStatus = InputStatus.WAY_STAGE;
			sharedInputState.lastFromWay = way;
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				new InputState() {
					public InputStatus getThisSourceStatus() {
						return sharedInputState.fromStatus;
					}				
					public OsmElement getThisSourceElement() {
						return sharedInputState.lastFromWay;
					}
					public InputStatus getComparisonSourceStatus() {
						return sharedInputState.toStatus;
					}
					public OsmElement getComparisonSourceElement() {
						return sharedInputState.lastToWay;
					}
					public void checkForErrors() {
						validateNoErrors();
					}
				},
				true
			);
			
			// The "from" source only cares about elements that don't exist in
			// the other source, the "to" source performs content comparison for
			// equal elements.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "to" source therefore it
				// has been deleted.
				sharedInputState.changeSink.processWay(
					sharedInputState.lastFromWay,
					ChangeAction.Delete
				);
			}
			
		} finally {
			sharedInputState.lock.unlock();
		}
	}


	/**
	 * Flags this source as complete. If both sources are complete, a complete
	 * request is called on the change sink.
	 */
	public void complete() {
		sharedInputState.lock.lock();

		try {
			// Ensure no errors have occurred.
			validateNoErrors();
			
			if (sharedInputState.fromStatus.compareTo(InputStatus.COMPLETE) < 0) {
				if (sharedInputState.toStatus.compareTo(InputStatus.COMPLETE) >= 0) {
					sharedInputState.changeSink.complete();
				}
				
				sharedInputState.fromStatus = InputStatus.COMPLETE;
			}
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
		} finally {
			sharedInputState.lock.unlock();
		}
	}


	/**
	 * Flags this source as released. If both sources are released, a release
	 * request is called on the change sink.
	 */
	public void release() {
		sharedInputState.lock.lock();

		try {
			if (!sharedInputState.fromReleased) {
				if (sharedInputState.toReleased) {
					sharedInputState.changeSink.release();
				}

				sharedInputState.fromReleased = true;
			}
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
		} finally {
			sharedInputState.lock.unlock();
		}
	}
}

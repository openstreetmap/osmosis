package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;


/**
 * Receives input data for the "to" input to the comparison.
 * 
 * @author Brett Henderson
 */
public class DeriverToInput extends DeriverInput {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparisonState
	 *            The shared state between input sources.
	 */
	public DeriverToInput(DeriverState comparisonState) {
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
			
			// Ensure no errors have occurred.
			validateNoErrors();
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.toStatus,
				InputStatus.NODE_STAGE,
				sharedInputState.lastToNode,
				node
			);
			
			// Update the state to match the new data.
			sharedInputState.toStatus = InputStatus.NODE_STAGE;
			sharedInputState.lastToNode = node;
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				sharedInputState.toStatus,
				sharedInputState.fromStatus,
				sharedInputState.lastToNode,
				sharedInputState.lastFromNode,
				true
			);
			
			// The "from" source only cares about elements that don't exist in
			// the other source, the "to" source performs content comparison for
			// equal elements.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "from" source therefore it
				// has been added.
				sharedInputState.changeSink.processNode(
					sharedInputState.lastToNode,
					ChangeAction.Create
				);
			} else if (comparisonOutcome == ComparisonOutcome.SameElement) {
				// The same element exists in both sources, we need to compare
				// the elements to see if their contents are different.
				if (sharedInputState.lastFromNode.compareTo(sharedInputState.lastToNode) != 0) {
					sharedInputState.changeSink.processNode(
						sharedInputState.lastToNode,
						ChangeAction.Modify
					);
				}
			}
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
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
			
			// Ensure no errors have occurred.
			validateNoErrors();
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.toStatus,
				InputStatus.SEGMENT_STAGE,
				sharedInputState.lastToSegment,
				segment
			);
			
			// Update the state to match the new data.
			sharedInputState.toStatus = InputStatus.SEGMENT_STAGE;
			sharedInputState.lastToSegment = segment;
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				sharedInputState.toStatus,
				sharedInputState.fromStatus,
				sharedInputState.lastToSegment,
				sharedInputState.lastFromSegment,
				true
			);
			
			// The "from" source only cares about elements that don't exist in
			// the other source, the "to" source performs content comparison for
			// equal elements.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "from" source therefore it
				// has been added.
				sharedInputState.changeSink.processSegment(
					sharedInputState.lastToSegment,
					ChangeAction.Create
				);
			} else if (comparisonOutcome == ComparisonOutcome.SameElement) {
				// The same element exists in both sources, we need to compare
				// the elements to see if their contents are different.
				if (sharedInputState.lastFromSegment.compareTo(sharedInputState.lastToSegment) != 0) {
					sharedInputState.changeSink.processSegment(
						sharedInputState.lastToSegment,
						ChangeAction.Modify
					);
				}
			}
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
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
			
			// Ensure no errors have occurred.
			validateNoErrors();
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.toStatus,
				InputStatus.WAY_STAGE,
				sharedInputState.lastToWay,
				way
			);
			
			// Update the state to match the new data.
			sharedInputState.toStatus = InputStatus.WAY_STAGE;
			sharedInputState.lastToWay = way;
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				sharedInputState.toStatus,
				sharedInputState.fromStatus,
				sharedInputState.lastToWay,
				sharedInputState.lastFromWay,
				true
			);
			
			// The "from" source only cares about elements that don't exist in
			// the other source, the "to" source performs content comparison for
			// equal elements.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "from" source therefore it
				// has been added.
				sharedInputState.changeSink.processWay(
					sharedInputState.lastToWay,
					ChangeAction.Create
				);
			} else if (comparisonOutcome == ComparisonOutcome.SameElement) {
				// The same element exists in both sources, we need to compare
				// the elements to see if their contents are different.
				if (sharedInputState.lastFromWay.compareTo(sharedInputState.lastToWay) != 0) {
					sharedInputState.changeSink.processWay(
						sharedInputState.lastToWay,
						ChangeAction.Modify
					);
				}
			}
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
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
			
			if (sharedInputState.toStatus.compareTo(InputStatus.COMPLETE) < 0) {
				if (sharedInputState.fromStatus.compareTo(InputStatus.COMPLETE) >= 0) {
					sharedInputState.changeSink.complete();
				}
				
				sharedInputState.toStatus = InputStatus.COMPLETE;
			}
			
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
			if (!sharedInputState.toReleased) {
				if (sharedInputState.fromReleased) {
					sharedInputState.changeSink.release();
				}

				sharedInputState.toReleased = true;
			}
		} finally {
			sharedInputState.lock.unlock();
		}
	}
}

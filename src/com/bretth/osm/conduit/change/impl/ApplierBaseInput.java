package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.Sink;


/**
 * Receives input data for the "base" input to the change application.
 * 
 * @author Brett Henderson
 */
public class ApplierBaseInput extends ApplierInput implements Sink {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sharedInputState
	 *            The shared state between input sources.
	 */
	public ApplierBaseInput(ApplierState sharedInputState) {
		super(sharedInputState);
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
				sharedInputState.baseStatus,
				InputStatus.NODE_STAGE,
				sharedInputState.lastBaseNode,
				node
			);
			
			// Update the state to match the new data.
			sharedInputState.baseStatus = InputStatus.NODE_STAGE;
			sharedInputState.lastBaseNode = node;
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();

			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				new InputState() {
					public InputStatus getThisSourceStatus() {
						return sharedInputState.baseStatus;
					}				
					public OsmElement getThisSourceElement() {
						return sharedInputState.lastBaseNode;
					}
					public InputStatus getComparisonSourceStatus() {
						return sharedInputState.changeStatus;
					}
					public OsmElement getComparisonSourceElement() {
						return sharedInputState.lastChangeNode;
					}
					public void checkForErrors() {
						validateNoErrors();
					}
				},
				true
			);
			
			// The "base" source only cares about elements that don't exist in
			// the "change" source (ie. the unchanged elements), the "change"
			// source performs all other updates.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "change" source therefore it
				// is unchanged and must be passed to the destination.
				sharedInputState.sink.processNode(
					sharedInputState.lastBaseNode
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
				sharedInputState.baseStatus,
				InputStatus.SEGMENT_STAGE,
				sharedInputState.lastBaseSegment,
				segment
			);
			
			// Update the state to match the new data.
			sharedInputState.baseStatus = InputStatus.SEGMENT_STAGE;
			sharedInputState.lastBaseSegment = segment;
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				new InputState() {
					public InputStatus getThisSourceStatus() {
						return sharedInputState.baseStatus;
					}				
					public OsmElement getThisSourceElement() {
						return sharedInputState.lastBaseSegment;
					}
					public InputStatus getComparisonSourceStatus() {
						return sharedInputState.changeStatus;
					}
					public OsmElement getComparisonSourceElement() {
						return sharedInputState.lastChangeSegment;
					}
					public void checkForErrors() {
						validateNoErrors();
					}
				},
				true
			);
			
			// The "base" source only cares about elements that don't exist in
			// the "change" source (ie. the unchanged elements), the "change"
			// source performs all other updates.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "change" source therefore it
				// is unchanged and must be passed to the destination.
				sharedInputState.sink.processSegment(
					sharedInputState.lastBaseSegment
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
				sharedInputState.baseStatus,
				InputStatus.SEGMENT_STAGE,
				sharedInputState.lastBaseWay,
				way
			);
			
			// Update the state to match the new data.
			sharedInputState.baseStatus = InputStatus.WAY_STAGE;
			sharedInputState.lastBaseWay = way;
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				new InputState() {
					public InputStatus getThisSourceStatus() {
						return sharedInputState.baseStatus;
					}				
					public OsmElement getThisSourceElement() {
						return sharedInputState.lastBaseWay;
					}
					public InputStatus getComparisonSourceStatus() {
						return sharedInputState.changeStatus;
					}
					public OsmElement getComparisonSourceElement() {
						return sharedInputState.lastChangeWay;
					}
					public void checkForErrors() {
						validateNoErrors();
					}
				},
				true
			);
			
			// The "base" source only cares about elements that don't exist in
			// the "change" source (ie. the unchanged elements), the "change"
			// source performs all other updates.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "change" source therefore it
				// is unchanged and must be passed to the destination.
				sharedInputState.sink.processWay(
					sharedInputState.lastBaseWay
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
			
			if (sharedInputState.baseStatus.compareTo(InputStatus.COMPLETE) < 0) {
				if (sharedInputState.changeStatus.compareTo(InputStatus.COMPLETE) >= 0) {
					sharedInputState.sink.complete();
				}
				
				sharedInputState.baseStatus = InputStatus.COMPLETE;
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
			if (!sharedInputState.baseReleased) {
				if (sharedInputState.changeReleased) {
					sharedInputState.sink.release();
				}

				sharedInputState.baseReleased = true;
			}
			
			// Notify the other source that new data is available.
			sharedInputState.lockCondition.signal();
			
		} finally {
			sharedInputState.lock.unlock();
		}
	}
}

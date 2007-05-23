package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;
import com.bretth.osm.conduit.task.ChangeSink;


/**
 * Receives input data for the "change" input to the change application.
 * 
 * @author Brett Henderson
 */
public class ApplierChangeInput extends ApplierInput implements ChangeSink {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sharedInputState
	 *            The shared state between input sources.
	 */
	public ApplierChangeInput(ApplierState sharedInputState) {
		super(sharedInputState);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node, ChangeAction action) {
		sharedInputState.lock.lock();

		try {
			ComparisonOutcome comparisonOutcome;
			
			// Ensure no errors have occurred.
			validateNoErrors();
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.changeStatus,
				InputStatus.NODE_STAGE,
				sharedInputState.lastChangeNode,
				node
			);
			
			// Update the state to match the new data.
			sharedInputState.changeStatus = InputStatus.NODE_STAGE;
			sharedInputState.lastChangeNode = node;
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				sharedInputState.changeStatus,
				sharedInputState.baseStatus,
				sharedInputState.lastChangeNode,
				sharedInputState.lastBaseNode,
				true
			);
			
			// The "base" source only cares about elements that don't exist in
			// the "change" source (ie. the unchanged elements), the "change"
			// source performs all other updates.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "base" source therefore we
				// are expecting an add.
				if (action.equals(ChangeAction.Create)) {
					sharedInputState.sink.addNode(sharedInputState.lastChangeNode);
					
				} else {
					throw new ConduitRuntimeException(
						"Cannot perform action " + action + " on node with id="
						+ sharedInputState.lastChangeNode.getId()
						+ " because it doesn't exist in the base source."
					);
				}
				
			} else if (comparisonOutcome == ComparisonOutcome.SameElement) {
				// The same element exists in both sources therefore we are
				// expecting a modify or delete.
				if (action.equals(ChangeAction.Modify)) {
					sharedInputState.sink.addNode(sharedInputState.lastChangeNode);
					
				} else if (action.equals(ChangeAction.Delete)) {
					// We don't need to do anything for delete.
					
				} else {
					throw new ConduitRuntimeException(
						"Cannot perform action " + action + " on node with id="
						+ sharedInputState.lastChangeNode.getId()
						+ " because it exists in the base source."
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
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment, ChangeAction action) {
		sharedInputState.lock.lock();

		try {
			ComparisonOutcome comparisonOutcome;
			
			// Ensure no errors have occurred.
			validateNoErrors();
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.changeStatus,
				InputStatus.SEGMENT_STAGE,
				sharedInputState.lastChangeSegment,
				segment
			);
			
			// Update the state to match the new data.
			sharedInputState.changeStatus = InputStatus.SEGMENT_STAGE;
			sharedInputState.lastChangeSegment = segment;
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				sharedInputState.changeStatus,
				sharedInputState.baseStatus,
				sharedInputState.lastChangeSegment,
				sharedInputState.lastBaseSegment,
				true
			);
			
			// The "base" source only cares about elements that don't exist in
			// the "change" source (ie. the unchanged elements), the "change"
			// source performs all other updates.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "base" source therefore we
				// are expecting an add.
				if (action.equals(ChangeAction.Create)) {
					sharedInputState.sink.addSegment(sharedInputState.lastChangeSegment);
					
				} else {
					throw new ConduitRuntimeException(
						"Cannot perform action " + action + " on segment with id="
						+ sharedInputState.lastChangeSegment.getId()
						+ " because it doesn't exist in the base source."
					);
				}
				
			} else if (comparisonOutcome == ComparisonOutcome.SameElement) {
				// The same element exists in both sources therefore we are
				// expecting a modify or delete.
				if (action.equals(ChangeAction.Modify)) {
					sharedInputState.sink.addSegment(sharedInputState.lastChangeSegment);
					
				} else if (action.equals(ChangeAction.Delete)) {
					// We don't need to do anything for delete.
					
				} else {
					throw new ConduitRuntimeException(
						"Cannot perform action " + action + " on segment with id="
						+ sharedInputState.lastChangeSegment.getId()
						+ " because it exists in the base source."
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
	 * {@inheritDoc}
	 */
	public void processWay(Way way, ChangeAction action) {
		sharedInputState.lock.lock();

		try {
			ComparisonOutcome comparisonOutcome;
			
			// Ensure no errors have occurred.
			validateNoErrors();
			
			// Ensure the new processing state and data are valid.
			validateState(
				sharedInputState.changeStatus,
				InputStatus.WAY_STAGE,
				sharedInputState.lastChangeWay,
				way
			);
			
			// Update the state to match the new data.
			sharedInputState.changeStatus = InputStatus.WAY_STAGE;
			sharedInputState.lastChangeWay = way;
			
			// Perform the comparison.
			comparisonOutcome = performElementComparison(
				sharedInputState.lockCondition,
				sharedInputState.changeStatus,
				sharedInputState.baseStatus,
				sharedInputState.lastChangeWay,
				sharedInputState.lastBaseWay,
				true
			);
			
			// The "base" source only cares about elements that don't exist in
			// the "change" source (ie. the unchanged elements), the "change"
			// source performs all other updates.
			if (comparisonOutcome == ComparisonOutcome.DifferentElement) {
				// This element doesn't exist in the "base" source therefore we
				// are expecting an add.
				if (action.equals(ChangeAction.Create)) {
					sharedInputState.sink.addWay(sharedInputState.lastChangeWay);
					
				} else {
					throw new ConduitRuntimeException(
						"Cannot perform action " + action + " on way with id="
						+ sharedInputState.lastChangeWay.getId()
						+ " because it doesn't exist in the base source."
					);
				}
				
			} else if (comparisonOutcome == ComparisonOutcome.SameElement) {
				// The same element exists in both sources therefore we are
				// expecting a modify or delete.
				if (action.equals(ChangeAction.Modify)) {
					sharedInputState.sink.addWay(sharedInputState.lastChangeWay);
					
				} else if (action.equals(ChangeAction.Delete)) {
					// We don't need to do anything for delete.
					
				} else {
					throw new ConduitRuntimeException(
						"Cannot perform action " + action + " on way with id="
						+ sharedInputState.lastChangeWay.getId()
						+ " because it exists in the base source."
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
			
			if (sharedInputState.changeStatus.compareTo(InputStatus.COMPLETE) < 0) {
				if (sharedInputState.baseStatus.compareTo(InputStatus.COMPLETE) >= 0) {
					sharedInputState.sink.complete();
				}
				
				sharedInputState.changeStatus = InputStatus.COMPLETE;
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
			if (!sharedInputState.changeReleased) {
				if (sharedInputState.baseReleased) {
					sharedInputState.sink.release();
				}

				sharedInputState.changeReleased = true;
			}
		} finally {
			sharedInputState.lock.unlock();
		}
	}
}

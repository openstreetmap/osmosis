// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.merge.v0_6;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.merge.v0_6.impl.SortedChangePipeValidator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.MultiChangeSinkRunnableChangeSource;


/**
 * Merges two change sources into a single data set. Conflicting elements are
 * resolved by using either the latest timestamp (default) or always selecting
 * the second source.
 * 
 * @author Brett Henderson
 */
public class ChangeMerger implements MultiChangeSinkRunnableChangeSource {
	
	private ChangeSink changeSink;
	private DataPostbox<ChangeContainer> postbox0;
	private DataPostbox<ChangeContainer> postbox1;
	private ConflictResolutionMethod conflictResolutionMethod;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param conflictResolutionMethod
	 *            The method to used to resolve conflict when two sources
	 *            contain the same entity.
	 * @param inputBufferCapacity
	 *            The size of the buffers to use for input sources.
	 */
	public ChangeMerger(ConflictResolutionMethod conflictResolutionMethod, int inputBufferCapacity) {
		this.conflictResolutionMethod = conflictResolutionMethod;
		
		postbox0 = new DataPostbox<ChangeContainer>(inputBufferCapacity);
		postbox1 = new DataPostbox<ChangeContainer>(inputBufferCapacity);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ChangeSink getChangeSink(int instance) {
		final DataPostbox<ChangeContainer> destinationPostbox;
		ChangeSink postboxChangeSink;
		SortedChangePipeValidator sortedPipeValidator;
		
		// Determine which postbox should be written to.
		switch (instance) {
		case 0:
			destinationPostbox = postbox0;
			break;
		case 1:
			destinationPostbox = postbox1;
			break;
		default:
			throw new OsmosisRuntimeException("Sink instance " + instance + " is not valid.");
		}
		
		// Create a changesink pointing to the postbox.
		postboxChangeSink = new ChangeSink() {
			private DataPostbox<ChangeContainer> postbox = destinationPostbox;

			public void process(ChangeContainer change) {
				postbox.put(change);
			}
			public void complete() {
				postbox.complete();
			}
			public void release() {
				postbox.release();
			}
		};
		
		// Create a validation class to verify that all incoming data is sorted
		// and connect its output to the postbox changesink.
		sortedPipeValidator = new SortedChangePipeValidator();
		sortedPipeValidator.setChangeSink(postboxChangeSink);
		
		return sortedPipeValidator;
	}


	/**
	 * This implementation always returns 2.
	 * 
	 * @return 2
	 */
	public int getChangeSinkCount() {
		return 2;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void run() {
		boolean completed = false;
		
		try {
			EntityByTypeThenIdComparator comparator;
			ChangeContainer changeContainer0 = null;
			ChangeContainer changeContainer1 = null;
			
			// Create a comparator for comparing two entities by type and identifier.
			comparator = new EntityByTypeThenIdComparator();
			
			// We continue in the comparison loop while both sources still have data.
			while (
					(changeContainer0 != null || postbox0.hasNext())
					&& (changeContainer1 != null || postbox1.hasNext())) {
				long comparisonResult;
				
				// Get the next input data where required.
				if (changeContainer0 == null) {
					changeContainer0 = postbox0.getNext();
				}
				if (changeContainer1 == null) {
					changeContainer1 = postbox1.getNext();
				}
				
				// Compare the two entities.
				comparisonResult =
					comparator.compare(changeContainer0.getEntityContainer(), changeContainer1.getEntityContainer());
				
				if (comparisonResult < 0) {
					// Entity 0 doesn't exist on the other source and can be
					// sent straight through.
					changeSink.process(changeContainer0);
					changeContainer0 = null;
				} else if (comparisonResult > 0) {
					// Entity 1 doesn't exist on the other source and can be
					// sent straight through.
					changeSink.process(changeContainer1);
					changeContainer1 = null;
				} else {
					// The entity exists on both sources so we must resolve the conflict.
					if (conflictResolutionMethod.equals(ConflictResolutionMethod.Timestamp)) {
						int timestampComparisonResult;
						
						timestampComparisonResult =
							changeContainer0.getEntityContainer().getEntity().getTimestamp()
							.compareTo(changeContainer1.getEntityContainer().getEntity().getTimestamp());
						
						if (timestampComparisonResult < 0) {
							changeSink.process(changeContainer1);
						} else if (timestampComparisonResult > 0) {
							changeSink.process(changeContainer0);
						} else {
							// If both have identical timestamps, use the second source.
							changeSink.process(changeContainer1);
						}
						
					} else if (conflictResolutionMethod.equals(ConflictResolutionMethod.LatestSource)) {
						changeSink.process(changeContainer1);
					} else if (conflictResolutionMethod.equals(ConflictResolutionMethod.Version)) {
						int version0 = changeContainer0.getEntityContainer().getEntity().getVersion();
						int version1 = changeContainer1.getEntityContainer().getEntity().getVersion();
						if (version0 < version1) {
							changeSink.process(changeContainer1);
						} else if (version0 > version1) {
							changeSink.process(changeContainer0);
						} else {
							// If both have identical versions, use the second source.
							changeSink.process(changeContainer1);
						}

					} else {
						throw new OsmosisRuntimeException(
								"Conflict resolution method " + conflictResolutionMethod + " is not recognized.");
					}
					
					changeContainer0 = null;
					changeContainer1 = null;
				}
			}
			
			// Any remaining entities on either source can be sent straight through.
			while (changeContainer0 != null || postbox0.hasNext()) {
				if (changeContainer0 == null) {
					changeContainer0 = postbox0.getNext();
				}
				changeSink.process(changeContainer0);
				changeContainer0 = null;
			}
			while (changeContainer1 != null || postbox1.hasNext()) {
				if (changeContainer1 == null) {
					changeContainer1 = postbox1.getNext();
				}
				changeSink.process(changeContainer1);
				changeContainer1 = null;
			}
			
			changeSink.complete();
			
			completed = true;
			
		} finally {
			if (!completed) {
				postbox0.setOutputError();
				postbox1.setOutputError();
			}
			
			changeSink.release();
		}
	}
}

// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.util.Collections;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdThenVersionComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityContainerComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.SortedHistoryChangePipeValidator;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.MultiChangeSinkRunnableChangeSource;
import org.openstreetmap.osmosis.set.v0_6.impl.DataPostboxChangeSink;


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
	private SortedHistoryChangePipeValidator sortedChangeValidator0;
	private DataPostbox<ChangeContainer> postbox1;
	private SortedHistoryChangePipeValidator sortedChangeValidator1;
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
		sortedChangeValidator0 = new SortedHistoryChangePipeValidator();
		sortedChangeValidator0.setChangeSink(new DataPostboxChangeSink(postbox0));
		
		postbox1 = new DataPostbox<ChangeContainer>(inputBufferCapacity);
		sortedChangeValidator1 = new SortedHistoryChangePipeValidator();
		sortedChangeValidator1.setChangeSink(new DataPostboxChangeSink(postbox1));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ChangeSink getChangeSink(int instance) {
		// Determine which postbox should be written to.
		switch (instance) {
		case 0:
			return sortedChangeValidator0;
		case 1:
			return sortedChangeValidator1;
		default:
			throw new OsmosisRuntimeException("Sink instance " + instance + " is not valid.");
		}
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
		try {
			EntityContainerComparator comparator;
			ChangeContainer changeContainer0 = null;
			ChangeContainer changeContainer1 = null;
			
			// Create a comparator for comparing two entities by type and identifier.
			comparator = new EntityContainerComparator(new EntityByTypeThenIdThenVersionComparator());
			
			// We can't get meaningful data from the initialize data on the
			// input streams, so pass empty meta data to the sink and discard
			// the input meta data.
			postbox0.outputInitialize();
			postbox1.outputInitialize();
			changeSink.initialize(Collections.<String, Object>emptyMap());
			
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
			
			postbox0.outputComplete();
			postbox1.outputComplete();
			
		} finally {
			changeSink.release();
			
			postbox0.outputRelease();
			postbox1.outputRelease();
		}
	}
}

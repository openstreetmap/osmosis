package com.bretth.osmosis.core.merge.v0_5;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.merge.common.ConflictResolutionMethod;
import com.bretth.osmosis.core.merge.v0_5.impl.SortedEntityPipeValidator;
import com.bretth.osmosis.core.sort.v0_5.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.store.DataPostbox;
import com.bretth.osmosis.core.task.v0_5.MultiSinkRunnableSource;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * Merges two sources into a single data set. Conflicting elements are resolved
 * by using either the latest timestamp (default) or always selecting the second
 * source.
 * 
 * @author Brett Henderson
 */
public class EntityMerger implements MultiSinkRunnableSource {
	
	private Sink sink;
	private DataPostbox<EntityContainer> postbox0;
	private DataPostbox<EntityContainer> postbox1;
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
	public EntityMerger(ConflictResolutionMethod conflictResolutionMethod, int inputBufferCapacity) {
		this.conflictResolutionMethod = conflictResolutionMethod;
		
		postbox0 = new DataPostbox<EntityContainer>(inputBufferCapacity);
		postbox1 = new DataPostbox<EntityContainer>(inputBufferCapacity);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		final DataPostbox<EntityContainer> destinationPostbox;
		Sink postboxSink;
		SortedEntityPipeValidator sortedPipeValidator;
		
		// Determine which postbox should be written to.
		switch (instance) {
		case 0:
			destinationPostbox = postbox0;
			break;
		case 1:
			destinationPostbox = postbox1;
			break;
		default:
			throw new OsmosisRuntimeException("Sink instance " + instance
					+ " is not valid.");
		}
		
		// Create a changesink pointing to the postbox.
		postboxSink = new Sink() {
			private DataPostbox<EntityContainer> postbox = destinationPostbox;
			
			public void process(EntityContainer entityContainer) {
				postbox.put(entityContainer);
			}
			public void complete() {
				postbox.complete();
			}
			public void release() {
				postbox.release();
			}
		};
		
		// Create a validation class to verify that all incoming data is sorted
		// and connect its output to the postbox sink.
		sortedPipeValidator = new SortedEntityPipeValidator();
		sortedPipeValidator.setSink(postboxSink);
		
		return sortedPipeValidator;
	}


	/**
	 * This implementation always returns 2.
	 * 
	 * @return 2
	 */
	public int getSinkCount() {
		return 2;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void run() {
		boolean completed = false;
		
		try {
			EntityByTypeThenIdComparator comparator;
			EntityContainer entityContainer0 = null;
			EntityContainer entityContainer1 = null;
			
			// Create a comparator for comparing two entities by type and identifier.
			comparator = new EntityByTypeThenIdComparator();
			
			// We continue in the comparison loop while both sources still have data.
			while ((entityContainer0 != null || postbox0.hasNext()) && (entityContainer1 != null || postbox1.hasNext())) {
				long comparisonResult;
				
				// Get the next input data where required.
				if (entityContainer0 == null) {
					entityContainer0 = postbox0.getNext();
				}
				if (entityContainer1 == null) {
					entityContainer1 = postbox1.getNext();
				}
				
				// Compare the two entities.
				comparisonResult = comparator.compare(entityContainer0, entityContainer1);
				
				if (comparisonResult < 0) {
					// Entity 0 doesn't exist on the other source and can be
					// sent straight through.
					sink.process(entityContainer0);
					entityContainer0 = null;
				} else if (comparisonResult > 0) {
					// Entity 1 doesn't exist on the other source and can be
					// sent straight through.
					sink.process(entityContainer1);
					entityContainer1 = null;
				} else {
					// The entity exists on both sources so we must resolve the conflict.
					if (conflictResolutionMethod.equals(ConflictResolutionMethod.Timestamp)) {
						int timestampComparisonResult;
						
						timestampComparisonResult = entityContainer0.getEntity().getTimestamp().compareTo(entityContainer1.getEntity().getTimestamp());
						
						if (timestampComparisonResult < 0) {
							sink.process(entityContainer1);
						} else if (timestampComparisonResult > 0) {
							sink.process(entityContainer0);
						} else {
							// If both have identical timestamps, use the second source.
							sink.process(entityContainer1);
						}
						
					} else if (conflictResolutionMethod.equals(ConflictResolutionMethod.LatestSource)) {
						sink.process(entityContainer1);
					} else {
						throw new OsmosisRuntimeException(
								"Conflict resolution method " + conflictResolutionMethod + " is not recognized.");
					}
					
					entityContainer0 = null;
					entityContainer1 = null;
				}
			}
			
			// Any remaining entities on either source can be sent straight through.
			while (entityContainer0 != null || postbox0.hasNext()) {
				if (entityContainer0 == null) {
					entityContainer0 = postbox0.getNext();
				}
				sink.process(entityContainer0);
				entityContainer0 = null;
			}
			while (entityContainer1 != null || postbox1.hasNext()) {
				if (entityContainer1 == null) {
					entityContainer1 = postbox1.getNext();
				}
				sink.process(entityContainer1);
				entityContainer1 = null;
			}
			
			sink.complete();
			
			completed = true;
			
		} finally {
			if (!completed) {
				postbox0.setOutputError();
				postbox1.setOutputError();
			}
			
			sink.release();
		}
	}
}

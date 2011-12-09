// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.util.Collections;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityContainerComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.SortedEntityPipeValidator;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_6.MultiSinkRunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.set.v0_6.impl.DataPostboxSink;


/**
 * Merges two sources into a single data set. Conflicting elements are resolved
 * by using either the latest timestamp (default) or always selecting the second
 * source.
 * 
 * @author Brett Henderson
 */
public class EntityMerger implements MultiSinkRunnableSource {
	
	private static final Logger LOG = Logger.getLogger(EntityMerger.class.getName());

	private Sink sink;
	private DataPostbox<EntityContainer> postbox0;
	private SortedEntityPipeValidator sortedEntityValidator0;
	private DataPostbox<EntityContainer> postbox1;
	private SortedEntityPipeValidator sortedEntityValidator1;
	private ConflictResolutionMethod conflictResolutionMethod;
	private BoundRemovedAction boundRemovedAction;

	/**
	 * Creates a new instance.
	 * 
	 * @param conflictResolutionMethod
	 *            The method to used to resolve conflict when two sources
	 *            contain the same entity.
	 * @param inputBufferCapacity
	 *            The size of the buffers to use for input sources.
	 * @param boundRemovedAction
	 *            The action to take if the merge operation removes 
	 *            a bound entity.
	 */
	public EntityMerger(ConflictResolutionMethod conflictResolutionMethod, int inputBufferCapacity, 
			BoundRemovedAction boundRemovedAction) {
		
		this.conflictResolutionMethod = conflictResolutionMethod;
		
		postbox0 = new DataPostbox<EntityContainer>(inputBufferCapacity);
		sortedEntityValidator0 = new SortedEntityPipeValidator();
		sortedEntityValidator0.setSink(new DataPostboxSink(postbox0));
		
		postbox1 = new DataPostbox<EntityContainer>(inputBufferCapacity);
		sortedEntityValidator1 = new SortedEntityPipeValidator();
		sortedEntityValidator1.setSink(new DataPostboxSink(postbox1));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		// Determine which postbox should be written to.
		switch (instance) {
		case 0:
			return sortedEntityValidator0;
		case 1:
			return sortedEntityValidator1;
		default:
			throw new OsmosisRuntimeException("Sink instance " + instance
					+ " is not valid.");
		}
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
		try {
			EntityContainerComparator comparator;
			EntityContainer entityContainer0 = null;
			EntityContainer entityContainer1 = null;
			
			// Create a comparator for comparing two entities by type and identifier.
			comparator = new EntityContainerComparator(new EntityByTypeThenIdComparator());
			
			// We can't get meaningful data from the initialize data on the
			// input streams, so pass empty meta data to the sink and discard
			// the input meta data.
			postbox0.outputInitialize();
			postbox1.outputInitialize();
			sink.initialize(Collections.<String, Object>emptyMap());
			
			// BEGIN bound special handling
			
			// If there is a bound, it's going to be the first object 
			// in a properly sorted stream
			entityContainer0 = nextOrNull(postbox0);
			entityContainer1 = nextOrNull(postbox1);
					
			// There's only need for special processing if there actually is some data
			// on both streams - no data implies no bound
			if (entityContainer0 != null && entityContainer1 != null) {
				Bound bound0 = null;
				Bound bound1 = null;
				
				// If there are any bounds upstream, eat them up
				if (entityContainer0.getEntity().getType() == EntityType.Bound) {
					bound0 = (Bound) entityContainer0.getEntity();
					entityContainer0 = nextOrNull(postbox0);
				}
				if (entityContainer1.getEntity().getType() == EntityType.Bound) {
					bound1 = (Bound) entityContainer1.getEntity();
					entityContainer1 = nextOrNull(postbox1);
				}

				// Only post a bound downstream if both upstream sources had a bound.
				// (Otherwise there's either nothing to post or the posted bound is going
				// to be smaller than the actual data, which is bad)
				if (bound0 != null && bound1 != null) {
					sink.process(new BoundContainer(bound0.union(bound1)));
				} else if ((bound0 != null && bound1 == null)
						|| (bound0 == null && bound1 != null)) {
					handleBoundRemoved(bound0 == null);
				}
			}
			
			// END bound special handling
			
			// We continue in the comparison loop while both sources still have data.
			while (
					(entityContainer0 != null || postbox0.hasNext())
					&& (entityContainer1 != null || postbox1.hasNext())) {
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
						
						timestampComparisonResult =
							entityContainer0.getEntity().getTimestamp()
								.compareTo(entityContainer1.getEntity().getTimestamp());
						
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
					} else if (conflictResolutionMethod.equals(ConflictResolutionMethod.Version)) {
						int version0 = entityContainer0.getEntity().getVersion();
						int version1 = entityContainer1.getEntity().getVersion();
						if (version0 < version1) {
							sink.process(entityContainer1);
						} else if (version0 > version1) {
							sink.process(entityContainer0);
						} else {
							// If both have identical versions, use the second source.
							sink.process(entityContainer1);
						}

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
			
			postbox0.outputComplete();
			postbox1.outputComplete();
			
		} finally {
			sink.release();
			
			postbox0.outputRelease();
			postbox1.outputRelease();
		}
	}
		
	private void handleBoundRemoved(boolean source0BoundMissing) {
		
		if (boundRemovedAction == BoundRemovedAction.Ignore) {
			// Nothing to do
			return;
		}
		
		// Message for log or exception
		String missingSourceID, otherSourceID;
		
		if (source0BoundMissing) {
			missingSourceID = "0";
			otherSourceID = "1";
		} else {
			missingSourceID = "1";
			otherSourceID = "0";
		}
		
		String message = String.format(
				"Source %s of the merge task has an explicit bound set, but source %s has not. "
				+ "Therefore the explicit bound has been removed from the merged stream.", 
				missingSourceID, otherSourceID);
		
		// Now actually log or fail.
		if (boundRemovedAction == BoundRemovedAction.Warn) {
			LOG.warning(message);
		} else if (boundRemovedAction == BoundRemovedAction.Fail) {
			throw new OsmosisRuntimeException(message);
		}
	}


	private static EntityContainer nextOrNull(DataPostbox<EntityContainer> postbox) {

		if (postbox.hasNext()) {
			return postbox.getNext();
		}
		
		return null;
	}
}

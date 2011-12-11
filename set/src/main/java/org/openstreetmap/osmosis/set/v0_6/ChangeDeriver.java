// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.util.Collections;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.change.v0_6.impl.TimestampSetter;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityContainerComparator;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.MultiSinkRunnableChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.set.v0_6.impl.DataPostboxSink;


/**
 * Compares two different data sources and produces a set of differences.
 * 
 * @author Brett Henderson
 */
public class ChangeDeriver implements MultiSinkRunnableChangeSource {

	private ChangeSink changeSink;
	private DataPostbox<EntityContainer> fromPostbox;
	private DataPostboxSink fromSink;
	private DataPostbox<EntityContainer> toPostbox;
	private DataPostboxSink toSink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inputBufferCapacity
	 *            The size of the buffers to use for input sources.
	 */
	public ChangeDeriver(int inputBufferCapacity) {
		fromPostbox = new DataPostbox<EntityContainer>(inputBufferCapacity);
		fromSink = new DataPostboxSink(fromPostbox);
		toPostbox = new DataPostbox<EntityContainer>(inputBufferCapacity);
		toSink = new DataPostboxSink(toPostbox);
	}


	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		switch (instance) {
		case 0:
			return fromSink;
		case 1:
			return toSink;
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
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Processes the input sources and sends the changes to the change sink.
	 */
	public void run() {
		try {
			EntityContainerComparator comparator;
			EntityContainer fromEntityContainer = null;
			EntityContainer toEntityContainer = null;
			TimestampSetter timestampSetter;
			
			// Create a comparator for comparing two entities by type and identifier.
			comparator = new EntityContainerComparator(new EntityByTypeThenIdComparator());
			
			// Create an object for setting the current timestamp on entities being deleted.
			timestampSetter = new TimestampSetter();
			
			// We can't get meaningful data from the initialize data on the
			// input streams, so pass empty meta data to the sink and discard
			// the input meta data.
			fromPostbox.outputInitialize();
			toPostbox.outputInitialize();
			changeSink.initialize(Collections.<String, Object>emptyMap());
			
			// We continue in the comparison loop while both sources still have data.
			while (
					(fromEntityContainer != null || fromPostbox.hasNext())
					&& (toEntityContainer != null || toPostbox.hasNext())) {
				int comparisonResult;
				
				// Get the next input data where required.
				if (fromEntityContainer == null) {
					fromEntityContainer = fromPostbox.getNext();
				}
				if (toEntityContainer == null) {
					toEntityContainer = toPostbox.getNext();
				}
				
				// Compare the two sources.
				comparisonResult = comparator.compare(fromEntityContainer, toEntityContainer);
				
				if (comparisonResult < 0) {
					// The from entity doesn't exist on the to source therefore
					// has been deleted. We don't know when the entity was
					// deleted so set the delete time to the current time.
					changeSink.process(
							new ChangeContainer(
									timestampSetter.updateTimestamp(fromEntityContainer),
									ChangeAction.Delete));
					fromEntityContainer = null;
				} else if (comparisonResult > 0) {
					// The to entity doesn't exist on the from source therefore has
					// been created.
					changeSink.process(new ChangeContainer(toEntityContainer, ChangeAction.Create));
					toEntityContainer = null;
				} else {
					// The entity exists on both sources, therefore we must
					// compare
					// the entities directly. If there is a difference, the
					// entity has been modified.
					if (!fromEntityContainer.getEntity().equals(toEntityContainer.getEntity())) {
						changeSink.process(new ChangeContainer(toEntityContainer, ChangeAction.Modify));
					}
					fromEntityContainer = null;
					toEntityContainer = null;
				}
			}
			
			// Any remaining "from" entities are deletes.
			while (fromEntityContainer != null || fromPostbox.hasNext()) {
				if (fromEntityContainer == null) {
					fromEntityContainer = fromPostbox.getNext();
				}

				// The from entity doesn't exist on the to source therefore
				// has been deleted. We don't know when the entity was
				// deleted so set the delete time to the current time.
				changeSink.process(
						new ChangeContainer(
								timestampSetter.updateTimestamp(fromEntityContainer),
								ChangeAction.Delete));
				fromEntityContainer = null;
			}
			// Any remaining "to" entities are creates.
			while (toEntityContainer != null || toPostbox.hasNext()) {
				if (toEntityContainer == null) {
					toEntityContainer = toPostbox.getNext();
				}
				changeSink.process(new ChangeContainer(toEntityContainer, ChangeAction.Create));
				toEntityContainer = null;
			}
			
			changeSink.complete();
			fromPostbox.outputComplete();
			toPostbox.outputComplete();
			
		} finally {
			changeSink.release();
			
			fromPostbox.outputRelease();
			toPostbox.outputRelease();
		}
	}
}

// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;


/**
 * Validates that change data in a pipeline is sorted by entity type then id thus only allowing
 * delta style changes (ie. not full history). It accepts input data from a Source and passes all
 * data to a downstream Sink.
 * 
 * @author Brett Henderson
 */
public class SortedDeltaChangePipeValidator implements ChangeSinkChangeSource {
	private ChangeSink changeSink;
	private Comparator<ChangeContainer> comparator;
	private ChangeContainer previousChangeContainer;
	
	
	/**
	 * Creates a new instance.
	 */
	public SortedDeltaChangePipeValidator() {
		comparator = new ChangeAsEntityComparator(new EntityContainerComparator(new EntityByTypeThenIdComparator()));
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		changeSink.initialize(metaData);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		changeSink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		// If this is not the first entity in the pipeline, make sure this
		// entity is greater than the previous.
		if (previousChangeContainer != null) {
			if (comparator.compare(previousChangeContainer, changeContainer) >= 0) {
				throw new OsmosisRuntimeException(
					"Pipeline entities are not sorted or contain multiple versions of a single entity"
					+ ", previous entity type=" + previousChangeContainer.getEntityContainer().getEntity().getType()
					+ ", id=" + previousChangeContainer.getEntityContainer().getEntity().getId()
					+ ", version=" + previousChangeContainer.getEntityContainer().getEntity().getVersion()
					+ " current entity type=" + changeContainer.getEntityContainer().getEntity().getType()
					+ ", id=" + changeContainer.getEntityContainer().getEntity().getId() 
					+ ", version=" + changeContainer.getEntityContainer().getEntity().getVersion() + "."
				);
			}
		}
		
		changeSink.process(changeContainer);
		
		previousChangeContainer = changeContainer;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		changeSink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
}

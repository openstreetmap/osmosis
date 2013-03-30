// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.tee.v0_6;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkMultiSource;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;
import org.openstreetmap.osmosis.core.task.v0_6.Source;


/**
 * Sends input data to two output destinations.
 * 
 * @author Brett Henderson
 */
public class EntityTee implements SinkMultiSource {
	
	private List<ProxySinkSource> sinkList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param outputCount
	 *            The number of output destinations to write to.
	 */
	public EntityTee(int outputCount) {
		sinkList = new ArrayList<ProxySinkSource>();
		
		for (int i = 0; i < outputCount; i++) {
			sinkList.add(new ProxySinkSource());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Source getSource(int index) {
		if (index < 0 || index >= sinkList.size()) {
			throw new OsmosisRuntimeException("Source index " + index
					+ " is in the range 0 to " + (sinkList.size() - 1) + ".");
		}
		
		return sinkList.get(index);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int getSourceCount() {
		return sinkList.size();
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		for (ProxySinkSource sink : sinkList) {
			sink.initialize(metaData);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		for (ProxySinkSource sink : sinkList) {
			// We're passing the data to multiple downstream tasks therefore should make the entity
			// read-only to prevent multiple threads impacting each other.
			entityContainer.getEntity().makeReadOnly();
			
			sink.process(entityContainer);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		for (ProxySinkSource sink : sinkList) {
			sink.complete();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		for (ProxySinkSource sink : sinkList) {
			sink.release();
		}
	}
	
	
	/**
	 * Instances of this class are returned via the parent class getSource method.
	 * 
	 * @author Brett Henderson
	 */
	private static class ProxySinkSource implements SinkSource {
		private Sink sink;
		
		
		/**
		 * Creates a new instance.
		 */
		public ProxySinkSource() {
			// Nothing to do.
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
		public void initialize(Map<String, Object> metaData) {
			sink.initialize(metaData);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(EntityContainer entityContainer) {
			sink.process(entityContainer);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void complete() {
			sink.complete();
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void release() {
			sink.release();
		}		
	}
}

package com.bretth.osmosis.core.tee.v0_5;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.task.v0_5.SinkMultiSource;
import com.bretth.osmosis.core.task.v0_5.SinkSource;
import com.bretth.osmosis.core.task.v0_5.Source;


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
	public void process(EntityContainer entityContainer) {
		for (ProxySinkSource sink : sinkList) {
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

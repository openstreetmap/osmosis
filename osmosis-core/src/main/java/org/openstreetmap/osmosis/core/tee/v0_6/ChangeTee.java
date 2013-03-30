// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.tee.v0_6;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkMultiChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSource;


/**
 * Sends input change data to two output destinations.
 * 
 * @author Brett Henderson
 */
public class ChangeTee implements ChangeSinkMultiChangeSource {
	
	private List<ProxyChangeSinkChangeSource> sinkList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param outputCount
	 *            The number of output destinations to write to.
	 */
	public ChangeTee(int outputCount) {
		sinkList = new ArrayList<ProxyChangeSinkChangeSource>();
		
		for (int i = 0; i < outputCount; i++) {
			sinkList.add(new ProxyChangeSinkChangeSource());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ChangeSource getChangeSource(int index) {
		if (index < 0 || index >= sinkList.size()) {
			throw new OsmosisRuntimeException("Source index " + index
					+ " is in the range 0 to " + (sinkList.size() - 1) + ".");
		}
		
		return sinkList.get(index);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int getChangeSourceCount() {
		return sinkList.size();
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		for (ProxyChangeSinkChangeSource sink : sinkList) {
			sink.initialize(metaData);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
		for (ProxyChangeSinkChangeSource sink : sinkList) {
			// We're passing the data to multiple downstream tasks therefore should make the entity
			// read-only to prevent multiple threads impacting each other.
			change.getEntityContainer().getEntity().makeReadOnly();
			
			sink.process(change);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		for (ProxyChangeSinkChangeSource sink : sinkList) {
			sink.complete();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		for (ProxyChangeSinkChangeSource sink : sinkList) {
			sink.release();
		}
	}
	
	
	/**
	 * Instances of this class are returned via the parent class getSource method.
	 * 
	 * @author Brett Henderson
	 */
	private static class ProxyChangeSinkChangeSource implements ChangeSinkChangeSource {
		private ChangeSink changeSink;
		
		
		/**
		 * Creates a new instance.
		 */
		public ProxyChangeSinkChangeSource() {
			// Nothing to do.
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
		public void initialize(Map<String, Object> metaData) {
			changeSink.initialize(metaData);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(ChangeContainer change) {
			changeSink.process(change);
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
		public void release() {
			changeSink.release();
		}
	}
}

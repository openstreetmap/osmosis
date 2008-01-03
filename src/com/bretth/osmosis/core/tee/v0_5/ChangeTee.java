// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.tee.v0_5;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.ChangeContainer;
import com.bretth.osmosis.core.task.v0_5.ChangeSink;
import com.bretth.osmosis.core.task.v0_5.ChangeSinkChangeSource;
import com.bretth.osmosis.core.task.v0_5.ChangeSinkMultiChangeSource;
import com.bretth.osmosis.core.task.v0_5.ChangeSource;


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
	public void process(ChangeContainer change) {
		for (ProxyChangeSinkChangeSource sink : sinkList) {
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

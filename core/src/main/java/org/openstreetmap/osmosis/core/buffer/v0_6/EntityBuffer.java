// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.buffer.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkRunnableSource;


/**
 * Splits the pipeline so that it can be processed on multiple threads. The
 * input thread to this task stores data in a buffer which blocks if it fills
 * up. This task runs on a new thread which reads data from the buffer and
 * writes it to the destination.
 * 
 * @author Brett Henderson
 */
public class EntityBuffer implements SinkRunnableSource {
	private Sink sink;
	private DataPostbox<EntityContainer> buffer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param bufferCapacity
	 *            The size of the buffer to use.
	 */
	public EntityBuffer(int bufferCapacity) {
		buffer = new DataPostbox<EntityContainer>(bufferCapacity);
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		buffer.initialize(metaData);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		buffer.put(entityContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		buffer.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		buffer.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * Sends all input data to the sink.
	 */
	public void run() {
		try {
			sink.initialize(buffer.outputInitialize());
			
			while (buffer.hasNext()) {
				sink.process(buffer.getNext());
			}
			
			sink.complete();
			buffer.outputComplete();
			
		} finally {
			sink.release();
			buffer.outputRelease();
		}
	}
}

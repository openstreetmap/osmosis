package com.bretth.osmosis.core.buffer.v0_4;

import com.bretth.osmosis.core.container.v0_4.EntityContainer;
import com.bretth.osmosis.core.store.DataPostbox;
import com.bretth.osmosis.core.task.v0_4.Sink;
import com.bretth.osmosis.core.task.v0_4.SinkRunnableSource;


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
		boolean completed = false;
		
		try {
			while (buffer.hasNext()) {
				sink.process(buffer.getNext());
			}
			
			sink.complete();
			completed = true;
			
		} finally {
			if (!completed) {
				buffer.setOutputError();
			}
			
			sink.release();
		}
	}
	
}

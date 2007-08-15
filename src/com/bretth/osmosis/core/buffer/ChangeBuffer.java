package com.bretth.osmosis.core.buffer;

import com.bretth.osmosis.core.change.impl.DataPostbox;
import com.bretth.osmosis.core.container.ChangeContainer;
import com.bretth.osmosis.core.task.ChangeSink;
import com.bretth.osmosis.core.task.ChangeSinkRunnableChangeSource;


/**
 * Splits the pipeline so that it can be processed on multiple threads. The
 * input thread to this task stores data in a buffer which blocks if it fills
 * up. This task runs on a new thread which reads data from the buffer and
 * writes it to the destination.
 * 
 * @author Brett Henderson
 */
public class ChangeBuffer implements ChangeSinkRunnableChangeSource {
	private ChangeSink changeSink;
	private DataPostbox<ChangeContainer> buffer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param bufferCapacity
	 *            The size of the buffer to use.
	 */
	public ChangeBuffer(int bufferCapacity) {
		buffer = new DataPostbox<ChangeContainer>(bufferCapacity);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		buffer.put(changeContainer);
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
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Sends all input data to the sink.
	 */
	public void run() {
		boolean completed = false;
		
		try {
			while (buffer.hasNext()) {
				changeSink.process(buffer.getNext());
			}
			
			changeSink.complete();
			completed = true;
			
		} finally {
			if (!completed) {
				buffer.setOutputError();
			}
			
			changeSink.release();
		}
	}
	
}

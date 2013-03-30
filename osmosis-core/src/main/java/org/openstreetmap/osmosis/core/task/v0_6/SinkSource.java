// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;


/**
 * Defines the interface for combining sink and source functionality. This is
 * typically used by classes performing some form of translation on an input
 * source before sending along to the output. This includes filtering tasks and
 * modification tasks.
 * 
 * @author Brett Henderson
 */
public interface SinkSource extends Sink, Source {
	// Interface only combines functionality of its extended interfaces.
}

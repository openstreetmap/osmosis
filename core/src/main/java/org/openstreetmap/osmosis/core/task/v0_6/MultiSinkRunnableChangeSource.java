// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;


/**
 * Defines the interface for combining multi sink and change source
 * functionality. This is typically used by classes performing difference
 * analysis of two data sources.
 * 
 * @author Brett Henderson
 */
public interface MultiSinkRunnableChangeSource extends MultiSink, RunnableChangeSource {
	// Interface only combines functionality of its extended interfaces.
}

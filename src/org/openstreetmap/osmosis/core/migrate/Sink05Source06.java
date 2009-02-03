// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.migrate;

import org.openstreetmap.osmosis.core.task.v0_5.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.Source;


/**
 * A SinkSource interface for converting from 0.5 to 0.6 format data.
 * 
 * @author Brett Henderson
 */
public interface Sink05Source06 extends Sink, Source {
	// Interface only combines functionality of its extended interfaces.
}

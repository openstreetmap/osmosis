package com.bretth.osmosis.core.migrate;

import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.task.v0_6.Source;


/**
 * A SinkSource interface for converting from 0.5 to 0.6 format data.
 * 
 * @author Brett Henderson
 */
public interface Sink05Source06 extends Sink, Source {
	// Interface only combines functionality of its extended interfaces.
}

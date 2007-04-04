package com.bretth.osm.transformer.xml.impl;

import java.util.Date;

import com.bretth.osm.transformer.pipeline.OsmSink;


public abstract class BaseElementProcessor implements ElementProcessor {
	private BaseElementProcessor parentProcessor;
	private ElementProcessor dummyChildProcessor;
	//private DateFormat dateFormat;
	
	
	protected BaseElementProcessor(BaseElementProcessor parentProcessor) {
		this.parentProcessor = parentProcessor;
		
		//dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	
	protected OsmSink getOsmSink() {
		return parentProcessor.getOsmSink();
	}
	
	
	protected ElementProcessor getDummyChildProcessor() {
		if (dummyChildProcessor == null) {
			dummyChildProcessor = new DummyElementProcessor(this);
		}
		
		return dummyChildProcessor;
	}
	
	
	public ElementProcessor getParent() {
		return parentProcessor;
	}
	
	
	protected Date parseTimestamp(String data) {
		//try {
			// TODO: Fix date format so it doesn't break on the planet file.
			// TODO: Fix the timezones so that it treats value as GMT.
			//return dateFormat.parse(data);
			return new Date();
			
		//} catch (ParseException e) {
		//	throw new OsmLoaderRuntimeException("Unable to parse date from data (" + data + ")");
		//}
	}
}

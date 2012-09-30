// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

import java.util.Collection;
import java.util.Map;

import uk.co.randomjunk.osmosis.transform.Match;
import uk.co.randomjunk.osmosis.transform.Output;

public class CopyAll implements Output {

	@Override
	public void apply(Map<String, String> originalTags,
			Map<String, String> tags, Collection<Match> matches) {
		// copy all tags
		tags.putAll(originalTags);
	}

}

// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.co.randomjunk.osmosis.transform.Match;
import uk.co.randomjunk.osmosis.transform.Output;

public class CopyUnmatched implements Output {

	@Override
	public void apply(Map<String, String> originalTags,
			Map<String, String> tags, Collection<Match> matches) {
		// copy the original, then remove the matches
		Map<String, String> toCopy = new HashMap<String, String>(originalTags);
		for ( Match match : matches ) {
			if ( match.getKeyGroupCount() > 0 )
				toCopy.remove(match.getKey(0));
		}
		// apply the copy
		tags.putAll(toCopy);
	}

}

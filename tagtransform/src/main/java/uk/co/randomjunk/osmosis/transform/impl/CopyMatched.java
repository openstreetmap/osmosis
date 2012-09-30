// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

import java.util.Collection;
import java.util.Map;

import uk.co.randomjunk.osmosis.transform.Match;
import uk.co.randomjunk.osmosis.transform.Output;

public class CopyMatched implements Output {

	@Override
	public void apply(Map<String, String> originalTags,
			Map<String, String> tags, Collection<Match> matches) {
		// put any matches directly
		for ( Match match : matches ) {
			if ( match.getKeyGroupCount() > 0 )
				tags.put(match.getKey(0), match.getValue(0));
		}
	}

}

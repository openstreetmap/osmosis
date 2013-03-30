// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.Collection;
import java.util.Map;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Output;


public class CopyMatched implements Output {

	@Override
	public void apply(Map<String, String> originalTags, Map<String, String> tags, Collection<Match> matches) {
		// put any matches directly
		for (Match match : matches) {
			if (match.getKeyGroupCount() > 0) {
				tags.put(match.getKey(0), match.getValue(0));
			}
		}
	}

}

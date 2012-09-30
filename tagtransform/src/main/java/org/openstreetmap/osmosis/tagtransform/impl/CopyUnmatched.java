// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Output;


public class CopyUnmatched implements Output {

	@Override
	public void apply(Map<String, String> originalTags, Map<String, String> tags, Collection<Match> matches) {
		// copy the original, then remove the matches
		Map<String, String> toCopy = new HashMap<String, String>(originalTags);
		for (Match match : matches) {
			if (match.getKeyGroupCount() > 0) {
				toCopy.remove(match.getKey(0));
			}
		}
		// apply the copy
		tags.putAll(toCopy);
	}

}

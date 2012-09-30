// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform;

import java.util.Collection;
import java.util.Map;

public interface Output {

	public void apply(Map<String, String> originalTags,
			Map<String, String> tags, Collection<Match> matches);
	
}

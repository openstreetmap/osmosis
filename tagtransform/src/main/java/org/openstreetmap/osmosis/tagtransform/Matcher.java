// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package org.openstreetmap.osmosis.tagtransform;

import java.util.Collection;
import java.util.Map;


public interface Matcher {

	public Collection<Match> match(Map<String, String> tags, TTEntityType type, String uname, int uid);

	public void outputStats(StringBuilder output, String indent);
	
}

// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import java.util.Collection;
import java.util.Map;


public interface Matcher {

	Collection<Match> match(Map<String, String> tags, TTEntityType type, String uname, int uid);


	void outputStats(StringBuilder output, String indent);

}

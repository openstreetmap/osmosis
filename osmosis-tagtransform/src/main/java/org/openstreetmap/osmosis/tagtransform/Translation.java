// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import java.util.Collection;
import java.util.Map;


public interface Translation {

	Collection<Match> match(Map<String, String> tags, TTEntityType entityType, String uname, int uid);


	boolean isDropOnMatch();


	Collection<Output> getOutputs();


	void outputStats(StringBuilder output, String indent);

}

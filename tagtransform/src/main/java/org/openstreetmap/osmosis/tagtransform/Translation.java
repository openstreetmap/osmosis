// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import java.util.Collection;
import java.util.Map;


public interface Translation {

	public Collection<Match> match(Map<String, String> tags, TTEntityType entityType, String uname, int uid);
	public boolean isDropOnMatch();
	public Collection<Output> getOutputs();
	public void outputStats(StringBuilder output, String indent);

}

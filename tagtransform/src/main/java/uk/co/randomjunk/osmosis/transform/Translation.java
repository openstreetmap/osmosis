// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform;

import java.util.Collection;
import java.util.Map;


public interface Translation {

	public Collection<Match> match(Map<String, String> tags, TTEntityType entityType, String uname, int uid);
	public boolean isDropOnMatch();
	public Collection<Output> getOutputs();
	public void outputStats(StringBuilder output, String indent);

}

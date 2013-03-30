// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Matcher;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;


public class OrMatcher implements Matcher {

	private Collection<Matcher> matchers;
	private long matchHits = 0;
	private TTEntityType type;
	private String uname;
	private int uid;


	public OrMatcher(Collection<Matcher> matchers, TTEntityType type, String uname, int uid) {
		this.matchers = matchers;
		this.type = type;
		this.uname = uname;
		this.uid = uid;
	}


	@Override
	public Collection<Match> match(Map<String, String> tags, TTEntityType entityType, String entityUname,
			int entityUid) {
		if (this.type != null && this.type != entityType) {
			return null;
		}
		if (this.uname != null && !this.uname.equals(entityUname)) {
			return null;
		}
		if (this.uid != 0 && this.uid != entityUid) {
			return null;
		}

		List<Match> allMatches = new ArrayList<Match>();
		for (Matcher matcher : matchers) {
			Collection<Match> matches = matcher.match(tags, entityType, entityUname, entityUid);
			if (matches != null) {
				allMatches.addAll(matches);
			}
		}
		if (!allMatches.isEmpty()) {
			matchHits++;
		}
		return allMatches;
	}


	@Override
	public void outputStats(StringBuilder output, String indent) {
		output.append(indent);
		output.append("Or: ");
		output.append(matchHits);
		output.append('\n');
		for (Matcher matcher : matchers) {
			matcher.outputStats(output, indent + "    ");
		}
	}

}

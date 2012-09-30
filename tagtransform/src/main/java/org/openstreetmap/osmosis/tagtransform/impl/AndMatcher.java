// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Matcher;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;


public class AndMatcher implements Matcher {

	private Collection<Matcher> matchers;
	private long matchHits = 0;
	private TTEntityType type;
	private String uname;
	private int uid;

	public AndMatcher(Collection<Matcher> matchers, TTEntityType type, String uname, int uid) {
		this.matchers = matchers;
		this.type = type;
		this.uname = uname;
		this.uid = uid;
	}
	
	@Override
	public Collection<Match> match(Map<String, String> tags, TTEntityType type, String uname, int uid) {
		if ( this.type != null && this.type != type )
			return null;
		if ( this.uname != null && ! this.uname.equals(uname) )
			return null;
		if ( this.uid != 0 && this.uid != uid )
			return null;
		
		List<Match> allMatches = new ArrayList<Match>();
		for ( Matcher matcher : matchers ) {
			Collection<Match> matches = matcher.match(tags, type, uname, uid);
			if ( matches == null || matches.isEmpty() )
				return null;
			allMatches.addAll(matches);
		}
		matchHits++;
		return allMatches;
	}

	@Override
	public void outputStats(StringBuilder output, String indent) {
		output.append(indent);
		output.append("And: ");
		output.append(matchHits);
		output.append('\n');
		for ( Matcher matcher : matchers )
			matcher.outputStats(output, indent+"    ");
	}
}

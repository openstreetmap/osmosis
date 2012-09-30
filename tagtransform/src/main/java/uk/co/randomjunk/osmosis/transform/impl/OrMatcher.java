// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.co.randomjunk.osmosis.transform.Match;
import uk.co.randomjunk.osmosis.transform.Matcher;
import uk.co.randomjunk.osmosis.transform.TTEntityType;

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
			if ( matches != null )
				allMatches.addAll(matches);
		}
		if ( !allMatches.isEmpty() )
			matchHits ++;
		return allMatches;
	}

	@Override
	public void outputStats(StringBuilder output, String indent) {
		output.append(indent);
		output.append("Or: ");
		output.append(matchHits);
		output.append('\n');
		for ( Matcher matcher : matchers )
			matcher.outputStats(output, indent+"    ");
	}

}

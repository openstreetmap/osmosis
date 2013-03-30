// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Matcher;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;


public class TagMatcher implements Matcher {

	private String matchID;
	private Pattern keyPattern;
	private Pattern valuePattern;
	private long matchHits = 0;


	public TagMatcher(String matchID, String keyPattern, String valuePattern) {
		this.matchID = matchID;
		this.keyPattern = Pattern.compile(keyPattern);
		this.valuePattern = Pattern.compile(valuePattern);
	}


	@Override
	public Collection<Match> match(Map<String, String> tags, TTEntityType type, String uname, int uid) {
		List<Match> matches = new ArrayList<Match>();

		// loop through the tags to find matches
		for (Entry<String, String> tag : tags.entrySet()) {
			java.util.regex.Matcher keyMatch = keyPattern.matcher(tag.getKey());
			java.util.regex.Matcher valueMatch = valuePattern.matcher(tag.getValue());
			if (keyMatch.matches() && valueMatch.matches()) {
				MatchResult keyRes = keyMatch.toMatchResult();
				MatchResult valueRes = valueMatch.toMatchResult();
				matches.add(new MatchResultMatch(matchID, keyRes, valueRes));
			}
		}

		matchHits += matches.size();
		return matches;
	}


	@Override
	public void outputStats(StringBuilder output, String indent) {
		output.append(indent);
		output.append("Tag[");
		if (matchID != null) {
			output.append(matchID);
			output.append(",");
		}
		output.append(keyPattern.pattern());
		output.append(",");
		output.append(valuePattern.pattern());
		output.append("]: ");
		output.append(matchHits);
		output.append('\n');
	}

}

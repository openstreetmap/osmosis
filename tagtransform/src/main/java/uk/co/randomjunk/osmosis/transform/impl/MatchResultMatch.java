// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

import java.util.regex.MatchResult;

import uk.co.randomjunk.osmosis.transform.Match;

public class MatchResultMatch implements Match {

	private MatchResult keyRes;
	private MatchResult valueRes;
	private String matchID;

	public MatchResultMatch(String matchID, MatchResult keyRes, MatchResult valueRes) {
		this.matchID = matchID;
		this.keyRes = keyRes;
		this.valueRes = valueRes;
	}

	@Override
	public String getKey(int group) {
		return keyRes.group(group);
	}

	@Override
	public int getKeyGroupCount() {
		return keyRes.groupCount() + 1;
	}

	@Override
	public String getMatchID() {
		return matchID;
	}

	@Override
	public String getValue(int group) {
		return valueRes.group(group);
	}

	@Override
	public int getValueGroupCount() {
		return valueRes.groupCount() + 1;
	}

}

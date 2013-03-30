// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.regex.MatchResult;

import org.openstreetmap.osmosis.tagtransform.Match;


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

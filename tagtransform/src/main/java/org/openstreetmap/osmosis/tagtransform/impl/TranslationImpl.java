// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Matcher;
import org.openstreetmap.osmosis.tagtransform.Output;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;
import org.openstreetmap.osmosis.tagtransform.Translation;


public class TranslationImpl implements Translation {

	private String name;
	private String description;
	private Matcher matcher;
	private List<Output> output;
	private Matcher finder;


	public TranslationImpl(String name, String description, Matcher matcher, Matcher finder, List<Output> output) {
		this.name = name;
		this.description = description;
		this.matcher = matcher;
		this.finder = finder;
		this.output = output;
	}


	@Override
	public Collection<Output> getOutputs() {
		return output;
	}


	@Override
	public boolean isDropOnMatch() {
		return output.isEmpty();
	}


	@Override
	public Collection<Match> match(Map<String, String> tags, TTEntityType type, String uname, int uid) {
		Collection<Match> matches = matcher.match(tags, type, uname, uid);
		if (matches != null && !matches.isEmpty()) {
			Collection<Match> finds;
			if (finder == null) {
				finds = null;
			} else {
				finds = finder.match(tags, type, uname, uid);
			}
			if (finds != null && !finds.isEmpty()) {
				if (matches instanceof ArrayList) {
					matches.addAll(finds);
				} else {
					List<Match> allMatches = new ArrayList<Match>();
					allMatches.addAll(matches);
					allMatches.addAll(finds);
					return allMatches;
				}
			}

			return matches;
		}

		return null;
	}


	@Override
	public void outputStats(StringBuilder statsOutput, String indent) {
		statsOutput.append(indent);
		statsOutput.append(name);
		statsOutput.append(":");
		statsOutput.append('\n');
		if (description != null && !description.isEmpty()) {
			statsOutput.append(description);
			statsOutput.append('\n');
		}
		matcher.outputStats(statsOutput, indent + "    ");
		if (finder != null) {
			finder.outputStats(statsOutput, "  + ");
		}
		statsOutput.append('\n');
	}

}

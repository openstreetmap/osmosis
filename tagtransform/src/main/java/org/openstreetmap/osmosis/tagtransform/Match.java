// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

public interface Match {

	public String getMatchID();
	public String getKey(int group);
	public String getValue(int group);
	public int getKeyGroupCount();
	public int getValueGroupCount();
	
}

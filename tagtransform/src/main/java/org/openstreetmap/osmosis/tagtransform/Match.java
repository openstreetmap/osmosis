// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

public interface Match {

	String getMatchID();


	String getKey(int group);


	String getValue(int group);


	int getKeyGroupCount();


	int getValueGroupCount();

}

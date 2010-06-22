// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Extends the basic task manager factory functionality with TagFilter task
 * specific common methods.
 *
 * @author Andrew Byrd
 */
public class TagFilterFactory extends TaskManagerFactory {
    
    
    /**
     * Decodes escaped wildcard, separator, equals, and space characters.
     *
     * @param s
     *      the String to decode.
     */
    private String unEscape(String s) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (char c : s.toCharArray()) {
            if (escaped) {
                switch (c) {                
                    case '%': sb.append('%'); break;
                    case 'a': sb.append('*'); break;
                    case 'c': sb.append(','); break; 
                    case 'e': sb.append('='); break;
                    case 's': sb.append(' '); break;
                    default : break;
                }
                escaped = false;
            } else {
                if (c == '%') {
                    escaped = true;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
        // Iterate over keyword arguments and fetch them through the appropriate TaskManagerFactory utility method
        // to avoid 'Argument was not recognized' exceptions from Osmosis
        Set<String> keys = new HashSet<String>();
        Map<String, Set<String>> keyValues = new HashMap<String, Set<String>>(); 
        for (String key : taskConfig.getConfigArgs().keySet()) {
            String value = getStringArgument(taskConfig, key);
            if (value.equals("*")) {
                keys.add(unEscape(key));
            } else {
                Set<String> values = new HashSet<String>();
                for (String v : value.split(",")) {
                    values.add(unEscape(v));
                }
                keyValues.put(unEscape(key), values);
            }
        }
        return new SinkSourceManager(
            taskConfig.getId(),
            new TagFilter(getDefaultStringArgument(taskConfig, ""), keys, keyValues),
            taskConfig.getPipeArgs()
        );
    }

}

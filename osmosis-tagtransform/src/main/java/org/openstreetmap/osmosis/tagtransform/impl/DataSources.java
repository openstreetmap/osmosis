// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.io.File;
import java.util.function.BiFunction;
import org.w3c.dom.NamedNodeMap;

import org.openstreetmap.osmosis.tagtransform.DataSource;

/**
 *
 * @author fwiesweg
 */
public enum DataSources {
        CSV(DataSourceCSV::new);

        private final BiFunction<File, NamedNodeMap, DataSource> factory;

        private DataSources(BiFunction<File, NamedNodeMap, DataSource> factory) {
                this.factory = factory;
        }

        public DataSource create(File parentDir, NamedNodeMap attributes) {
                return this.factory.apply(parentDir, attributes);
        }
}

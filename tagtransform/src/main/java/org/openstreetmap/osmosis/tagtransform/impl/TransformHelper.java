// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.common.Task;
import org.openstreetmap.osmosis.core.task.v0_6.Initializable;
import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Output;
import org.openstreetmap.osmosis.tagtransform.StatsSaveException;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;
import org.openstreetmap.osmosis.tagtransform.Translation;
import org.openstreetmap.osmosis.xml.common.XmlTimestampFormat;


/**
 * Class is intended to provide utility place for tag transform functionality. <br/>
 * See {@link org.openstreetmap.osmosis.tagtransform.v0_6.TransformTask
 * TransformTask} for example implementation.
 * 
 * @author apopov
 * 
 * @param <T>
 *            is a sink type.
 */
public abstract class TransformHelper<T extends Task & Initializable> implements Initializable {
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	protected T sink;
	protected String statsFile;
	protected String configFile;
	protected List<Translation> translations;
	protected static TimestampFormat timestampFormat = new XmlTimestampFormat();


	public TransformHelper(String configFile, String statsFile) {
		logger.log(Level.FINE, "Transform configured with " + configFile + " and " + statsFile);
		translations = new TransformLoader().load(configFile);
		this.statsFile = statsFile;
		this.configFile = configFile;
	}


	@Override
	public void initialize(Map<String, Object> metaData) {
		sink.initialize(metaData);
	}


	public void setSink(T sink) {
		this.sink = sink;
	}


	@Override
	public void complete() {
		if (statsFile != null && !statsFile.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			builder.append(configFile);
			builder.append("\n\n");
			for (Translation t : translations) {
				t.outputStats(builder, "");
			}

			Writer writer = null;
			try {
				writer = new FileWriter(new File(statsFile));
				writer.write(builder.toString());
			} catch (IOException e) {
				throw new StatsSaveException("Failed to save stats: " + e.getLocalizedMessage(), e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						logger.log(Level.WARNING, "Unable to close stats file " + statsFile + ".", e);
					}
				}
			}
		}
		sink.complete();
	}


	@Override
	public void release() {
		sink.release();
	}


	/**
	 * Transforms entity container according to configFile.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 * @return transformed (if needed) entityContainer
	 */
	protected EntityContainer processEntityContainer(EntityContainer entityContainer) {
		// Entities may have been made read-only at some point in the pipeline.
		// We want a writeable instance so that we can update the tags.
		EntityContainer writeableEntityContainer = entityContainer.getWriteableInstance();
		Entity entity = entityContainer.getEntity();
		Collection<Tag> entityTags = entity.getTags();
		EntityType entityType = entity.getType();

		// Store the tags in a map keyed by tag key.
		Map<String, String> tagMap = new HashMap<String, String>();
		for (Tag tag : entity.getTags()) {
			tagMap.put(tag.getKey(), tag.getValue());
		}

		// Apply tag transformations.
		for (Translation translation : translations) {
			Collection<Match> matches = translation.match(tagMap, TTEntityType.fromEntityType06(entityType), entity
					.getUser().getName(), entity.getUser().getId());
			if (matches == null || matches.isEmpty()) {
				continue;
			}
			if (translation.isDropOnMatch()) {
				return null;
			}

			Map<String, String> newTags = new HashMap<String, String>();
			for (Output output : translation.getOutputs()) {
				output.apply(tagMap, newTags, matches);
			}
			tagMap = newTags;
		}

		// Replace the entity tags with the transformed values.
		entityTags.clear();
		for (Entry<String, String> tag : tagMap.entrySet()) {
			entityTags.add(new Tag(tag.getKey(), tag.getValue()));
		}

		return writeableEntityContainer;
	}
}

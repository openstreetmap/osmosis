// This software is released into the Public Domain.  See copying.txt for details.
package crosby.binary.osmosis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import crosby.binary.BinarySerializer;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseInfo;
import crosby.binary.StringTable;
import crosby.binary.Osmformat.Relation.MemberType;
import crosby.binary.file.BlockOutputStream;
import crosby.binary.file.FileBlock;

/**
 * Receives data from the Osmosis pipeline and stores it in the PBF format.
 */
public class OsmosisSerializer extends BinarySerializer implements Sink {
	private static final Logger LOG = Logger.getLogger(OsmosisSerializer.class.getName());
	
  /** Additional configuration flag for whether to serialize into DenseNodes/DenseInfo? */
  protected boolean useDense = true;

  /** Has the header been written yet? */
  protected boolean headerWritten = false;
  
  /**
   * Tracks the number of warnings that have occurred during serialisation.
   */
  static int warncount = 0;

	/**
	 * Construct a serializer that writes to the target BlockOutputStream.
	 * 
	 * @param output
	 *            The PBF block stream to send serialized data.
	 */
  public OsmosisSerializer(BlockOutputStream output) {
	  super(output);
  }

  /**
	 * Change the flag of whether to use the dense format.
	 * 
	 * @param useDense
	 *            The new use dense value.
	 */
  public void setUseDense(boolean useDense) {
    this.useDense = useDense;
  }

  /** Base class containing common code needed for serializing each type of primitives. */
    private abstract class Prim<T extends Entity> {
      /** Queue that tracks the list of all primitives. */
      ArrayList<T> contents = new ArrayList<T>();

      /** Add to the queue.
       * @param item The entity to add */
        public void add(T item) {
            contents.add(item);
        }

        /** Add all of the tags of all entities in the queue to the stringtable. */
        public void addStringsToStringtable() {
            StringTable stable = getStringTable();
            for (T i : contents) {
                Collection<Tag> tags = i.getTags();
                for (Tag tag : tags) {
                    stable.incr(tag.getKey());
                    stable.incr(tag.getValue());
                }
                if (!omit_metadata) {
                    stable.incr(i.getUser().getName());
                }
            }
        }
        private static final int MAXWARN = 100;
        public void serializeMetadataDense(DenseInfo.Builder b, List<? extends Entity> entities) {
			if (omit_metadata) {
				return;
			}

			long lasttimestamp = 0, lastchangeset = 0;
			int lastuserSid = 0, lastuid = 0;
			StringTable stable = getStringTable();
			for (Entity e : entities) {

            if (e.getUser() == OsmUser.NONE && warncount  < MAXWARN) {
              LOG.warning("Attention: Data being output lacks metadata. Please use omitmetadata=true");
              warncount++;
            }
				int uid = e.getUser().getId();
				int userSid = stable.getIndex(e.getUser().getName());
				int timestamp = (int) (e.getTimestamp().getTime() / date_granularity);
				int version = e.getVersion();
				long changeset = e.getChangesetId();

				b.addVersion(version);
				b.addTimestamp(timestamp - lasttimestamp);
				lasttimestamp = timestamp;
				b.addChangeset(changeset - lastchangeset);
				lastchangeset = changeset;
				b.addUid(uid - lastuid);
				lastuid = uid;
				b.addUserSid(userSid - lastuserSid);
				lastuserSid = userSid;
			}
        }
         
        public Osmformat.Info.Builder serializeMetadata(Entity e) {
            StringTable stable = getStringTable();
            Osmformat.Info.Builder b = Osmformat.Info.newBuilder();
            if (!omit_metadata) {
                if (e.getUser() == OsmUser.NONE && warncount  < MAXWARN) {
                  LOG.warning("Attention: Data being output lacks metadata. Please use omitmetadata=true");
                  warncount++;
                }
                if (e.getUser() != OsmUser.NONE) {
                    b.setUid(e.getUser().getId());
                    b.setUserSid(stable.getIndex(e.getUser().getName()));
                }
                b.setTimestamp((int) (e.getTimestamp().getTime() / date_granularity));
                b.setVersion(e.getVersion());
                b.setChangeset(e.getChangesetId());
            }
            return b;
        }
    }

    private class NodeGroup extends Prim<Node> implements PrimGroupWriterInterface {

      public Osmformat.PrimitiveGroup serialize() {
          if (useDense) {
            return serializeDense();
          } else {
            return serializeNonDense();
          }
      }
        
        /**
         *  Serialize all nodes in the 'dense' format.
         */
        public Osmformat.PrimitiveGroup serializeDense() {
            if (contents.size() == 0) {
              return null;
            }
            // System.out.format("%d Dense   ",nodes.size());
            Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
                    .newBuilder();
            StringTable stable = getStringTable();

            long lastlat = 0, lastlon = 0, lastid = 0;
            Osmformat.DenseNodes.Builder bi = Osmformat.DenseNodes.newBuilder();
            boolean doesBlockHaveTags = false;
            // Does anything in this block have tags?
            for (Node i : contents) {
              doesBlockHaveTags = doesBlockHaveTags || (!i.getTags().isEmpty());
            }
            if (!omit_metadata) {
              Osmformat.DenseInfo.Builder bdi = Osmformat.DenseInfo.newBuilder();
              serializeMetadataDense(bdi, contents);
              bi.setDenseinfo(bdi);
            }
              
              for (Node i : contents) {
                long id = i.getId();
                int lat = mapDegrees(i.getLatitude());
                int lon = mapDegrees(i.getLongitude());
                bi.addId(id - lastid);
                lastid = id;
                bi.addLon(lon - lastlon);
                lastlon = lon;
                bi.addLat(lat - lastlat);
                lastlat = lat;

                // Then we must include tag information.
                if (doesBlockHaveTags) {
                  for (Tag t : i.getTags()) {
                      bi.addKeysVals(stable.getIndex(t.getKey()));
                      bi.addKeysVals(stable.getIndex(t.getValue()));
                  }
                  bi.addKeysVals(0); // Add delimiter.
                }
            }
            builder.setDense(bi);
            return builder.build();
        }
        
        /**
         *  Serialize all nodes in the non-dense format.
         * 
         * @param parentbuilder Add to this PrimitiveBlock.
         */
        public Osmformat.PrimitiveGroup serializeNonDense() {
          if (contents.size() == 0) {
            return null;
          }
          // System.out.format("%d Nodes   ",nodes.size());
          StringTable stable = getStringTable();
          Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
          .newBuilder();
          for (Node i : contents) {
            long id = i.getId();
            int lat = mapDegrees(i.getLatitude());
            int lon = mapDegrees(i.getLongitude());
            Osmformat.Node.Builder bi = Osmformat.Node.newBuilder();
            bi.setId(id);
            bi.setLon(lon);
            bi.setLat(lat);
            for (Tag t : i.getTags()) {
              bi.addKeys(stable.getIndex(t.getKey()));
              bi.addVals(stable.getIndex(t.getValue()));
            }
            if (!omit_metadata) {
              bi.setInfo(serializeMetadata(i));
            }
            builder.addNodes(bi);
          }
          return builder.build();
        }
    
    }

    

    private class WayGroup extends Prim<Way> implements PrimGroupWriterInterface {
      public Osmformat.PrimitiveGroup serialize() {
        if (contents.size() == 0) {
          return null;
        }

            // System.out.format("%d Ways  ",contents.size());
            StringTable stable = getStringTable();
            Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
                    .newBuilder();
            for (Way i : contents) {
                Osmformat.Way.Builder bi = Osmformat.Way.newBuilder();
                bi.setId(i.getId());
                long lastid = 0;
                for (WayNode j : i.getWayNodes()) {
                    long id = j.getNodeId();
                    bi.addRefs(id - lastid);
                    lastid = id;
                }
                for (Tag t : i.getTags()) {
                    bi.addKeys(stable.getIndex(t.getKey()));
                    bi.addVals(stable.getIndex(t.getValue()));
                }
                if (!omit_metadata) {
                    bi.setInfo(serializeMetadata(i));
                }
                builder.addWays(bi);
            }
            return builder.build();
        }
    }

    private class RelationGroup extends Prim<Relation> implements
            PrimGroupWriterInterface {
        public void addStringsToStringtable() {
            StringTable stable = getStringTable();
            super.addStringsToStringtable();
            for (Relation i : contents) {
                for (RelationMember j : i.getMembers()) {
                    stable.incr(j.getMemberRole());
                }
            }
        }

        public Osmformat.PrimitiveGroup serialize() {
          if (contents.size() == 0) {
            return null;
          }

          // System.out.format("%d Relations  ",contents.size());
            StringTable stable = getStringTable();
            Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
                    .newBuilder();
            for (Relation i : contents) {
                Osmformat.Relation.Builder bi = Osmformat.Relation.newBuilder();
                bi.setId(i.getId());
                RelationMember[] arr = new RelationMember[i.getMembers().size()];
                i.getMembers().toArray(arr);
                long lastid = 0;
                for (RelationMember j : i.getMembers()) {
                    long id = j.getMemberId();
                    bi.addMemids(id - lastid);
                    lastid = id;
                    if (j.getMemberType() == EntityType.Node) {
                        bi.addTypes(MemberType.NODE);
                    } else if (j.getMemberType() == EntityType.Way) {
                        bi.addTypes(MemberType.WAY);
                    } else if (j.getMemberType() == EntityType.Relation) {
                        bi.addTypes(MemberType.RELATION);
                    } else {
                        assert (false); // Software bug: Unknown entity.
                    }
                    bi.addRolesSid(stable.getIndex(j.getMemberRole()));
                }

                for (Tag t : i.getTags()) {
                    bi.addKeys(stable.getIndex(t.getKey()));
                    bi.addVals(stable.getIndex(t.getValue()));
                }
                if (!omit_metadata) {
                    bi.setInfo(serializeMetadata(i));
                }
                builder.addRelations(bi);
            }
            return builder.build();
        }
    }

    /* One list for each type */
    private WayGroup ways;
    private NodeGroup nodes;
    private RelationGroup relations;

    private Processor processor = new Processor();

    /**
     * Buffer up events into groups that are all of the same type, or all of the
     * same length, then process each buffer.
     */
    public class Processor implements EntityProcessor {
        @Override
        public void process(BoundContainer bound) {
            // Specialcase this. Assume we only ever get one contigious bound
            // request.
            switchTypes();
            processBounds(bound.getEntity());
        }

		/**
		 * Check if we've reached the batch size limit and process the batch if
		 * we have.
		 */
        public void checkLimit() {
            total_entities++;
            if (++batch_size < batch_limit) {
                return;
            }
            switchTypes();
            processBatch();
        }

        @Override
        public void process(NodeContainer node) {
            if (nodes == null) {
                writeEmptyHeaderIfNeeded();
                // Need to switch types.
                switchTypes();
                nodes = new NodeGroup();
            }
            nodes.add(node.getEntity());
            checkLimit();
        }

        @Override
        public void process(WayContainer way) {
            if (ways == null) {
                writeEmptyHeaderIfNeeded();
                switchTypes();
                ways = new WayGroup();
            }
            ways.add(way.getEntity());
            checkLimit();
        }

        @Override
        public void process(RelationContainer relation) {
            if (relations == null) {
                writeEmptyHeaderIfNeeded();
                switchTypes();
                relations = new RelationGroup();
            }
            relations.add(relation.getEntity());
            checkLimit();
        }
    }

    /**
     * At the end of this function, all of the lists of unprocessed 'things'
     * must be null
     */
    private void switchTypes() {
        if (nodes != null) {
            groups.add(nodes);
            nodes = null;
        } else if (ways != null) {
            groups.add(ways);
            ways = null;
        } else if (relations != null) {
            groups.add(relations);
            relations = null;
        } else {
            return; // No data. Is this an empty file?
        }
    }

    /**
     * {@inheritDoc}
     */
    public void processBounds(Bound entity) {
        Osmformat.HeaderBlock.Builder headerblock = Osmformat.HeaderBlock
                .newBuilder();
        
        Osmformat.HeaderBBox.Builder bbox = Osmformat.HeaderBBox.newBuilder();
        bbox.setLeft(mapRawDegrees(entity.getLeft()));
        bbox.setBottom(mapRawDegrees(entity.getBottom()));
        bbox.setRight(mapRawDegrees(entity.getRight()));
        bbox.setTop(mapRawDegrees(entity.getTop()));
        headerblock.setBbox(bbox);

        if (entity.getOrigin() != null) {
        	headerblock.setSource(entity.getOrigin());
        }
        finishHeader(headerblock);
    }

    /** Write empty header block when there's no bounds entity. */
    public void writeEmptyHeaderIfNeeded() {
      if (headerWritten) {
        return;
      }
      Osmformat.HeaderBlock.Builder headerblock = Osmformat.HeaderBlock.newBuilder();
      finishHeader(headerblock);
    }

    /** Write the header fields that are always needed.
     * 
     * @param headerblock Incomplete builder to complete and write.
     * */
    public void finishHeader(Osmformat.HeaderBlock.Builder headerblock) {
      headerblock.setWritingprogram(OsmosisConstants.VERSION);
      headerblock.addRequiredFeatures("OsmSchema-V0.6");
      if (useDense) {
        headerblock.addRequiredFeatures("DenseNodes");
      }
      Osmformat.HeaderBlock message = headerblock.build();
      try {
          output.write(FileBlock.newInstance("OSMHeader", message
                  .toByteString(), null));
      } catch (IOException e) {
          throw new OsmosisRuntimeException("Unable to write OSM header.", e);
      }
      headerWritten = true;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
   
    
    /**
     * {@inheritDoc}
     */
    public void process(EntityContainer entityContainer) {
        entityContainer.process(processor);
    }

    @Override
    public void complete() {
        try {
            switchTypes();
            processBatch();
            flush();
        } catch (IOException e) {
        	throw new OsmosisRuntimeException("Unable to complete the PBF file.", e);
        }
    }

    @Override
    public void release() {
        try {
            close();
        } catch (IOException e) {
        	LOG.log(Level.WARNING, "Unable to release PBF file resources during release.", e);
        }

    }
}

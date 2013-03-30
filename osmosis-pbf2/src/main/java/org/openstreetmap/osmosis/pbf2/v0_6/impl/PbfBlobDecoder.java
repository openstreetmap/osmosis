// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat;
import org.openstreetmap.osmosis.pbf2.marshall.Fileformat.Blob;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.DenseInfo;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.HeaderBBox;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Info;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Node;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.PrimitiveGroup;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Relation;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Way;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Relation.MemberType;

import com.google.protobuf.InvalidProtocolBufferException;


/**
 * Converts PBF block data into decoded entities ready to be passed into an
 * Osmosis pipeline. This class is designed to be passed into a pool of worker
 * threads to allow multi-threaded decoding.
 * 
 * @author Brett Henderson
 */
public class PbfBlobDecoder implements Runnable {

	private static Logger log = Logger.getLogger(PbfBlobDecoder.class.getName());

	private static final double COORDINATE_SCALING_FACTOR = 0.000000001;
	private static final int EMPTY_VERSION = -1;
	private static final Date EMPTY_TIMESTAMP = new Date(0);
	private static final long EMPTY_CHANGESET = -1;

	private String blobType;
	private byte[] rawBlob;
	private PbfBlobDecoderListener listener;
	private List<EntityContainer> decodedEntities;


	/**
	 * Creates a new instance.
	 * 
	 * @param blobType
	 *            The type of blob.
	 * @param rawBlob
	 *            The raw data of the blob.
	 * @param listener
	 *            The listener for receiving decoding results.
	 */
	public PbfBlobDecoder(String blobType, byte[] rawBlob, PbfBlobDecoderListener listener) {
		this.blobType = blobType;
		this.rawBlob = rawBlob;
		this.listener = listener;
	}


	private byte[] readBlobContent() throws IOException {
		Blob blob = Blob.parseFrom(rawBlob);
		byte[] blobData;

		if (blob.hasRaw()) {
			blobData = blob.getRaw().toByteArray();
		} else if (blob.hasZlibData()) {
			Inflater inflater = new Inflater();
			inflater.setInput(blob.getZlibData().toByteArray());
			blobData = new byte[blob.getRawSize()];
			try {
				inflater.inflate(blobData);
			} catch (DataFormatException e) {
				throw new OsmosisRuntimeException("Unable to decompress PBF blob.", e);
			}
			if (!inflater.finished()) {
				throw new OsmosisRuntimeException("PBF blob contains incomplete compressed data.");
			}
		} else {
			throw new OsmosisRuntimeException("PBF blob uses unsupported compression, only raw or zlib may be used.");
		}

		return blobData;
	}


	private void processOsmHeader(byte[] data) throws InvalidProtocolBufferException {
		Osmformat.HeaderBlock header = Osmformat.HeaderBlock.parseFrom(data);

		// Build the list of active and unsupported features in the file.
		List<String> supportedFeatures = Arrays.asList("OsmSchema-V0.6", "DenseNodes");
		List<String> activeFeatures = new ArrayList<String>();
		List<String> unsupportedFeatures = new ArrayList<String>();
		for (String feature : header.getRequiredFeaturesList()) {
			if (supportedFeatures.contains(feature)) {
				activeFeatures.add(feature);
			} else {
				unsupportedFeatures.add(feature);
			}
		}

		// We can't continue if there are any unsupported features. We wait
		// until now so that we can display all unsupported features instead of
		// just the first one we encounter.
		if (unsupportedFeatures.size() > 0) {
			throw new OsmosisRuntimeException("PBF file contains unsupported features " + unsupportedFeatures);
		}

		// Build a new bound object which corresponds to the header.
		Bound bound;
		if (header.hasBbox()) {
			HeaderBBox bbox = header.getBbox();
			bound = new Bound(bbox.getRight() * COORDINATE_SCALING_FACTOR, bbox.getLeft() * COORDINATE_SCALING_FACTOR,
					bbox.getTop() * COORDINATE_SCALING_FACTOR, bbox.getBottom() * COORDINATE_SCALING_FACTOR,
					header.getSource());
		} else {
			bound = new Bound(header.getSource());
		}

		// Add the bound object to the results.
		decodedEntities.add(new BoundContainer(bound));
	}


	private void buildTags(CommonEntityData entityData, List<Integer> keys, List<Integer> values,
			PbfFieldDecoder fieldDecoder) {
		Collection<Tag> tags = entityData.getTags();

		// Ensure parallel lists are of equal size.
		if (keys.size() != values.size()) {
			throw new OsmosisRuntimeException("Number of tag keys (" + keys.size() + ") and tag values ("
					+ values.size() + ") don't match");
		}

		Iterator<Integer> keyIterator = keys.iterator();
		Iterator<Integer> valueIterator = values.iterator();
		while (keyIterator.hasNext()) {
			String key = fieldDecoder.decodeString(keyIterator.next());
			String value = fieldDecoder.decodeString(valueIterator.next());
			Tag tag = new Tag(key, value);
			tags.add(tag);
		}
	}


	private CommonEntityData buildCommonEntityData(long entityId, List<Integer> keys, List<Integer> values, Info info,
			PbfFieldDecoder fieldDecoder) {
		OsmUser user;
		CommonEntityData entityData;

		// Build the user, but only if one exists.
		if (info.hasUid() && info.getUid() >= 0 && info.hasUserSid()) {
			user = new OsmUser(info.getUid(), fieldDecoder.decodeString(info.getUserSid()));
		} else {
			user = OsmUser.NONE;
		}

		entityData = new CommonEntityData(entityId, info.getVersion(),
				fieldDecoder.decodeTimestamp(info.getTimestamp()), user, info.getChangeset());

		buildTags(entityData, keys, values, fieldDecoder);

		return entityData;
	}


	private CommonEntityData buildCommonEntityData(long entityId, List<Integer> keys, List<Integer> values,
			PbfFieldDecoder fieldDecoder) {
		CommonEntityData entityData;

		entityData = new CommonEntityData(entityId, EMPTY_VERSION, EMPTY_TIMESTAMP, OsmUser.NONE, EMPTY_CHANGESET);

		buildTags(entityData, keys, values, fieldDecoder);

		return entityData;
	}


	private void processNodes(List<Node> nodes, PbfFieldDecoder fieldDecoder) {
		for (Node node : nodes) {
			org.openstreetmap.osmosis.core.domain.v0_6.Node osmNode;
			CommonEntityData entityData;

			if (node.hasInfo()) {
				entityData = buildCommonEntityData(node.getId(), node.getKeysList(), node.getValsList(),
						node.getInfo(), fieldDecoder);

			} else {
				entityData = buildCommonEntityData(node.getId(), node.getKeysList(), node.getValsList(), fieldDecoder);
			}

			osmNode = new org.openstreetmap.osmosis.core.domain.v0_6.Node(entityData, fieldDecoder.decodeLatitude(node
					.getLat()), fieldDecoder.decodeLatitude(node.getLon()));

			// Add the bound object to the results.
			decodedEntities.add(new NodeContainer(osmNode));
		}
	}


	private void processNodes(DenseNodes nodes, PbfFieldDecoder fieldDecoder) {
		List<Long> idList = nodes.getIdList();
		List<Long> latList = nodes.getLatList();
		List<Long> lonList = nodes.getLonList();

		// Ensure parallel lists are of equal size.
		if ((idList.size() != latList.size()) || (idList.size() != lonList.size())) {
			throw new OsmosisRuntimeException("Number of ids (" + idList.size() + "), latitudes (" + latList.size()
					+ "), and longitudes (" + lonList.size() + ") don't match");
		}

		Iterator<Integer> keysValuesIterator = nodes.getKeysValsList().iterator();

		DenseInfo denseInfo;
		if (nodes.hasDenseinfo()) {
			denseInfo = nodes.getDenseinfo();
		} else {
			denseInfo = null;
		}

		long nodeId = 0;
		long latitude = 0;
		long longitude = 0;
		int userId = 0;
		int userSid = 0;
		long timestamp = 0;
		long changesetId = 0;
		for (int i = 0; i < idList.size(); i++) {
			CommonEntityData entityData;
			org.openstreetmap.osmosis.core.domain.v0_6.Node node;

			// Delta decode node fields.
			nodeId += idList.get(i);
			latitude += latList.get(i);
			longitude += lonList.get(i);

			if (denseInfo != null) {
				// Delta decode dense info fields.
				userId += denseInfo.getUid(i);
				userSid += denseInfo.getUserSid(i);
				timestamp += denseInfo.getTimestamp(i);
				changesetId += denseInfo.getChangeset(i);

				// Build the user, but only if one exists.
				OsmUser user;
				if (userId >= 0) {
					user = new OsmUser(userId, fieldDecoder.decodeString(userSid));
				} else {
					user = OsmUser.NONE;
				}

				entityData = new CommonEntityData(nodeId, denseInfo.getVersion(i),
						fieldDecoder.decodeTimestamp(timestamp), user, changesetId);
			} else {
				entityData = new CommonEntityData(nodeId, EMPTY_VERSION, EMPTY_TIMESTAMP, OsmUser.NONE,
						EMPTY_CHANGESET);
			}

			// Build the tags. The key and value string indexes are sequential
			// in the same PBF array. Each set of tags is delimited by an index
			// with a value of 0.
			Collection<Tag> tags = entityData.getTags();
			while (keysValuesIterator.hasNext()) {
				int keyIndex = keysValuesIterator.next();
				if (keyIndex == 0) {
					break;
				}
				if (!keysValuesIterator.hasNext()) {
					throw new OsmosisRuntimeException(
							"The PBF DenseInfo keys/values list contains a key with no corresponding value.");
				}
				int valueIndex = keysValuesIterator.next();

				Tag tag = new Tag(fieldDecoder.decodeString(keyIndex), fieldDecoder.decodeString(valueIndex));
				tags.add(tag);
			}

			node = new org.openstreetmap.osmosis.core.domain.v0_6.Node(entityData,
					fieldDecoder.decodeLatitude(latitude), fieldDecoder.decodeLongitude(longitude));

			// Add the bound object to the results.
			decodedEntities.add(new NodeContainer(node));
		}
	}


	private void processWays(List<Way> ways, PbfFieldDecoder fieldDecoder) {
		for (Way way : ways) {
			org.openstreetmap.osmosis.core.domain.v0_6.Way osmWay;
			CommonEntityData entityData;

			if (way.hasInfo()) {
				entityData = buildCommonEntityData(way.getId(), way.getKeysList(), way.getValsList(), way.getInfo(),
						fieldDecoder);

			} else {
				entityData = buildCommonEntityData(way.getId(), way.getKeysList(), way.getValsList(), fieldDecoder);
			}

			osmWay = new org.openstreetmap.osmosis.core.domain.v0_6.Way(entityData);

			// Build up the list of way nodes for the way. The node ids are
			// delta encoded meaning that each id is stored as a delta against
			// the previous one.
			long nodeId = 0;
			List<WayNode> wayNodes = osmWay.getWayNodes();
			for (long nodeIdOffset : way.getRefsList()) {
				nodeId += nodeIdOffset;
				wayNodes.add(new WayNode(nodeId));
			}

			decodedEntities.add(new WayContainer(osmWay));
		}
	}


	private void buildRelationMembers(org.openstreetmap.osmosis.core.domain.v0_6.Relation relation,
			List<Long> memberIds, List<Integer> memberRoles, List<MemberType> memberTypes,
			PbfFieldDecoder fieldDecoder) {

		List<RelationMember> members = relation.getMembers();

		// Ensure parallel lists are of equal size.
		if ((memberIds.size() != memberRoles.size()) || (memberIds.size() != memberTypes.size())) {
			throw new OsmosisRuntimeException("Number of member ids (" + memberIds.size() + "), member roles ("
					+ memberRoles.size() + "), and member types (" + memberTypes.size() + ") don't match");
		}

		Iterator<Long> memberIdIterator = memberIds.iterator();
		Iterator<Integer> memberRoleIterator = memberRoles.iterator();
		Iterator<MemberType> memberTypeIterator = memberTypes.iterator();

		// Build up the list of relation members for the way. The member ids are
		// delta encoded meaning that each id is stored as a delta against
		// the previous one.
		long memberId = 0;
		while (memberIdIterator.hasNext()) {
			MemberType memberType = memberTypeIterator.next();
			memberId += memberIdIterator.next();
			EntityType entityType;
			RelationMember member;

			if (memberType == MemberType.NODE) {
				entityType = EntityType.Node;
			} else if (memberType == MemberType.WAY) {
				entityType = EntityType.Way;
			} else if (memberType == MemberType.RELATION) {
				entityType = EntityType.Relation;
			} else {
				throw new OsmosisRuntimeException("Member type of " + memberType + " is not supported.");
			}

			member = new RelationMember(memberId, entityType, fieldDecoder.decodeString(memberRoleIterator.next()));

			members.add(member);
		}
	}


	private void processRelations(List<Relation> relations, PbfFieldDecoder fieldDecoder) {
		for (Relation relation : relations) {
			org.openstreetmap.osmosis.core.domain.v0_6.Relation osmRelation;
			CommonEntityData entityData;

			if (relation.hasInfo()) {
				entityData = buildCommonEntityData(relation.getId(), relation.getKeysList(), relation.getValsList(),
						relation.getInfo(), fieldDecoder);

			} else {
				entityData = buildCommonEntityData(relation.getId(), relation.getKeysList(), relation.getValsList(),
						fieldDecoder);
			}

			osmRelation = new org.openstreetmap.osmosis.core.domain.v0_6.Relation(entityData);

			buildRelationMembers(osmRelation, relation.getMemidsList(), relation.getRolesSidList(),
					relation.getTypesList(), fieldDecoder);

			// Add the bound object to the results.
			decodedEntities.add(new RelationContainer(osmRelation));
		}
	}


	private void processOsmPrimitives(byte[] data) throws InvalidProtocolBufferException {
		Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(data);
		PbfFieldDecoder fieldDecoder = new PbfFieldDecoder(block);

		for (PrimitiveGroup primitiveGroup : block.getPrimitivegroupList()) {
			log.finer("Processing OSM primitive group.");
			processNodes(primitiveGroup.getDense(), fieldDecoder);
			processNodes(primitiveGroup.getNodesList(), fieldDecoder);
			processWays(primitiveGroup.getWaysList(), fieldDecoder);
			processRelations(primitiveGroup.getRelationsList(), fieldDecoder);
		}
	}


	private void runAndTrapExceptions() {
		try {
			decodedEntities = new ArrayList<EntityContainer>();

			if ("OSMHeader".equals(blobType)) {
				processOsmHeader(readBlobContent());

			} else if ("OSMData".equals(blobType)) {
				processOsmPrimitives(readBlobContent());

			} else {
				if (log.isLoggable(Level.FINER)) {
					log.finer("Skipping unrecognised blob type " + blobType);
				}
			}

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to process PBF blob", e);
		}
	}


	@Override
	public void run() {
		try {
			runAndTrapExceptions();

			listener.complete(decodedEntities);

		} catch (RuntimeException e) {
			listener.error();
		}
	}
}

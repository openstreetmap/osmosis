// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
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
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.marshall.Fileformat;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat;
import org.openstreetmap.osmosis.pbf2.marshall.Fileformat.Blob;
import org.openstreetmap.osmosis.pbf2.marshall.Fileformat.BlobHeader;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.HeaderBBox;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Info;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Node;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.PrimitiveGroup;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Relation;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Way;
import org.openstreetmap.osmosis.pbf2.marshall.Osmformat.Relation.MemberType;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfFieldDecoder;

import com.google.protobuf.InvalidProtocolBufferException;



/**
 * An OSM data source reading from a PBF file. The entire contents of the file
 * are read.
 * 
 * @author Brett Henderson
 */
public class PbfReader implements RunnableSource {

	private static Logger log = Logger.getLogger(PbfReader.class.getName());

	private static final double COORDINATE_SCALING_FACTOR = 0.000000001;
	private static final int EMPTY_VERSION = -1;
	private static final Date EMPTY_TIMESTAMP = new Date(0);
	private static final long EMPTY_CHANGESET = -1;

	private File file;
	private Sink sink;


	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to read.
	 */
	public PbfReader(File file) {
		this.file = file;
	}


	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}


	private BlobHeader readHeader(int headerLength, DataInputStream dis) throws IOException {
		byte[] headerBuffer = new byte[headerLength];
		dis.readFully(headerBuffer);

		BlobHeader blobHeader = Fileformat.BlobHeader.parseFrom(headerBuffer);

		return blobHeader;
	}


	private byte[] readBlobData(BlobHeader blobHeader, DataInputStream dis) throws IOException {
		byte[] blobData;
		byte[] blobBuffer = new byte[blobHeader.getDatasize()];

		dis.readFully(blobBuffer);
		Blob blob = Blob.parseFrom(blobBuffer);

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

		// Pass the bound object downstream.
		sink.process(new BoundContainer(bound));
	}
	
	
	private void buildTags(CommonEntityData entityData, List<Integer> keys, List<Integer> values,
			PbfFieldDecoder fieldDecoder) {
		Collection<Tag> tags = entityData.getTags();

		// Ensure parallel lists are of equal size.
		if (keys.size() != values.size()) {
			throw new OsmosisRuntimeException("Number of tag keys (" + keys.size()
					+ ") and tag values (" + values.size() + ") don't match");
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

		entityData = new CommonEntityData(
				entityId,
				info.getVersion(),
				new Date(info.getTimestamp()),
				user,
				info.getChangeset());

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
				entityData = buildCommonEntityData(
						node.getId(),
						node.getKeysList(),
						node.getValsList(),
						node.getInfo(),
						fieldDecoder);
				
			} else {
				entityData = buildCommonEntityData(node.getId(), node.getKeysList(), node.getValsList(), fieldDecoder);
			}

			osmNode = new org.openstreetmap.osmosis.core.domain.v0_6.Node(
					entityData,
					node.getLat() * COORDINATE_SCALING_FACTOR,
					node.getLon() * COORDINATE_SCALING_FACTOR);

			sink.process(new NodeContainer(osmNode));
		}
	}
	
	
	private void processWays(List<Way> ways, PbfFieldDecoder fieldDecoder) {
		for (Way way : ways) {
			org.openstreetmap.osmosis.core.domain.v0_6.Way osmWay;
			CommonEntityData entityData;

			if (way.hasInfo()) {
				entityData = buildCommonEntityData(
						way.getId(),
						way.getKeysList(),
						way.getValsList(),
						way.getInfo(),
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

			sink.process(new WayContainer(osmWay));
		}
	}


	private void buildRelationMembers(org.openstreetmap.osmosis.core.domain.v0_6.Relation relation,
			List<Long> memberIds, List<Integer> memberRoles, List<MemberType> memberTypes,
			PbfFieldDecoder fieldDecoder) {

		List<RelationMember> members = relation.getMembers();

		// Ensure parallel lists are of equal size.
		if ((memberIds.size() != memberRoles.size()) && (memberIds.size() != memberTypes.size())) {
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
				entityData = buildCommonEntityData(
						relation.getId(),
						relation.getKeysList(),
						relation.getValsList(),
						relation.getInfo(),
						fieldDecoder);

			} else {
				entityData = buildCommonEntityData(relation.getId(), relation.getKeysList(), relation.getValsList(),
						fieldDecoder);
			}

			osmRelation = new org.openstreetmap.osmosis.core.domain.v0_6.Relation(entityData);

			buildRelationMembers(
					osmRelation,
					relation.getMemidsList(),
					relation.getRolesSidList(),
					relation.getTypesList(),
					fieldDecoder);

			sink.process(new RelationContainer(osmRelation));
		}
	}


	private void processOsmPrimitives(byte[] data) throws InvalidProtocolBufferException {
		Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(data);
		PbfFieldDecoder fieldDecoder = new PbfFieldDecoder(block);

		for (PrimitiveGroup primitiveGroup : block.getPrimitivegroupList()) {
			processNodes(primitiveGroup.getNodesList(), fieldDecoder);
			processWays(primitiveGroup.getWaysList(), fieldDecoder);
			processRelations(primitiveGroup.getRelationsList(), fieldDecoder);
		}
	}


	private void processBlobs(DataInputStream dis) throws IOException {
		while (true) {
			// Read the length of the next header block. This is the only time
			// we should expect to encounter an EOF exception. In all other
			// cases it indicates a corrupt or truncated file.
			int headerLength;
			try {
				headerLength = dis.readInt();
			} catch (EOFException e) {
				return;
			}

			// Read the header message then process the associated blob message
			// according to its type. Skip any unknown blob bytes.
			BlobHeader blobHeader = readHeader(headerLength, dis);
			if ("OSMHeader".equals(blobHeader.getType())) {
				processOsmHeader(readBlobData(blobHeader, dis));
			} else if ("OSMData".equals(blobHeader.getType())) {
				processOsmPrimitives(readBlobData(blobHeader, dis));
			} else {
				if (dis.skip(blobHeader.getDatasize()) != blobHeader.getDatasize()) {
					throw new OsmosisRuntimeException(
							"Unable to skip over current data block, data may be truncated or corrupt.");
				}
			}
		}
	}


	@Override
	public void run() {
		InputStream inputStream = null;

		try {
			sink.initialize(Collections.<String, Object>emptyMap());

			// make "-" an alias for /dev/stdin
			if (file.getName().equals("-")) {
				inputStream = System.in;
			} else {
				inputStream = new FileInputStream(file);
			}

			// Process all blobs of data in the stream.
			DataInputStream dis = new DataInputStream(inputStream);
			processBlobs(dis);

			sink.complete();

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read PBF file " + file + ".", e);
		} finally {
			sink.release();

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, "Unable to close input stream.", e);
				}
				inputStream = null;
			}
		}
	}
}

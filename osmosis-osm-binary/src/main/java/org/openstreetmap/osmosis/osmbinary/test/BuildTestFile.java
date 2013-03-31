/** Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as 
   published by the Free Software Foundation, either version 3 of the 
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.openstreetmap.osmosis.osmbinary.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import com.google.protobuf.ByteString;

import org.openstreetmap.osmosis.osmbinary.Fileformat.Blob;
import org.openstreetmap.osmosis.osmbinary.Fileformat.BlobHeader;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Info;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.PrimitiveBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.PrimitiveBlock.Builder;
import org.openstreetmap.osmosis.osmbinary.Osmformat.PrimitiveGroup;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation.MemberType;
import org.openstreetmap.osmosis.osmbinary.Osmformat.StringTable;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;
import org.openstreetmap.osmosis.osmbinary.file.FileBlock;

public class BuildTestFile {
  BlockOutputStream output;
  public static final long BILLION = 1000000000L;
  
  StringTable makeStringTable(String prefix) {
    return 
    StringTable.newBuilder()
    .addS(ByteString.copyFromUtf8("")) // Never used.
    .addS(ByteString.copyFromUtf8(prefix+"Offset1"))
    .addS(ByteString.copyFromUtf8(prefix+"Offset2"))
    .addS(ByteString.copyFromUtf8(prefix+"Offset3"))
    .addS(ByteString.copyFromUtf8(prefix+"Offset4"))
    .addS(ByteString.copyFromUtf8(prefix+"Offset5"))
    .addS(ByteString.copyFromUtf8(prefix+"Offset6"))
    .addS(ByteString.copyFromUtf8(prefix+"Offset7"))
    .addS(ByteString.copyFromUtf8(prefix+"Offset8"))
    .build();
  }
  void makeSimpleFileBlock1() throws IOException {
    PrimitiveBlock.Builder b1 = PrimitiveBlock.newBuilder();
    b1.setStringtable(makeStringTable("B1"));

    b1.addPrimitivegroup(
        PrimitiveGroup.newBuilder()
        .addNodes(Node.newBuilder()
            .setId(101).setLat(13*10*1000*1000).setLon(-14*10*1000*1000)
            .addKeys(1).addVals(2))
        .addNodes(Node.newBuilder()
            .setId(101).setLat(12345678).setLon(-23456789)) // Should be 1.2345678 degrees lat and -2.3456789 lon.
    );
    b1.addPrimitivegroup(
        PrimitiveGroup.newBuilder()
        .addWays(Way.newBuilder()
            .setId(201)
            .addRefs(101).addRefs(1).addRefs(-1).addRefs(10).addRefs(-20) // Delta coded. Should be 101, 102, 101, 111, 91.
            .addKeys(2).addVals(1).addKeys(3).addVals(4))
        .addWays(Way.newBuilder()
            .setId(-301)
            .addRefs(211).addRefs(1).addRefs(-1).addRefs(10).addRefs(-300) // Delta coded. Should be 211, 212, 211, 221, -79
            .addKeys(4).addVals(3).addKeys(5).addVals(6))
        .addWays(Way.newBuilder()
            .setId(401).addRefs(211).addRefs(1))
        .addWays(Way.newBuilder()
            .setId(501))            
    );
    
    b1.addPrimitivegroup(
        PrimitiveGroup.newBuilder()
        .addRelations(Relation.newBuilder()
            .setId(601)
            .addTypes(MemberType.NODE).addMemids(50).addRolesSid(2)
            .addTypes(MemberType.NODE).addMemids(3).addRolesSid(3)
            .addTypes(MemberType.WAY).addMemids(3).addRolesSid(4)
            .addTypes(MemberType.RELATION).addMemids(3).addRolesSid(5))
        .addRelations(Relation.newBuilder()
            .setId(701)
            .addTypes(MemberType.RELATION).addMemids(60).addRolesSid(6)
            .addTypes(MemberType.RELATION).addMemids(5).addRolesSid(7)
            .addKeys(1).addVals(2)));

    b1.addPrimitivegroup(
        PrimitiveGroup.newBuilder()
        .setDense(DenseNodes.newBuilder()
            .addId(1001).addId(110).addId(-2000).addId(8889)
            .addLat(12*10000000).addLat(1500000).addLat(-12*10000000).addLat(-12*10000000)
            .addLon(-12*10000000).addLon(2500000).addLon(13*10000000).addLon(2*10000000)
            .addKeysVals(1).addKeysVals(2).addKeysVals(0)
            .addKeysVals(0)
            .addKeysVals(2).addKeysVals(3).addKeysVals(4).addKeysVals(5).addKeysVals(0)
            .addKeysVals(3).addKeysVals(3).addKeysVals(0)
            ));

    output.write(FileBlock.newInstance("OSMData", b1.build().toByteString(),null));

    PrimitiveBlock.Builder b2 = PrimitiveBlock.newBuilder();
    b2.setLatOffset(10*BILLION + 109208300)
     .setLonOffset(20*BILLION + 901802700)
     .setGranularity(1200);
    b2.setStringtable(makeStringTable("B2"));
    
    // Test out granularity stuff.
    b2.addPrimitivegroup(
        PrimitiveGroup.newBuilder()
        .addNodes(Node.newBuilder().setId(100000).setLat(0).setLon(0))
        .addNodes(Node.newBuilder().setId(100001).setLat(1000).setLon(2000))
        .addNodes(Node.newBuilder().setId(100002).setLat(1001).setLon(2001))
        .addNodes(Node.newBuilder().setId(100003).setLat(1002).setLon(2002))
        .addNodes(Node.newBuilder().setId(100004).setLat(1003).setLon(2003))
        .addNodes(Node.newBuilder().setId(100005).setLat(1004).setLon(2004)));
    
    
    output.write(FileBlock.newInstance("OSMData", b2.build().toByteString(),null));
  }

  
  BuildTestFile(String name, String compress) throws IOException {
    output = new BlockOutputStream(new FileOutputStream(name));
    output.setCompress(compress);
    HeaderBlock.Builder b = HeaderBlock.newBuilder();
    b.addRequiredFeatures("OsmSchema-V0.6").addRequiredFeatures("DenseNodes").setSource("QuickBrownFox");
    output.write(FileBlock.newInstance("OSMHeader",b.build().toByteString(),null));
  }
  
  
  public static void main(String [] args) {
    try {
      BuildTestFile out1a = new BuildTestFile("TestFile1-deflate.osm.pbf","deflate");
      out1a.makeSimpleFileBlock1();
      out1a.output.close();

      BuildTestFile out1b = new BuildTestFile("TestFile1-none.osm.pbf","none");
      out1b.makeSimpleFileBlock1();
      out1b.output.close();

      BuildTestFile out2 = new BuildTestFile("TestFile2-uncom.osm.pbf","deflate");
      out2.makeGranFileBlock1();
      out2.output.close();

    
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  
  void makeGranFileBlock1() throws IOException {
    PrimitiveBlock.Builder b1 = PrimitiveBlock.newBuilder();
    b1.setLatOffset(10*BILLION + 109208300)
     .setLonOffset(20*BILLION + 901802700)
     .setGranularity(1200)
     .setDateGranularity(2500);
    b1.setStringtable(makeStringTable("C1"));

    b1.addPrimitivegroup(
        PrimitiveGroup.newBuilder()
            .addNodes(Node.newBuilder()
            .setId(100001)
            .setLat(1000).setLon(2000)
            .setInfo(Info.newBuilder()
                .setTimestamp(1001)
                .setChangeset(-12)
                .setUid(21)
                .setUserSid(6)
                .build())
             .build())
        .addNodes(Node.newBuilder()
            .setId(100002)
            .setLat(1001).setLon(2001)
            .setInfo(Info.newBuilder()
                .setVersion(102)
                .setTimestamp(1002)
                .setChangeset(12)
                .setUid(-21)
                .setUserSid(5)
                .build())
            .build())
        .addNodes(Node.newBuilder()
            .setId(100003)
            .setLat(1003).setLon(2003)
            .setInfo(Info.newBuilder()
                .setVersion(103)
                .setUserSid(4)
                .build())
           .build())
    )

    ;
    
    // The same, but with different granularities.
    PrimitiveBlock.Builder b2 = PrimitiveBlock.newBuilder();
    b2.setLatOffset(12*BILLION + 303)
     .setLonOffset(22*BILLION + 404)
     .setGranularity(1401)
     .setDateGranularity(3003);
    b2.setStringtable(makeStringTable("C2"));
    b2.addPrimitivegroup(
        PrimitiveGroup.newBuilder()
        .addNodes(Node.newBuilder()
            .setId(100001)
            .addKeys(1).addVals(2)
            .addKeys(1).addVals(3) // Support multiple vals for a key.
            .addKeys(3).addVals(4)
            .setLat(1000).setLon(2000)
            .build())
        .addNodes(Node.newBuilder()
            .setId(100002)
            .setLat(1001).setLon(2001)
            .build())
        .addNodes(Node.newBuilder()
            .setId(100003)
            .setLat(1003).setLon(2003)
            .addKeys(5).addVals(6)
            .build())
    );
    output.write(FileBlock.newInstance("OSMData", b1.build().toByteString(),null));
    output.write(FileBlock.newInstance("OSMData", b2.build().toByteString(),null));
  }
  
}

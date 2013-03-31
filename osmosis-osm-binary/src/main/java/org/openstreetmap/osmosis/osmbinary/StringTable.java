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

package org.openstreetmap.osmosis.osmbinary;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import com.google.protobuf.ByteString;

/**
 * Class for mapping a set of strings to integers, giving frequently occuring
 * strings small integers.
 */
public class StringTable {
    public StringTable() {
        clear();
    }

    private HashMap<String, Integer> counts;
    private HashMap<String, Integer> stringmap;
    private String set[];

    public void incr(String s) {
        if (counts.containsKey(s)) {
            counts.put(s, new Integer(counts.get(s).intValue() + 1));
        } else {
            counts.put(s, new Integer(1));
        }
    }

    /** After the stringtable has been built, return the offset of a string in it.
     *
     * Note, value '0' is reserved for use as a delimiter and will not be returned.
     * @param s
     * @return
     */
    public int getIndex(String s) {
        return stringmap.get(s).intValue();
    }

    public void finish() {
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(final String s1, String s2) {
                int diff = counts.get(s2) - counts.get(s1);
                return diff;
            }
        };

        /* Sort the stringtable */

        /*
        When a string is referenced, strings in the stringtable with indices:
               0                : Is reserved (used as a delimiter in tags
         A:  1 to 127          : Uses can be represented with 1 byte
         B: 128 to 128**2-1 : Uses can be represented with 2 bytes,
         C: 128*128  to X    : Uses can be represented with 3 bytes in the unlikely case we have >16k strings in a block. No block will contain enough strings that we'll need 4 bytes.

        There are goals that will improve compression:
          1. I want to use 1 bytes for the most frequently occurring strings, then 2 bytes, then 3 bytes.
          2. I want to use low integers as frequently as possible (for better
             entropy encoding out of deflate)
          3. I want the stringtable to compress as small as possible.

        Condition 1 is obvious. Condition 2 makes deflate compress stringtable references more effectively.
        When compressing entities, delta coding causes small positive integers to occur more frequently
        than larger integers. Even though a stringtable references to indices of 1 and 127 both use one
        byte in a decompressed file, the small integer bias causes deflate to use fewer bits to represent
        the smaller index when compressed. Condition 3 is most effective when adjacent strings in the
        stringtable have a lot of common substrings.

        So, when I decide on the master stringtable to use, I put the 127 most frequently occurring
        strings into A (accomplishing goal 1), and sort them by frequency (to accomplish goal 2), but
        for B and C, which contain the less progressively less frequently encountered strings, I sort
        them lexiconographically, to maximize goal 3 and ignoring goal 2.

        Goal 1 is the most important. Goal 2 helped enough to be worth it, and goal 3 was pretty minor,
        but all should be re-benchmarked.


        */



        set = counts.keySet().toArray(new String[0]);
        if (set.length > 0) {
          // Sort based on the frequency.
          Arrays.sort(set, comparator);
          // Each group of keys that serializes to the same number of bytes is
          // sorted lexiconographically.
          // to maximize deflate compression.

          // Don't sort the first array. There's not likely to be much benefit, and we want frequent values to be small.
          //Arrays.sort(set, Math.min(0, set.length-1), Math.min(1 << 7, set.length-1));

          Arrays.sort(set, Math.min(1 << 7, set.length-1), Math.min(1 << 14,
              set.length-1));
          Arrays.sort(set, Math.min(1 << 14, set.length-1), Math.min(1 << 21,
              set.length-1), comparator);
        }
        stringmap = new HashMap<String, Integer>(2 * set.length);
        for (int i = 0; i < set.length; i++) {
            stringmap.put(set[i], new Integer(i+1)); // Index 0 is reserved for use as a delimiter.
        }
        counts = null;
    }

    public void clear() {
        counts = new HashMap<String, Integer>(100);
        stringmap = null;
        set = null;
    }

    public Osmformat.StringTable.Builder serialize() {
        Osmformat.StringTable.Builder builder = Osmformat.StringTable
                .newBuilder();
        builder.addS(ByteString.copyFromUtf8("")); // Add a unused string at offset 0 which is used as a delimiter.
        for (int i = 0; i < set.length; i++)
            builder.addS(ByteString.copyFromUtf8(set[i]));
        return builder;
    }
}

/*
 * This file has been copied from the following location:
 * http://archives.postgresql.org/pgsql-jdbc/2009-12/msg00037.php
 * 
 * PostgreSQL code is typically under a BSD licence.
 * http://jdbc.postgresql.org/license.html
 */

/*-------------------------------------------------------------------------
*
* A preliminary version of a custom type wrapper for hstore data.
* Once it gets some testing and cleanups it will go into the official
* PG JDBC driver, but stick it here for now because we need it sooner.
*
* Copyright (c) 2009, PostgreSQL Global Development Group
*
* IDENTIFICATION
*   $PostgreSQL$
*
*-------------------------------------------------------------------------
*/
package org.openstreetmap.osmosis.hstore;

import java.io.Serializable;

import java.sql.SQLException;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

import org.postgresql.util.PGobject;


/**
 * This implements a class that handles the PostgreSQL contrib/hstore type
 */
public class PGHStore extends PGobject implements Serializable, Cloneable, Map<String, String>
{
    private final static long serialVersionUID = 1;
    private Map<String, String> _map;

    /**
     * required by the driver
     */
    public PGHStore()
    {
        setType("hstore");
        _map = new HashMap<String, String>();
    }

    /**
     * Initialize a hstore with a given string representation
     *
     * @param value String representated hstore
     * @throws SQLException Is thrown if the string representation has an unknown format
     * @see #setValue(String)
     */
    public PGHStore(String value)
    throws SQLException
    {
        this();
        setValue(value);
    }

    public PGHStore(Map<String, String> map)
    {
        this();
        setValue(map);
    }

    public void setValue(Map<String, String> map)
    {
        _map = map;
    }

    /**
     */
    public void setValue(String value)
    throws SQLException
    {
        Parser p = new Parser();
        _map = p.parse(value);
    }

    /**
     * Returns the stored information as a string
     *
     * @return String represented hstore
     */
    public String getValue()
    {
        StringBuffer buf = new StringBuffer();
        Iterator<String> i = _map.keySet().iterator();
        boolean first = true;
        while (i.hasNext()) {
            Object key = i.next();
            Object value = _map.get(key);

            if (first) {
                first = false;
            } else {
                buf.append(',');
            }

            writeValue(buf, key);
            buf.append("=>");
            writeValue(buf, value);
        }

        return buf.toString();
    }

    private static void writeValue(StringBuffer buf, Object o) {
        if (o == null) {
            buf.append("NULL");
            return;
        }

        String s = o.toString();

        buf.append('"');
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                buf.append('\\');
            }
            buf.append(c);
        }
        buf.append('"');
    }


    /**
     * Returns whether an object is equal to this one or not
     *
     * @param obj Object to compare with
     * @return true if the two hstores are identical
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (obj == this)
            return true;

        if (!(obj instanceof PGHStore))
            return false;

        return _map.equals(((PGHStore)obj)._map);

    }

    private static class Parser {
        private String value;
        private int ptr;
        private StringBuffer cur;
        private boolean escaped;

        private List<String> keys;
        private List<String> values;

        private final static int GV_WAITVAL = 0;
        private final static int GV_INVAL = 1;
        private final static int GV_INESCVAL = 2;
        private final static int GV_WAITESCIN = 3;
        private final static int GV_WAITESCESCIN = 4;

        private final static int WKEY = 0;
        private final static int WVAL = 1;
        private final static int WEQ = 2;
        private final static int WGT = 3;
        private final static int WDEL = 4;

        public Parser() {
        }

    	private Map<String, String> parse(String value) throws SQLException {
            this.value = value;
            ptr = 0;
            keys = new ArrayList<String>();
            values = new ArrayList<String>();
            
            parseHStore();

            Map<String, String> map = new HashMap<String, String>();
            for (int i=0; i<keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }

            return map;
        }

        private boolean getValue(boolean ignoreEqual) throws SQLException {
            int state = GV_WAITVAL;

            cur = new StringBuffer();
            escaped = false;

            while (true) {
                boolean atEnd = (value.length() == ptr);
                char c = '\0';
                if (!atEnd) {
                    c = value.charAt(ptr);
                }

                if (state == GV_WAITVAL) {
                    if (c == '"') {
                        escaped = true;
                        state = GV_INESCVAL;
                    } else if (c == '\0') {
                        return false;
                    } else if (c == '=' && !ignoreEqual) {
                        throw new SQLException("KJJ");
                    } else if (c == '\\') {
                        state = GV_WAITESCIN;
                    } else if (!Character.isWhitespace(c)) {
                        cur.append(c);
                        state = GV_INVAL;
                    }
                } else if (state == GV_INVAL) {
                    if (c == '\\') {
                        state = GV_WAITESCIN;
                    } else if (c == '=' && !ignoreEqual) {
                        ptr--;
                        return true;
                    } else if (c == ',' && ignoreEqual) {
                        ptr--;
                        return true;
                    } else if (Character.isWhitespace(c)) {
                        return true;
                    } else if (c == '\0') {
                        ptr--;
                        return true;
                    } else {
                        cur.append(c);
                    }
                } else if (state == GV_INESCVAL) {
                    if (c == '\\') {
                        state = GV_WAITESCESCIN;
                    } else if (c == '"') {
                        return true;
                    } else if (c == '\0') {
                        throw new SQLException("KJJ, unexpected end of string");
                    } else {
                        cur.append(c);
                    }
                } else if (state == GV_WAITESCIN) {
                    if (c == '\0') {
                        throw new SQLException("KJJ, unexpected end of string");
                    }

                    cur.append(c);
                    state = GV_INVAL;
                } else if (state == GV_WAITESCESCIN) {
                    if (c == '\0') {
                        throw new SQLException("KJJ, unexpected end of string");
                    }

                    cur.append(c);
                    state = GV_INESCVAL;
                } else {
                    throw new SQLException("KJJ");
                }

                ptr++;
            }
        }

    	private void parseHStore() throws SQLException {
            int state = WKEY;
            escaped = false;

            while (true) {
                char c = '\0';
                if (ptr < value.length()) {
                    c = value.charAt(ptr);
                }

                if (state == WKEY) {
                    if (!getValue(false))
                        return;

                    keys.add(cur.toString());
                    cur = null;
                    state = WEQ;
                } else if (state == WEQ) {
                    if (c == '=') {
                        state = WGT;
                    } else if (state == '\0') {
                        throw new SQLException("KJJ, unexpected end of string");
                    } else if (!Character.isWhitespace(c)) {
                        throw new SQLException("KJJ, syntax err");
                    }
                } else if (state == WGT) {
                    if (c == '>') {
                        state = WVAL;
                    } else if (c == '\0') {
                        throw new SQLException("KJJ, unexpected end of string");
                    } else {
                        throw new SQLException("KJJ, syntax err [" + c + "] at " + ptr);
                    }
                } else if (state == WVAL) {
                    if (!getValue(true)) {
                        throw new SQLException("KJJ, unexpected end of string");
                    }

                    String val = cur.toString();
                    cur = null;
                    if (!escaped && "null".equalsIgnoreCase(val)) {
                        val = null;
                    }

                    values.add(val);
                    state = WDEL;
                } else if (state == WDEL) {
                    if (c == ',')
                    {
                        state = WKEY;
                    } else if (c == '\0') {
                        return;
                    } else if (!Character.isWhitespace(c)) {
                        throw new SQLException("KJJ, syntax err");
                    }
                } else {
                    throw new SQLException("KJJ unknown state");
                }

                ptr++;
            }
        }

    }


    // Farm out all the work to the real underlying map.

    public void clear() { _map.clear(); }

    public boolean containsKey(Object key) { return _map.containsKey(key); }

    public boolean containsValue(Object value) { return _map.containsValue(value); }

    public Set<Map.Entry<String, String>> entrySet() { return _map.entrySet(); }

    public String get(Object key) { return _map.get(key); }

    public int hashCode() { return _map.hashCode(); }

    public boolean isEmpty() { return _map.isEmpty(); }

    public Set<String> keySet() { return _map.keySet(); }

    public String put(String key, String value) { return _map.put(key, value); }

    public void putAll(Map<? extends String, ? extends String> m) { _map.putAll(m); }

    public String remove(Object key) { return _map.remove(key); }

    public int size() { return _map.size(); }

    public Collection<String> values() { return _map.values(); }

}

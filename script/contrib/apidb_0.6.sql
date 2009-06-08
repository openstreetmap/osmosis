--
-- PostgreSQL database dump
--

-- Started on 2009-06-08 17:57:34 EST

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

--
-- TOC entry 350 (class 1247 OID 35463)
-- Dependencies: 3
-- Name: nwr_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE nwr_enum AS ENUM (
    'Node',
    'Way',
    'Relation'
);


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1599 (class 1259 OID 35568)
-- Dependencies: 3
-- Name: acls; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE acls (
    id integer NOT NULL,
    address inet NOT NULL,
    netmask inet NOT NULL,
    k character varying(255) NOT NULL,
    v character varying(255)
);


--
-- TOC entry 1598 (class 1259 OID 35566)
-- Dependencies: 1599 3
-- Name: acls_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE acls_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2096 (class 0 OID 0)
-- Dependencies: 1598
-- Name: acls_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE acls_id_seq OWNED BY acls.id;


--
-- TOC entry 2097 (class 0 OID 0)
-- Dependencies: 1598
-- Name: acls_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('acls_id_seq', 1, false);


--
-- TOC entry 1604 (class 1259 OID 35704)
-- Dependencies: 1924 1925 3
-- Name: changeset_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE changeset_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1603 (class 1259 OID 35697)
-- Dependencies: 1923 3
-- Name: changesets; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE changesets (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    min_lat integer,
    max_lat integer,
    min_lon integer,
    max_lon integer,
    closed_at timestamp without time zone NOT NULL,
    num_changes integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 1602 (class 1259 OID 35695)
-- Dependencies: 3 1603
-- Name: changesets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE changesets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2098 (class 0 OID 0)
-- Dependencies: 1602
-- Name: changesets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE changesets_id_seq OWNED BY changesets.id;


--
-- TOC entry 2099 (class 0 OID 0)
-- Dependencies: 1602
-- Name: changesets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('changesets_id_seq', 1, false);


--
-- TOC entry 1606 (class 1259 OID 40565)
-- Dependencies: 3
-- Name: countries; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE countries (
    id integer NOT NULL,
    code character varying(2) NOT NULL,
    min_lat double precision NOT NULL,
    max_lat double precision NOT NULL,
    min_lon double precision NOT NULL,
    max_lon double precision NOT NULL
);


--
-- TOC entry 1605 (class 1259 OID 40563)
-- Dependencies: 1606 3
-- Name: countries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE countries_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2100 (class 0 OID 0)
-- Dependencies: 1605
-- Name: countries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE countries_id_seq OWNED BY countries.id;


--
-- TOC entry 2101 (class 0 OID 0)
-- Dependencies: 1605
-- Name: countries_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('countries_id_seq', 246, true);


--
-- TOC entry 1600 (class 1259 OID 35586)
-- Dependencies: 1918 1919 3
-- Name: current_node_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_node_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1585 (class 1259 OID 35414)
-- Dependencies: 3
-- Name: current_nodes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_nodes (
    id bigint NOT NULL,
    latitude integer NOT NULL,
    longitude integer NOT NULL,
    changeset_id bigint NOT NULL,
    visible boolean NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    tile bigint NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1584 (class 1259 OID 35412)
-- Dependencies: 1585 3
-- Name: current_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE current_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2102 (class 0 OID 0)
-- Dependencies: 1584
-- Name: current_nodes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE current_nodes_id_seq OWNED BY current_nodes.id;


--
-- TOC entry 2103 (class 0 OID 0)
-- Dependencies: 1584
-- Name: current_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('current_nodes_id_seq', 1, false);


--
-- TOC entry 1587 (class 1259 OID 35459)
-- Dependencies: 1904 3 350
-- Name: current_relation_members; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_relation_members (
    id bigint NOT NULL,
    member_id bigint NOT NULL,
    member_role character varying(255) NOT NULL,
    member_type nwr_enum NOT NULL,
    sequence_id integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 1588 (class 1259 OID 35470)
-- Dependencies: 1905 1906 3
-- Name: current_relation_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_relation_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1590 (class 1259 OID 35482)
-- Dependencies: 3
-- Name: current_relations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_relations (
    id bigint NOT NULL,
    changeset_id bigint NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    visible boolean NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1589 (class 1259 OID 35480)
-- Dependencies: 3 1590
-- Name: current_relations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE current_relations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2104 (class 0 OID 0)
-- Dependencies: 1589
-- Name: current_relations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE current_relations_id_seq OWNED BY current_relations.id;


--
-- TOC entry 2105 (class 0 OID 0)
-- Dependencies: 1589
-- Name: current_relations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('current_relations_id_seq', 1, false);


--
-- TOC entry 1595 (class 1259 OID 35528)
-- Dependencies: 3
-- Name: current_way_nodes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_way_nodes (
    id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id bigint NOT NULL
);


--
-- TOC entry 1561 (class 1259 OID 34580)
-- Dependencies: 1874 1875 3
-- Name: current_way_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_way_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1563 (class 1259 OID 34592)
-- Dependencies: 3
-- Name: current_ways; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_ways (
    id bigint NOT NULL,
    changeset_id bigint NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    visible boolean NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1562 (class 1259 OID 34590)
-- Dependencies: 3 1563
-- Name: current_ways_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE current_ways_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2106 (class 0 OID 0)
-- Dependencies: 1562
-- Name: current_ways_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE current_ways_id_seq OWNED BY current_ways.id;


--
-- TOC entry 2107 (class 0 OID 0)
-- Dependencies: 1562
-- Name: current_ways_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('current_ways_id_seq', 1, false);


--
-- TOC entry 1597 (class 1259 OID 35551)
-- Dependencies: 3
-- Name: diary_comments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE diary_comments (
    id bigint NOT NULL,
    diary_entry_id bigint NOT NULL,
    user_id bigint NOT NULL,
    body text NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


--
-- TOC entry 1596 (class 1259 OID 35549)
-- Dependencies: 3 1597
-- Name: diary_comments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE diary_comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2108 (class 0 OID 0)
-- Dependencies: 1596
-- Name: diary_comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE diary_comments_id_seq OWNED BY diary_comments.id;


--
-- TOC entry 2109 (class 0 OID 0)
-- Dependencies: 1596
-- Name: diary_comments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('diary_comments_id_seq', 1, false);


--
-- TOC entry 1565 (class 1259 OID 34600)
-- Dependencies: 3
-- Name: diary_entries; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE diary_entries (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    title character varying(255) NOT NULL,
    body text NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    latitude double precision,
    longitude double precision,
    language_code character varying(255)
);


--
-- TOC entry 1564 (class 1259 OID 34598)
-- Dependencies: 1565 3
-- Name: diary_entries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE diary_entries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2110 (class 0 OID 0)
-- Dependencies: 1564
-- Name: diary_entries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE diary_entries_id_seq OWNED BY diary_entries.id;


--
-- TOC entry 2111 (class 0 OID 0)
-- Dependencies: 1564
-- Name: diary_entries_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('diary_entries_id_seq', 1, false);


--
-- TOC entry 1567 (class 1259 OID 34611)
-- Dependencies: 3
-- Name: friends; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE friends (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    friend_user_id bigint NOT NULL
);


--
-- TOC entry 1566 (class 1259 OID 34609)
-- Dependencies: 3 1567
-- Name: friends_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE friends_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2112 (class 0 OID 0)
-- Dependencies: 1566
-- Name: friends_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE friends_id_seq OWNED BY friends.id;


--
-- TOC entry 2113 (class 0 OID 0)
-- Dependencies: 1566
-- Name: friends_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('friends_id_seq', 1, false);


--
-- TOC entry 1568 (class 1259 OID 34618)
-- Dependencies: 3
-- Name: gps_points; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE gps_points (
    altitude double precision,
    trackid integer NOT NULL,
    latitude integer NOT NULL,
    longitude integer NOT NULL,
    gpx_id bigint NOT NULL,
    "timestamp" timestamp without time zone,
    tile bigint
);


--
-- TOC entry 1570 (class 1259 OID 34626)
-- Dependencies: 1879 3
-- Name: gpx_file_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE gpx_file_tags (
    gpx_id bigint DEFAULT 0 NOT NULL,
    tag character varying(255) NOT NULL,
    id bigint NOT NULL
);


--
-- TOC entry 1569 (class 1259 OID 34624)
-- Dependencies: 1570 3
-- Name: gpx_file_tags_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE gpx_file_tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2114 (class 0 OID 0)
-- Dependencies: 1569
-- Name: gpx_file_tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE gpx_file_tags_id_seq OWNED BY gpx_file_tags.id;


--
-- TOC entry 2115 (class 0 OID 0)
-- Dependencies: 1569
-- Name: gpx_file_tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('gpx_file_tags_id_seq', 1, false);


--
-- TOC entry 1572 (class 1259 OID 34636)
-- Dependencies: 1882 1883 1884 1885 3
-- Name: gpx_files; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE gpx_files (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    visible boolean DEFAULT true NOT NULL,
    name character varying(255) DEFAULT ''::character varying NOT NULL,
    size bigint,
    latitude double precision,
    longitude double precision,
    "timestamp" timestamp without time zone NOT NULL,
    public boolean DEFAULT true NOT NULL,
    description character varying(255) DEFAULT ''::character varying NOT NULL,
    inserted boolean NOT NULL
);


--
-- TOC entry 1571 (class 1259 OID 34634)
-- Dependencies: 1572 3
-- Name: gpx_files_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE gpx_files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2116 (class 0 OID 0)
-- Dependencies: 1571
-- Name: gpx_files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE gpx_files_id_seq OWNED BY gpx_files.id;


--
-- TOC entry 2117 (class 0 OID 0)
-- Dependencies: 1571
-- Name: gpx_files_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('gpx_files_id_seq', 1, false);


--
-- TOC entry 1607 (class 1259 OID 40572)
-- Dependencies: 3
-- Name: languages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE languages (
    code character varying(255) NOT NULL,
    english_name character varying(255) NOT NULL,
    native_name character varying(255)
);


--
-- TOC entry 1574 (class 1259 OID 34659)
-- Dependencies: 1887 3
-- Name: messages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE messages (
    id bigint NOT NULL,
    from_user_id bigint NOT NULL,
    title character varying(255) NOT NULL,
    body text NOT NULL,
    sent_on timestamp without time zone NOT NULL,
    message_read boolean DEFAULT false NOT NULL,
    to_user_id bigint NOT NULL
);


--
-- TOC entry 1573 (class 1259 OID 34657)
-- Dependencies: 3 1574
-- Name: messages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2118 (class 0 OID 0)
-- Dependencies: 1573
-- Name: messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE messages_id_seq OWNED BY messages.id;


--
-- TOC entry 2119 (class 0 OID 0)
-- Dependencies: 1573
-- Name: messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('messages_id_seq', 1, false);


--
-- TOC entry 1601 (class 1259 OID 35594)
-- Dependencies: 1920 1921 3
-- Name: node_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE node_tags (
    id bigint NOT NULL,
    version bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1586 (class 1259 OID 35438)
-- Dependencies: 3
-- Name: nodes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE nodes (
    id bigint NOT NULL,
    latitude integer NOT NULL,
    longitude integer NOT NULL,
    changeset_id bigint NOT NULL,
    visible boolean NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    tile bigint NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1591 (class 1259 OID 35488)
-- Dependencies: 1908 1909 1910 350 3
-- Name: relation_members; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE relation_members (
    id bigint DEFAULT 0 NOT NULL,
    member_id bigint NOT NULL,
    member_role character varying(255) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    member_type nwr_enum NOT NULL,
    sequence_id integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 1592 (class 1259 OID 35496)
-- Dependencies: 1911 1912 1913 3
-- Name: relation_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE relation_tags (
    id bigint DEFAULT 0 NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1593 (class 1259 OID 35506)
-- Dependencies: 1914 1915 3
-- Name: relations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE relations (
    id bigint DEFAULT 0 NOT NULL,
    changeset_id bigint NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    version bigint NOT NULL,
    visible boolean DEFAULT true NOT NULL
);


--
-- TOC entry 1560 (class 1259 OID 34529)
-- Dependencies: 3
-- Name: schema_migrations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE schema_migrations (
    version character varying(255) NOT NULL
);


--
-- TOC entry 1580 (class 1259 OID 35382)
-- Dependencies: 3
-- Name: sessions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sessions (
    id integer NOT NULL,
    session_id character varying(255),
    data text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


--
-- TOC entry 1579 (class 1259 OID 35380)
-- Dependencies: 1580 3
-- Name: sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2120 (class 0 OID 0)
-- Dependencies: 1579
-- Name: sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE sessions_id_seq OWNED BY sessions.id;


--
-- TOC entry 2121 (class 0 OID 0)
-- Dependencies: 1579
-- Name: sessions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sessions_id_seq', 1, false);


--
-- TOC entry 1581 (class 1259 OID 35392)
-- Dependencies: 3
-- Name: user_preferences; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_preferences (
    user_id bigint NOT NULL,
    k character varying(255) NOT NULL,
    v character varying(255) NOT NULL
);


--
-- TOC entry 1583 (class 1259 OID 35402)
-- Dependencies: 3
-- Name: user_tokens; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_tokens (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token character varying(255) NOT NULL,
    expiry timestamp without time zone NOT NULL
);


--
-- TOC entry 1582 (class 1259 OID 35400)
-- Dependencies: 1583 3
-- Name: user_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2122 (class 0 OID 0)
-- Dependencies: 1582
-- Name: user_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_tokens_id_seq OWNED BY user_tokens.id;


--
-- TOC entry 2123 (class 0 OID 0)
-- Dependencies: 1582
-- Name: user_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_tokens_id_seq', 1, false);


--
-- TOC entry 1576 (class 1259 OID 34700)
-- Dependencies: 1889 1890 1891 1892 1893 1894 1895 1896 1897 3
-- Name: users; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE users (
    email character varying(255) NOT NULL,
    id bigint NOT NULL,
    active integer DEFAULT 0 NOT NULL,
    pass_crypt character varying(255) NOT NULL,
    creation_time timestamp without time zone NOT NULL,
    display_name character varying(255) DEFAULT ''::character varying NOT NULL,
    data_public boolean DEFAULT false NOT NULL,
    description text DEFAULT ''::text NOT NULL,
    home_lat double precision,
    home_lon double precision,
    home_zoom smallint DEFAULT 3,
    nearby integer DEFAULT 50,
    pass_salt character varying(255),
    image text,
    administrator boolean DEFAULT false NOT NULL,
    email_valid boolean DEFAULT false NOT NULL,
    new_email character varying(255),
    visible boolean DEFAULT true NOT NULL,
    creation_ip character varying(255),
    languages character varying(255)
);


--
-- TOC entry 1575 (class 1259 OID 34698)
-- Dependencies: 3 1576
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2124 (class 0 OID 0)
-- Dependencies: 1575
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- TOC entry 2125 (class 0 OID 0)
-- Dependencies: 1575
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('users_id_seq', 1, false);


--
-- TOC entry 1594 (class 1259 OID 35523)
-- Dependencies: 3
-- Name: way_nodes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE way_nodes (
    id bigint NOT NULL,
    node_id bigint NOT NULL,
    version bigint NOT NULL,
    sequence_id bigint NOT NULL
);


--
-- TOC entry 1577 (class 1259 OID 34733)
-- Dependencies: 1898 3
-- Name: way_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE way_tags (
    id bigint DEFAULT 0 NOT NULL,
    k character varying(255) NOT NULL,
    v character varying(255) NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1578 (class 1259 OID 34741)
-- Dependencies: 1899 1900 3
-- Name: ways; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE ways (
    id bigint DEFAULT 0 NOT NULL,
    changeset_id bigint NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    version bigint NOT NULL,
    visible boolean DEFAULT true NOT NULL
);


--
-- TOC entry 1917 (class 2604 OID 35571)
-- Dependencies: 1599 1598 1599
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE acls ALTER COLUMN id SET DEFAULT nextval('acls_id_seq'::regclass);


--
-- TOC entry 1922 (class 2604 OID 35700)
-- Dependencies: 1602 1603 1603
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE changesets ALTER COLUMN id SET DEFAULT nextval('changesets_id_seq'::regclass);


--
-- TOC entry 1926 (class 2604 OID 40568)
-- Dependencies: 1606 1605 1606
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE countries ALTER COLUMN id SET DEFAULT nextval('countries_id_seq'::regclass);


--
-- TOC entry 1903 (class 2604 OID 35417)
-- Dependencies: 1584 1585 1585
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE current_nodes ALTER COLUMN id SET DEFAULT nextval('current_nodes_id_seq'::regclass);


--
-- TOC entry 1907 (class 2604 OID 35485)
-- Dependencies: 1589 1590 1590
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE current_relations ALTER COLUMN id SET DEFAULT nextval('current_relations_id_seq'::regclass);


--
-- TOC entry 1876 (class 2604 OID 34595)
-- Dependencies: 1562 1563 1563
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE current_ways ALTER COLUMN id SET DEFAULT nextval('current_ways_id_seq'::regclass);


--
-- TOC entry 1916 (class 2604 OID 35554)
-- Dependencies: 1596 1597 1597
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE diary_comments ALTER COLUMN id SET DEFAULT nextval('diary_comments_id_seq'::regclass);


--
-- TOC entry 1877 (class 2604 OID 34603)
-- Dependencies: 1565 1564 1565
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE diary_entries ALTER COLUMN id SET DEFAULT nextval('diary_entries_id_seq'::regclass);


--
-- TOC entry 1878 (class 2604 OID 34614)
-- Dependencies: 1566 1567 1567
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE friends ALTER COLUMN id SET DEFAULT nextval('friends_id_seq'::regclass);


--
-- TOC entry 1880 (class 2604 OID 34630)
-- Dependencies: 1569 1570 1570
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE gpx_file_tags ALTER COLUMN id SET DEFAULT nextval('gpx_file_tags_id_seq'::regclass);


--
-- TOC entry 1881 (class 2604 OID 34639)
-- Dependencies: 1572 1571 1572
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE gpx_files ALTER COLUMN id SET DEFAULT nextval('gpx_files_id_seq'::regclass);


--
-- TOC entry 1886 (class 2604 OID 34662)
-- Dependencies: 1573 1574 1574
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE messages ALTER COLUMN id SET DEFAULT nextval('messages_id_seq'::regclass);


--
-- TOC entry 1901 (class 2604 OID 35385)
-- Dependencies: 1580 1579 1580
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE sessions ALTER COLUMN id SET DEFAULT nextval('sessions_id_seq'::regclass);


--
-- TOC entry 1902 (class 2604 OID 35405)
-- Dependencies: 1583 1582 1583
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE user_tokens ALTER COLUMN id SET DEFAULT nextval('user_tokens_id_seq'::regclass);


--
-- TOC entry 1888 (class 2604 OID 34703)
-- Dependencies: 1576 1575 1576
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- TOC entry 2084 (class 0 OID 35568)
-- Dependencies: 1599
-- Data for Name: acls; Type: TABLE DATA; Schema: public; Owner: -
--

COPY acls (id, address, netmask, k, v) FROM stdin;
\.


--
-- TOC entry 2088 (class 0 OID 35704)
-- Dependencies: 1604
-- Data for Name: changeset_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY changeset_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2087 (class 0 OID 35697)
-- Dependencies: 1603
-- Data for Name: changesets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY changesets (id, user_id, created_at, min_lat, max_lat, min_lon, max_lon, closed_at, num_changes) FROM stdin;
\.


--
-- TOC entry 2089 (class 0 OID 40565)
-- Dependencies: 1606
-- Data for Name: countries; Type: TABLE DATA; Schema: public; Owner: -
--

COPY countries (id, code, min_lat, max_lat, min_lon, max_lon) FROM stdin;
1	AD	42.435073852539098	42.658699035644503	1.4221107959747299	1.78038918972015
2	AE	22.633327484130898	26.084161758422901	51.583324432372997	56.381664276122997
3	AF	29.377470016479499	38.483425140380902	60.478435516357401	74.879463195800795
4	AG	16.996976852416999	17.7293891906738	-61.906429290771499	-61.672416687011697
5	AI	18.166812896728501	18.28342628479	-63.172904968261697	-62.971351623535199
6	AL	39.648353576660199	42.665615081787102	19.293968200683601	21.068475723266602
7	AM	38.397052764892599	41.301837921142599	43.449775695800803	49.478397369384801
8	AN	12.017148017883301	12.385673522949199	-69.157218933105497	-68.192298889160199
9	AO	-18.042078018188501	-4.3768253326415998	11.679217338561999	24.0821228027344
10	AQ	-89.999908447265597	-60.515525817871101	-179.999923706055	179.999923706055
11	AR	-55.061321258544901	-21.781274795532202	-73.582984924316406	-53.591827392578097
12	AS	-14.382479667663601	-14.1621150970459	-170.84133911132801	-169.41606140136699
13	AT	46.378025054931598	49.017063140869098	9.5359144210815394	17.162725448608398
14	AU	-43.643974304199197	-10.062803268432599	112.91104888916	153.63928222656199
15	AW	12.4060916900635	12.630619049072299	-70.061141967773395	-69.866851806640597
16	AX	59.977935791015597	60.425117492675803	19.649024963378899	20.290779113769499
17	AZ	38.820186614990199	41.905647277832003	44.7741088867188	50.370090484619098
18	BA	42.546104431152301	45.239200592041001	15.7189435958862	19.622226715087901
19	BB	13.039842605590801	13.327258110046399	-59.648929595947301	-59.420372009277301
20	BD	20.743331909179702	26.6319484710693	88.028327941894503	92.673683166503906
21	BE	49.493602752685497	51.505451202392599	2.5469436645507799	6.4038615226745597
22	BF	9.4011068344116193	15.082594871521	-5.5189166069030797	2.40539526939392
23	BG	41.242076873779297	44.217647552490199	22.371162414550799	28.612169265747099
24	BH	25.796859741210898	26.282585144043001	50.454135894775398	50.664478302002003
25	BI	-4.4657135009765598	-2.3101227283477801	28.993057250976602	30.847732543945298
26	BJ	6.22574710845947	12.418348312377899	0.774574935436249	3.8517012596130402
27	BL	17.883939743041999	17.932689666748001	-62.8763618469238	-62.792404174804702
28	BM	32.246631622314503	32.3790092468262	-64.896064758300795	-64.651985168457003
29	BN	4.0030822753906197	5.0471677780151403	114.07143402099599	115.35945892334
30	BO	-22.896135330200199	-9.6805658340454102	-69.640769958496094	-57.458091735839801
31	BR	-33.7507133483887	5.2648777961731001	-73.985549926757798	-32.392993927002003
32	BS	22.852739334106399	26.919246673583999	-78.995925903320298	-74.423866271972699
33	BT	26.707637786865199	28.323780059814499	88.759712219238295	92.125205993652301
34	BV	-54.462387084960902	-54.400318145752003	3.33549857139587	3.48797631263733
35	BW	-26.907249450683601	-17.780809402465799	19.999532699585	29.360784530639599
36	BY	51.2564086914062	56.165813446044901	23.176885604858398	32.770809173583999
37	BZ	15.8892984390259	18.496559143066399	-89.224822998046903	-87.776969909667997
38	CA	41.675975799560497	83.110633850097699	-141.00001525878901	-52.636283874511697
39	CC	-12.185417175293001	-12.114803314209	96.821266174316406	96.936096191406193
40	CD	-13.455676078796399	5.3860988616943404	12.2041425704956	31.305913925170898
41	CF	2.2205135822296098	11.007570266723601	14.4200954437256	27.463424682617202
42	CG	-5.0272235870361301	3.7030823230743399	11.2050075531006	18.6498413085938
43	CH	45.825687408447301	47.805335998535199	5.9574713706970197	10.491473197936999
44	CI	4.3570661544799796	10.7366437911987	-8.59930324554443	-2.4948966503143302
45	CK	-21.944166183471701	-10.0231122970581	-161.093673706055	-157.31211853027301
46	CL	-55.916355133056598	-17.5075492858887	-109.45590209960901	-66.417549133300795
47	CM	1.65254783630371	13.0780572891235	8.4947614669799805	16.1921195983887
48	CN	15.775414466857899	53.560867309570298	73.557678222656193	134.77394104003901
49	CO	-4.22586965560913	13.38050365448	-81.728118896484403	-66.869827270507798
50	CR	8.0329732894897496	11.216820716857899	-85.950637817382798	-82.555976867675795
51	CU	19.828079223632798	23.226045608520501	-84.957443237304702	-74.131767272949205
52	CV	14.808020591735801	17.197181701660199	-25.358749389648398	-22.669439315795898
53	CX	-10.5757246017456	-10.4157762527466	105.538932800293	105.719596862793
54	CY	34.563491821289098	35.701534271240199	32.273078918457003	34.597923278808601
55	CZ	48.581375122070298	51.0536079406738	12.0937032699585	18.852220535278299
56	DE	47.275772094726598	55.055641174316399	5.8656382560729998	15.0398902893066
57	DJ	10.909915924072299	12.706834793090801	41.773468017578097	43.416976928710902
58	DK	54.562381744384801	57.748424530029297	8.0756092071533203	15.1588354110718
59	DM	15.201688766479499	15.6318101882935	-61.484115600585902	-61.244144439697301
60	DO	17.543155670166001	19.9298610687256	-72.003501892089801	-68.319992065429702
61	DZ	18.960025787353501	37.093727111816399	-8.6738691329956108	11.979549407959
62	EC	-4.9988236427307102	1.41893422603607	-91.661888122558594	-75.184577941894503
63	EE	57.516185760497997	59.676231384277301	21.837581634521499	28.209974288940401
64	EG	21.725385665893601	31.6673374176025	24.6981086730957	35.794868469238303
65	EH	20.774154663085898	27.669677734375	-17.103185653686499	-8.6702747344970703
66	ER	12.3595533370972	18.003086090087901	36.438774108886697	43.134647369384801
67	ES	27.638816833496101	43.791725158691399	-18.169641494751001	4.31538963317871
68	ET	3.4024217128753702	14.893751144409199	32.999935150146499	47.986183166503899
69	FI	59.808773040771499	70.096061706542997	19.520717620849599	31.580945968627901
70	FJ	-20.675971984863299	-12.480109214782701	177.12936401367199	-178.42445373535199
71	FK	-52.360519409179702	-51.240642547607401	-61.345199584960902	-57.712478637695298
72	FM	5.2653322219848597	9.6363620758056605	138.052810668945	163.034912109375
73	FO	61.394935607910199	62.400753021240199	-7.4580006599426296	-6.3995823860168501
74	FR	41.371574401855497	51.092811584472699	-5.1422228813171396	9.5615577697753906
75	GA	-3.9788062572479199	2.3226122856140101	8.6954698562622106	14.5023488998413
76	GB	49.9061889648438	60.845809936523402	-8.6235561370849592	1.75900018215179
77	GD	11.992372512817401	12.3193960189819	-61.799980163574197	-61.573825836181598
78	GE	41.053192138671903	43.586502075195298	40.0101318359375	46.725975036621101
79	GF	2.1270935535430899	5.7764968872070304	-54.542518615722699	-51.613945007324197
80	GG	49.422412872314503	49.514698028564503	-2.6824724674224898	-2.5091106891632098
81	GH	4.7367224693298304	11.173302650451699	-3.25542044639587	1.1917811632156401
82	GI	36.1124076843262	36.1598091125488	-5.3572506904602104	-5.33963823318481
83	GL	59.777397155761697	83.627365112304702	-73.042037963867202	-11.3123178482056
84	GM	13.064250946044901	13.8265724182129	-16.8250827789307	-13.7977914810181
85	GN	7.1935524940490696	12.676221847534199	-14.9266204833984	-7.6410703659057599
86	GP	15.8675632476807	16.516851425170898	-61.544769287109403	-60.999996185302699
87	GQ	0.92085993289947499	2.3469893932342498	9.34686374664307	11.3357257843018
88	GR	34.809635162353501	41.757423400878899	19.374443054199201	28.246391296386701
89	GS	-59.479263305664098	-53.970462799072301	-38.0211791992188	-26.229322433471701
90	GT	13.737300872802701	17.815223693847699	-92.236305236816406	-88.223190307617202
91	GU	13.2406091690063	13.6523342132568	144.619216918945	144.95399475097699
92	GW	10.924263954162599	12.6807909011841	-16.717536926269499	-13.6365203857422
93	GY	1.1750798225402801	8.5575685501098597	-61.384769439697301	-56.4802436828613
94	HK	22.153247833251999	22.559780120849599	113.83773803710901	114.434761047363
95	HM	-53.192005157470703	-52.909408569335902	72.596527099609403	73.859153747558594
96	HN	12.982409477233899	16.510259628295898	-89.350807189941406	-83.155387878417997
97	HR	42.435882568359403	46.5387573242188	13.4932203292847	19.427391052246101
98	HT	18.0210285186768	20.087821960449201	-74.478591918945298	-71.613349914550795
99	HU	45.743602752685497	48.5856742858887	16.1118869781494	22.906002044677699
100	ID	-10.9418621063232	5.9044175148010298	95.009323120117202	141.02183532714801
101	IE	51.451580047607401	55.387924194335902	-10.478557586669901	-6.0023884773254403
102	IL	29.496635437011701	33.340141296386697	34.230442047119098	35.876808166503899
103	IM	54.055912017822301	54.419731140136697	-4.7987227439880398	-4.3114991188049299
104	IN	6.7471385002136204	35.504230499267599	68.186676025390597	97.403312683105497
105	IO	-7.4380288124084499	-5.26833248138428	71.259963989257798	72.493171691894503
106	IQ	29.069442749023398	37.378036499023402	38.795883178710902	48.575920104980497
107	IR	25.064079284668001	39.777229309082003	44.047271728515597	63.317478179931598
108	IS	63.393245697021499	66.534645080566406	-24.546525955200199	-13.495813369751
109	IT	36.652774810791001	47.095203399658203	6.6148881912231401	18.51344871521
110	JE	49.169826507568402	49.265064239502003	-2.2600283622741699	-2.0220825672149698
111	JM	17.703550338745099	18.526979446411101	-78.366645812988295	-76.180313110351605
112	JO	29.185884475708001	33.367671966552699	34.959991455078097	39.301174163818402
113	JP	24.249469757080099	45.523147583007798	122.938522338867	145.82090759277301
114	KE	-4.6780476570129403	5.0199389457702601	33.908851623535199	41.899082183837898
115	KG	39.172824859619098	43.238227844238303	69.276596069335895	80.283180236816406
116	KH	10.409081459045399	14.686418533325201	102.339981079102	107.627738952637
117	KI	-11.437039375305201	1.9487781524658201	172.95521545410199	-151.80386352539099
118	KM	-12.3878583908081	-11.362380027771	43.215785980224602	44.5382270812988
119	KN	17.095340728759801	17.420120239257798	-62.869564056396499	-62.543258666992202
120	KP	37.673324584960902	43.006061553955099	124.315872192383	130.67489624023401
121	KR	33.190940856933601	38.612453460693402	125.887100219727	129.58468627929699
122	KW	28.524608612060501	30.095947265625	46.555549621582003	48.431480407714801
123	KY	19.263025283813501	19.761703491210898	-81.432785034179702	-79.727256774902301
124	KZ	40.936328887939503	55.451202392578097	46.491851806640597	87.312683105468807
125	LA	13.9100255966187	22.500391006469702	100.093048095703	107.69703674316401
126	LB	33.053855895996101	34.6914253234863	35.114273071289098	36.639198303222699
127	LC	13.704776763916	14.1032466888428	-61.074153900146499	-60.874198913574197
128	LI	47.055854797363303	47.273532867431598	9.4778032302856392	9.6321964263915998
129	LK	5.9168324470520002	9.8313627243041992	79.652908325195298	81.881294250488295
130	LR	4.3530564308166504	8.5517921447753906	-11.4920845031738	-7.3651123046875
131	LS	-30.668966293335	-28.572055816650401	27.029066085815401	29.465763092041001
132	LT	53.901298522949197	56.446922302246101	20.941524505615199	26.871946334838899
133	LU	49.446578979492202	50.184947967529297	5.7345552444457999	6.5284729003906197
134	LV	55.668853759765597	58.082313537597699	20.9742736816406	28.241168975830099
135	LY	19.508041381835898	33.1690063476562	9.3870182037353498	25.1506156921387
136	MA	27.662111282348601	35.928031921386697	-13.1685876846313	-0.99174988269805897
137	MC	43.727542877197301	43.7730522155762	7.3863883018493697	7.4392933845520002
138	MD	45.468879699707003	48.4901733398438	26.618940353393601	30.135448455810501
139	ME	41.8501586914062	43.570140838622997	18.4613037109375	20.3588352203369
140	MF	18.052228927612301	18.130357742309599	-63.1527709960938	-63.012989044189503
141	MG	-25.608955383300799	-11.945431709289601	43.224868774414098	50.483787536621101
142	MH	5.5876383781433097	14.620001792907701	165.52490234375	171.93182373046901
143	MK	40.860187530517599	42.361812591552699	20.464693069458001	23.038141250610401
144	ML	10.1595115661621	25.000005722045898	-12.2426156997681	4.2449688911437997
145	MM	9.7845811843872106	28.543251037597699	92.189270019531193	101.176795959473
146	MN	41.567630767822301	52.154254913330099	87.749649047851605	119.924324035645
147	MO	22.180385589599599	22.222337722778299	113.528938293457	113.565841674805
148	MP	14.1073598861694	18.8050861358643	145.12680053710901	145.84942626953099
149	MQ	14.392260551452599	14.8788204193115	-61.230125427246101	-60.815505981445298
150	MR	14.7155456542969	27.2980766296387	-17.066524505615199	-4.8276734352111799
151	MS	16.671003341674801	16.8173313140869	-62.242588043212898	-62.146415710449197
152	MT	35.810268402099602	36.08203125	14.1915826797485	14.577640533447299
153	MU	-20.525720596313501	-10.3192539215088	56.512710571289098	63.5001831054688
154	MV	-0.69269406795501698	7.0983614921569798	72.693206787109403	73.637290954589801
155	MW	-17.125001907348601	-9.3675394058227504	32.673942565917997	35.916828155517599
156	MX	14.532864570617701	32.716766357421903	-118.453964233398	-86.703376770019503
157	MY	0.855221927165985	7.3634176254272496	99.643440246582003	119.26751708984401
158	MZ	-26.868688583373999	-10.471881866455099	30.2173156738281	40.843002319335902
159	NA	-28.971433639526399	-16.9598903656006	11.715628623962401	25.2567043304443
160	NC	-22.698003768920898	-19.549776077270501	163.56465148925801	168.129150390625
161	NE	11.696973800659199	23.525028228759801	0.166249975562096	15.995644569396999
162	NF	-29.052726745605501	-28.992387771606399	167.91638183593801	167.99998474121099
163	NG	4.2771434783935502	13.8920087814331	2.6684317588806201	14.6800746917725
164	NI	10.7075414657593	15.025910377502401	-87.690322875976605	-82.73828125
165	NL	50.753913879394503	53.512203216552699	3.3625557422637899	7.22794485092163
166	NO	57.977912902832003	71.188117980957003	4.6501665115356401	30.945558547973601
167	NP	26.3567199707031	30.433393478393601	80.056259155273395	88.1993408203125
168	NR	-0.55233311653137196	-0.50430589914321899	166.89901733398401	166.9453125
169	NU	-19.1455593109131	-18.963331222534201	-169.95307922363301	-169.78138732910199
170	NZ	-52.607585906982401	-29.241094589233398	165.996170043945	-176.27584838867199
171	OM	16.645746231079102	26.387975692748999	51.881996154785199	59.836585998535199
172	PA	7.1979050636291504	9.6375150680541992	-83.051452636718807	-77.174095153808594
173	PE	-18.3497314453125	-0.012976998463273	-81.326751708984403	-68.677970886230497
174	PF	-27.6535739898682	-7.9035720825195304	-152.877197265625	-134.92980957031199
175	PG	-11.657862663269	-1.3186388015747099	140.842849731445	155.96347045898401
176	PH	4.6433053016662598	21.120613098144499	116.93154907226599	126.601531982422
177	PK	23.786718368530298	37.097003936767599	60.878608703613303	77.840927124023395
178	PL	49.006359100341797	54.839141845703097	14.122998237609901	24.150751113891602
179	PM	46.786033630371101	47.146289825439503	-56.420661926269503	-56.252986907958999
180	PN	-24.6725673675537	-24.315862655639599	-128.34646606445301	-124.772834777832
181	PR	17.9264030456543	18.520168304443398	-67.942733764648395	-65.242729187011705
182	PS	31.216539382934599	32.546390533447301	34.2166557312012	35.573299407958999
183	PT	36.980659484863303	42.145645141601598	-9.4959449768066406	-6.1826934814453098
184	PW	6.8862771987915004	7.7321119308471697	134.12319946289099	134.653732299805
185	PY	-27.608741760253899	-19.294038772583001	-62.647083282470703	-54.259349822997997
186	QA	24.4829406738281	26.1547241210938	50.757213592529297	51.636646270752003
187	RE	-21.3722133636475	-20.8568515777588	55.219081878662102	55.845043182372997
188	RO	43.627296447753899	48.266952514648402	20.269969940185501	29.691057205200199
189	RS	41.855823516845703	46.181392669677699	18.817018508911101	23.004999160766602
190	RU	41.188858032226598	81.857376098632798	19.25	-169.05000000000001
191	RW	-2.8406794071197501	-1.0534808635711701	28.8567905426025	30.895961761474599
192	SA	15.6142482757568	32.158340454101598	34.495685577392599	55.666587829589801
193	SB	-11.8505563735962	-6.5896100997924796	155.50859069824199	166.980880737305
194	SC	-9.7538681030273402	-4.2837162017822301	46.204761505127003	56.279514312744098
195	SD	3.48638963699341	23.146892547607401	21.838943481445298	38.580036163330099
196	SE	55.337104797363303	69.062507629394503	11.1186923980713	24.160892486572301
197	SG	1.25855576992035	1.4712781906127901	103.638259887695	104.00747680664099
198	SH	-16.019546508789102	-7.8878145217895499	-14.421231269836399	-5.6387524604797399
199	SI	45.413131713867202	46.877922058105497	13.3830814361572	16.566003799438501
200	SJ	79.220291137695298	80.762100219726605	17.699386596679702	33.287338256835902
201	SK	47.728103637695298	49.603172302246101	16.8477478027344	22.5704460144043
202	SL	6.9296102523803702	10.0000009536743	-13.3076324462891	-10.284236907959
203	SM	43.897911071777301	43.999809265136697	12.401859283447299	12.515556335449199
204	SN	12.307273864746101	16.691635131835898	-17.5352382659912	-11.3558855056763
205	SO	-1.6748682260513299	11.9791669845581	40.986587524414098	51.412643432617202
206	SR	1.8311448097228999	6.0045466423034703	-58.0865669250488	-53.977485656738303
207	ST	0.024765998125076301	1.7013231515884399	6.4701690673828098	7.4663748741149902
208	SV	13.148677825927701	14.445068359375	-90.128669738769503	-87.692153930664105
209	SY	32.310657501220703	37.319145202636697	35.727214813232401	42.385036468505902
210	SZ	-27.317104339599599	-25.7196445465088	30.794103622436499	32.137264251708999
211	TC	21.422622680664102	21.9618816375732	-72.483879089355497	-71.123634338378906
212	TD	7.4410672187805202	23.450372695922901	13.473473548889199	24.00266456604
213	TF	-49.735191345214801	-37.790718078613303	50.170253753662102	77.598815917968807
214	TG	6.1044163703918501	11.1389780044556	-0.14732402563095101	1.8066931962966899
215	TH	5.6099991798400897	20.463197708129901	97.345626831054702	105.63939666748
216	TJ	36.674129486083999	41.042259216308601	67.387123107910199	75.137229919433594
217	TK	-9.3811120986938494	-8.5536127090454102	-172.50035095214801	-171.21141052246099
218	TL	-9.4636278152465803	-8.1358327865600604	124.046089172363	127.308601379395
219	TM	35.141078948974602	47.015617370605497	46.684604644775398	66.684310913085895
220	TN	30.240413665771499	37.543922424316399	7.5248322486877397	11.598278999328601
221	TO	-21.4550590515137	-15.562986373901399	-175.68228149414099	-173.90756225585901
222	TR	35.8154106140137	42.107620239257798	25.668498992919901	44.835002899169901
223	TT	10.0361032485962	11.3383436203003	-61.923778533935497	-60.517929077148402
224	TV	-7.4943618774414098	-5.6687493324279803	176.11897277832	178.69947814941401
225	TW	21.9018039703369	25.298252105712901	119.53468322753901	122.000457763672
226	TZ	-11.7456970214844	-0.99073588848114003	29.327165603637699	40.443225860595703
227	UA	44.390407562255902	52.369369506835902	22.128885269165	40.2073974609375
228	UG	-1.4840501546859699	4.2144279479980504	29.573249816894499	35.036056518554702
229	UM	-0.38900604844093301	18.4209995269775	-176.64511108398401	-74.999992370605497
230	US	24.544242858886701	49.388618469238303	-124.733261108398	-66.954795837402301
231	UY	-34.980823516845703	-30.082221984863299	-58.442726135253899	-53.073928833007798
232	UZ	37.184436798095703	45.575008392333999	55.996631622314503	73.132286071777301
233	VA	41.900272369384801	41.907444000244098	12.4457054138184	12.458376884460399
234	VC	12.5810098648071	13.377835273742701	-61.459259033203097	-61.1138725280762
235	VE	0.62631088495254505	12.201904296875	-73.354087829589801	-59.803775787353501
236	VG	18.389976501464801	18.7572231292725	-64.715377807617202	-64.268753051757798
237	VI	17.6817226409912	18.391750335693398	-65.038246154785199	-64.565170288085895
238	VN	8.5596094131469709	23.388837814331101	102.14821624755901	109.464653015137
239	VU	-20.248947143554702	-13.073442459106399	166.52496337890599	169.90480041503901
240	WF	-14.3286018371582	-13.2142496109009	-178.20680236816401	-176.12875366210901
241	WS	-14.040940284729	-13.4322052001953	-172.79861450195301	-171.41572570800801
242	YE	12.111081123352101	19.002336502075199	42.532524108886697	54.530532836914098
243	YT	-13.0001335144043	-12.648889541626	45.037952423095703	45.292957305908203
244	ZA	-34.839832305908203	-22.126609802246101	16.4580173492432	32.8959770202637
245	ZM	-18.079475402831999	-8.2243585586547905	21.9993686676025	33.705711364746101
246	ZW	-22.417741775512699	-15.608833312988301	25.237024307251001	33.056312561035199
\.


--
-- TOC entry 2085 (class 0 OID 35586)
-- Dependencies: 1600
-- Data for Name: current_node_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_node_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2073 (class 0 OID 35414)
-- Dependencies: 1585
-- Data for Name: current_nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_nodes (id, latitude, longitude, changeset_id, visible, "timestamp", tile, version) FROM stdin;
\.


--
-- TOC entry 2075 (class 0 OID 35459)
-- Dependencies: 1587
-- Data for Name: current_relation_members; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_relation_members (id, member_id, member_role, member_type, sequence_id) FROM stdin;
\.


--
-- TOC entry 2076 (class 0 OID 35470)
-- Dependencies: 1588
-- Data for Name: current_relation_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_relation_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2077 (class 0 OID 35482)
-- Dependencies: 1590
-- Data for Name: current_relations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_relations (id, changeset_id, "timestamp", visible, version) FROM stdin;
\.


--
-- TOC entry 2082 (class 0 OID 35528)
-- Dependencies: 1595
-- Data for Name: current_way_nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_way_nodes (id, node_id, sequence_id) FROM stdin;
\.


--
-- TOC entry 2059 (class 0 OID 34580)
-- Dependencies: 1561
-- Data for Name: current_way_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_way_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2060 (class 0 OID 34592)
-- Dependencies: 1563
-- Data for Name: current_ways; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_ways (id, changeset_id, "timestamp", visible, version) FROM stdin;
\.


--
-- TOC entry 2083 (class 0 OID 35551)
-- Dependencies: 1597
-- Data for Name: diary_comments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY diary_comments (id, diary_entry_id, user_id, body, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 2061 (class 0 OID 34600)
-- Dependencies: 1565
-- Data for Name: diary_entries; Type: TABLE DATA; Schema: public; Owner: -
--

COPY diary_entries (id, user_id, title, body, created_at, updated_at, latitude, longitude, language_code) FROM stdin;
\.


--
-- TOC entry 2062 (class 0 OID 34611)
-- Dependencies: 1567
-- Data for Name: friends; Type: TABLE DATA; Schema: public; Owner: -
--

COPY friends (id, user_id, friend_user_id) FROM stdin;
\.


--
-- TOC entry 2063 (class 0 OID 34618)
-- Dependencies: 1568
-- Data for Name: gps_points; Type: TABLE DATA; Schema: public; Owner: -
--

COPY gps_points (altitude, trackid, latitude, longitude, gpx_id, "timestamp", tile) FROM stdin;
\.


--
-- TOC entry 2064 (class 0 OID 34626)
-- Dependencies: 1570
-- Data for Name: gpx_file_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY gpx_file_tags (gpx_id, tag, id) FROM stdin;
\.


--
-- TOC entry 2065 (class 0 OID 34636)
-- Dependencies: 1572
-- Data for Name: gpx_files; Type: TABLE DATA; Schema: public; Owner: -
--

COPY gpx_files (id, user_id, visible, name, size, latitude, longitude, "timestamp", public, description, inserted) FROM stdin;
\.


--
-- TOC entry 2090 (class 0 OID 40572)
-- Dependencies: 1607
-- Data for Name: languages; Type: TABLE DATA; Schema: public; Owner: -
--

COPY languages (code, english_name, native_name) FROM stdin;
zu	Zulu	isiZulu
sa	Sanskrit	संस्कृतम्
rn	Kirundi	kiRundi
f	Norwegian	Norsk
is	Icelandic	Íslenska
ty	Tahitian	Reo Mā`ohi
ro	Romanian	română
it	Italian	Italiano
am	Amharic	አማርኛ
ba	Bashkir	башҡорт теле
sc	Sardinian	sardu
iu	Inuktitut	ᐃᓄᒃᑎᑐᑦ
an	Aragonese	Aragonés
cy	Welsh	Cymraeg
mg	Malagasy	Malagasy fiteny
sd	Sindhi	सिन्धी; سنڌي، سندھی‎
or	Oriya	ଓଡ଼ିଆ
lt	Lithuanian	lietuvių kalba
mh	Marshallese	Kajin M̧ajeļ
se	Northern Sami	Davvisámegiella
os	Ossetian	Ирон æвзаг
fy	Western Frisian	Frysk
gl	Galician	Galego
lu	Luba-Katanga	\N
mi	Māori	te reo Māori
lv	Latvian	latviešu valoda
ha	Hausa	هَوُسَ
ve	Venda	Tshivenḓa
ur	Urdu	اردو
sg	Sango	yângâ tî sängö
pi	Pāli	पाऴि
ar	Arabic	العربية
be	Belarusian	Беларуская
gn	Guaraní	Avañe'ẽ
mk	Macedonian	македонски јазик
ka	Georgian	ქართული
ml	Malayalam	മലയാളം
as	Assamese	অসমীয়া
si	Sinhala	සිංහල
ru	Russian	русский язык
bg	Bulgarian	български език
ee	Ewe	Ɛʋɛgbɛ
pl	Polish	polski
bh	Bihari	भोजपुरी
he	Hebrew	עברית
na	Nauru	Ekakairũ Naoero
vi	Vietnamese	Tiếng Việt
sk	Slovak	slovenčina
rw	Kinyarwanda	Ikinyarwanda
bi	Bislama	Bislama
mn	Mongolian	Монгол
nb	Norwegian Bokmål	Norsk bokmål
av	Avaric	авар мацӀ; магӀарул мацӀ
sl	Slovenian	slovenščina
yi	Yiddish	ייִדיש
sm	Samoan	gagana fa'a Samoa
dv	Divehi	ދިވެހި
nd	North Ndebele	isiNdebele
ta	Tamil	தமிழ்
sn	Shona	chiShona
gu	Gujarati	ગુજરાતી
ne	Nepali	नेपाली
ay	Aymara	aymar aru
kg	Kongo	KiKongo
so	Somali	Soomaaliga; af Soomaali
ca	Catalan	Català
hi	Hindi	हिन्दी; हिंदी
mr	Marathi	मराठी
az	Azerbaijani	azərbaycan dili
bm	Bambara	bamanankan
gv	Manx	Gaelg; Gailck
wa	Walloon	Walon
uz	Uzbek	O'zbek; Ўзбек; أۇزبېك‎
ng	Ndonga	Owambo
ms	Malay	bahasa Melayu; بهاس ملايو‎
el	Greek	Ελληνικά
bn	Bengali	বাংলা
ki	Kikuyu	Gĩkũyũ
vo	Volapük	Volapük
sq	Albanian	Shqip
dz	Dzongkha	རྫོང་ཁ
mt	Maltese	Malti
jv	Javanese	basa Jawa
fa	Persian	فارسی
bo	Tibetan	བོད་ཡིག
kj	Kwanyama	Kuanyama
za	Zhuang	Saɯ cueŋƅ; Saw cuengh
te	Telugu	తెలుగు
sr	Serbian	српски језик
ps	Pashto	پښتو
en	English	English
yo	Yoruba	Yorùbá
pt	Portuguese	Português
ia	Interlingua (International Auxiliary Language Association)	Interlingua
eo	Esperanto	Esperanto
kk	Kazakh	Қазақ тілі
tg	Tajik	тоҷикӣ; toğikī; تاجیکی‎
ss	Swati	SiSwati
br	Breton	brezhoneg
kl	Kalaallisut	kalaallisut; kalaallit oqaasii
ce	Chechen	нохчийн мотт
th	Thai	ไทย
st	Southern Sotho	Sesotho
nl	Dutch	Nederlands
bs	Bosnian	bosanski jezik
km	Khmer	ភាសាខ្មែរ
la	Latin	latine; lingua latina
ho	Hiri Motu	Hiri Motu
ti	Tigrinya	ትግርኛ
su	Sundanese	Basa Sunda
id	Indonesian	Bahasa Indonesia
kn	Kannada	ಕನ್ನಡ
lb	Luxembourgish	Lëtzebuergesch
my	Burmese	ဗမာစာ
sv	Swedish	svenska
es	Spanish	Español; castellano
ff	Fulah	Fulfulde
ko	Korean	한국어 (韓國語); 조선말 (朝鮮語)
ch	Chamorro	Chamoru
ie	Interlingue	Interlingue
tk	Turkmen	Türkmen; Түркмен
sw	Swahili	Kiswahili
nn	Norwegian Nynorsk	Norsk nynorsk
et	Estonian	eesti; eesti keel
hr	Croatian	Hrvatski
zh	Chinese	中文 (Zhōngwén), 汉语, 漢語
tl	Tagalog	Tagalog
oc	Occitan	Occitan
ig	Igbo	Igbo
eu	Basque	euskara; euskera
fi	Finnish	suomi; suomen kieli
kr	Kanuri	Kanuri
ht	Haitian	Kreyòl ayisyen
tn	Tswana	Setswana
fj	Fijian	vosa Vakaviti
ks	Kashmiri	कश्मीरी; كشميري‎
lg	Ganda	Luganda
hu	Hungarian	Magyar
aa	Afar	Afaraf
to	Tonga	faka Tonga
nr	South Ndebele	isiNdebele
da	Danish	dansk
ii	Sichuan Yi	ꆇꉙ
ab	Abkhazian	Аҧсуа
ku	Kurdish	Kurdî; كوردی‎
li	Limburgish	Limburgs
wo	Wolof	Wollof
kv	Komi	коми кыв
ga	Irish	Gaeilge
co	Corsican	corsu; lingua corsa
ik	Inupiaq	Iñupiaq; Iñupiatun
tr	Turkish	Türkçe
kw	Cornish	Kernewek
hy	Armenian	Հայերեն
ae	Avestan	avesta
ts	Tsonga	Xitsonga
oj	Ojibwa	ᐊᓂᔑᓈᐯᒧᐎᓐ
nv	Navajo	Diné bizaad; Dinékʼehǰí
hz	Herero	Otjiherero
af	Afrikaans	Afrikaans
ja	Japanese	日本語 (にほんご／にっぽんご)
fo	Faroese	Føroyskt
ug	Uighur	Uyƣurqə; ئۇيغۇرچە‎
qu	Quechua	Runa Simi; Kichwa
cr	Cree	ᓀᐦᐃᔭᐍᐏᐣ
de	German	Deutsch
ky	Kirghiz	кыргыз тили
gd	Scottish Gaelic	Gàidhlig
tt	Tatar	татарча; tatarça; تاتارچا‎
cs	Czech	česky; čeština
io	Ido	Ido
om	Oromo	Afaan Oromoo
ny	Chichewa	chiCheŵa; chinyanja
ln	Lingala	Lingála
fr	French	Français; langue française
xh	Xhosa	isiXhosa
pa	Panjabi	ਪੰਜਾਬੀ; پنجابی‎
cu	Church Slavic	ѩзыкъ словѣньскъ
lo	Lao	ພາສາລາວ
uk	Ukrainian	Українська
tw	Twi	Twi
rm	Raeto-Romance	rumantsch grischun
ak	Akan	Akan
cv	Chuvash	чӑваш чӗлхи
\.


--
-- TOC entry 2066 (class 0 OID 34659)
-- Dependencies: 1574
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: -
--

COPY messages (id, from_user_id, title, body, sent_on, message_read, to_user_id) FROM stdin;
\.


--
-- TOC entry 2086 (class 0 OID 35594)
-- Dependencies: 1601
-- Data for Name: node_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY node_tags (id, version, k, v) FROM stdin;
\.


--
-- TOC entry 2074 (class 0 OID 35438)
-- Dependencies: 1586
-- Data for Name: nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY nodes (id, latitude, longitude, changeset_id, visible, "timestamp", tile, version) FROM stdin;
\.


--
-- TOC entry 2078 (class 0 OID 35488)
-- Dependencies: 1591
-- Data for Name: relation_members; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relation_members (id, member_id, member_role, version, member_type, sequence_id) FROM stdin;
\.


--
-- TOC entry 2079 (class 0 OID 35496)
-- Dependencies: 1592
-- Data for Name: relation_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relation_tags (id, k, v, version) FROM stdin;
\.


--
-- TOC entry 2080 (class 0 OID 35506)
-- Dependencies: 1593
-- Data for Name: relations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relations (id, changeset_id, "timestamp", version, visible) FROM stdin;
\.


--
-- TOC entry 2058 (class 0 OID 34529)
-- Dependencies: 1560
-- Data for Name: schema_migrations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY schema_migrations (version) FROM stdin;
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
\.


--
-- TOC entry 2070 (class 0 OID 35382)
-- Dependencies: 1580
-- Data for Name: sessions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sessions (id, session_id, data, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 2071 (class 0 OID 35392)
-- Dependencies: 1581
-- Data for Name: user_preferences; Type: TABLE DATA; Schema: public; Owner: -
--

COPY user_preferences (user_id, k, v) FROM stdin;
\.


--
-- TOC entry 2072 (class 0 OID 35402)
-- Dependencies: 1583
-- Data for Name: user_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY user_tokens (id, user_id, token, expiry) FROM stdin;
\.


--
-- TOC entry 2067 (class 0 OID 34700)
-- Dependencies: 1576
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY users (email, id, active, pass_crypt, creation_time, display_name, data_public, description, home_lat, home_lon, home_zoom, nearby, pass_salt, image, administrator, email_valid, new_email, visible, creation_ip, languages) FROM stdin;
\.


--
-- TOC entry 2081 (class 0 OID 35523)
-- Dependencies: 1594
-- Data for Name: way_nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY way_nodes (id, node_id, version, sequence_id) FROM stdin;
\.


--
-- TOC entry 2068 (class 0 OID 34733)
-- Dependencies: 1577
-- Data for Name: way_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY way_tags (id, k, v, version) FROM stdin;
\.


--
-- TOC entry 2069 (class 0 OID 34741)
-- Dependencies: 1578
-- Data for Name: ways; Type: TABLE DATA; Schema: public; Owner: -
--

COPY ways (id, changeset_id, "timestamp", version, visible) FROM stdin;
\.


--
-- TOC entry 2009 (class 2606 OID 35576)
-- Dependencies: 1599 1599
-- Name: acls_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY acls
    ADD CONSTRAINT acls_pkey PRIMARY KEY (id);


--
-- TOC entry 2018 (class 2606 OID 35703)
-- Dependencies: 1603 1603
-- Name: changesets_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY changesets
    ADD CONSTRAINT changesets_pkey PRIMARY KEY (id);


--
-- TOC entry 2023 (class 2606 OID 40570)
-- Dependencies: 1606 1606
-- Name: countries_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);


--
-- TOC entry 2011 (class 2606 OID 35627)
-- Dependencies: 1600 1600 1600
-- Name: current_node_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_node_tags
    ADD CONSTRAINT current_node_tags_pkey PRIMARY KEY (id, k);


--
-- TOC entry 1973 (class 2606 OID 35423)
-- Dependencies: 1585 1585
-- Name: current_nodes_pkey1; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_nodes
    ADD CONSTRAINT current_nodes_pkey1 PRIMARY KEY (id);


--
-- TOC entry 1983 (class 2606 OID 35748)
-- Dependencies: 1587 1587 1587 1587 1587 1587
-- Name: current_relation_members_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_relation_members
    ADD CONSTRAINT current_relation_members_pkey PRIMARY KEY (id, member_type, member_id, member_role, sequence_id);


--
-- TOC entry 1985 (class 2606 OID 35631)
-- Dependencies: 1588 1588 1588
-- Name: current_relation_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_relation_tags
    ADD CONSTRAINT current_relation_tags_pkey PRIMARY KEY (id, k);


--
-- TOC entry 1987 (class 2606 OID 35487)
-- Dependencies: 1590 1590
-- Name: current_relations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_relations
    ADD CONSTRAINT current_relations_pkey PRIMARY KEY (id);


--
-- TOC entry 2003 (class 2606 OID 35532)
-- Dependencies: 1595 1595 1595
-- Name: current_way_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_way_nodes
    ADD CONSTRAINT current_way_nodes_pkey PRIMARY KEY (id, sequence_id);


--
-- TOC entry 1929 (class 2606 OID 35629)
-- Dependencies: 1561 1561 1561
-- Name: current_way_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_way_tags
    ADD CONSTRAINT current_way_tags_pkey PRIMARY KEY (id, k);


--
-- TOC entry 1931 (class 2606 OID 34597)
-- Dependencies: 1563 1563
-- Name: current_ways_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_ways
    ADD CONSTRAINT current_ways_pkey PRIMARY KEY (id);


--
-- TOC entry 2006 (class 2606 OID 35559)
-- Dependencies: 1597 1597
-- Name: diary_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY diary_comments
    ADD CONSTRAINT diary_comments_pkey PRIMARY KEY (id);


--
-- TOC entry 1934 (class 2606 OID 34608)
-- Dependencies: 1565 1565
-- Name: diary_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY diary_entries
    ADD CONSTRAINT diary_entries_pkey PRIMARY KEY (id);


--
-- TOC entry 1936 (class 2606 OID 34616)
-- Dependencies: 1567 1567
-- Name: friends_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY friends
    ADD CONSTRAINT friends_pkey PRIMARY KEY (id);


--
-- TOC entry 1943 (class 2606 OID 34632)
-- Dependencies: 1570 1570
-- Name: gpx_file_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY gpx_file_tags
    ADD CONSTRAINT gpx_file_tags_pkey PRIMARY KEY (id);


--
-- TOC entry 1946 (class 2606 OID 34648)
-- Dependencies: 1572 1572
-- Name: gpx_files_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY gpx_files
    ADD CONSTRAINT gpx_files_pkey PRIMARY KEY (id);


--
-- TOC entry 2025 (class 2606 OID 40579)
-- Dependencies: 1607 1607
-- Name: languages_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY languages
    ADD CONSTRAINT languages_pkey PRIMARY KEY (code);


--
-- TOC entry 1951 (class 2606 OID 34669)
-- Dependencies: 1574 1574
-- Name: messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- TOC entry 2013 (class 2606 OID 35633)
-- Dependencies: 1601 1601 1601 1601
-- Name: node_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY node_tags
    ADD CONSTRAINT node_tags_pkey PRIMARY KEY (id, version, k);


--
-- TOC entry 1978 (class 2606 OID 35639)
-- Dependencies: 1586 1586 1586
-- Name: nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT nodes_pkey PRIMARY KEY (id, version);


--
-- TOC entry 1991 (class 2606 OID 35745)
-- Dependencies: 1591 1591 1591 1591 1591 1591 1591
-- Name: relation_members_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relation_members
    ADD CONSTRAINT relation_members_pkey PRIMARY KEY (id, version, member_type, member_id, member_role, sequence_id);


--
-- TOC entry 1993 (class 2606 OID 35637)
-- Dependencies: 1592 1592 1592 1592
-- Name: relation_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relation_tags
    ADD CONSTRAINT relation_tags_pkey PRIMARY KEY (id, version, k);


--
-- TOC entry 1996 (class 2606 OID 35618)
-- Dependencies: 1593 1593 1593
-- Name: relations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_pkey PRIMARY KEY (id, version);


--
-- TOC entry 1964 (class 2606 OID 35390)
-- Dependencies: 1580 1580
-- Name: sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (id);


--
-- TOC entry 1967 (class 2606 OID 35399)
-- Dependencies: 1581 1581 1581
-- Name: user_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_preferences
    ADD CONSTRAINT user_preferences_pkey PRIMARY KEY (user_id, k);


--
-- TOC entry 1969 (class 2606 OID 35407)
-- Dependencies: 1583 1583
-- Name: user_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_tokens
    ADD CONSTRAINT user_tokens_pkey PRIMARY KEY (id);


--
-- TOC entry 1956 (class 2606 OID 34715)
-- Dependencies: 1576 1576
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 2000 (class 2606 OID 35527)
-- Dependencies: 1594 1594 1594 1594
-- Name: way_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT way_nodes_pkey PRIMARY KEY (id, version, sequence_id);


--
-- TOC entry 1958 (class 2606 OID 35635)
-- Dependencies: 1577 1577 1577 1577
-- Name: way_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY way_tags
    ADD CONSTRAINT way_tags_pkey PRIMARY KEY (id, version, k);


--
-- TOC entry 1961 (class 2606 OID 35609)
-- Dependencies: 1578 1578 1578
-- Name: ways_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT ways_pkey PRIMARY KEY (id, version);


--
-- TOC entry 2007 (class 1259 OID 35577)
-- Dependencies: 1599
-- Name: acls_k_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX acls_k_idx ON acls USING btree (k);


--
-- TOC entry 2020 (class 1259 OID 35712)
-- Dependencies: 1604
-- Name: changeset_tags_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changeset_tags_id_idx ON changeset_tags USING btree (id);


--
-- TOC entry 2014 (class 1259 OID 40492)
-- Dependencies: 1603 1603 1603 1603
-- Name: changesets_bbox_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_bbox_idx ON changesets USING btree (min_lat, max_lat, min_lon, max_lon);


--
-- TOC entry 2015 (class 1259 OID 40491)
-- Dependencies: 1603
-- Name: changesets_closed_at_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_closed_at_idx ON changesets USING btree (closed_at);


--
-- TOC entry 2016 (class 1259 OID 40490)
-- Dependencies: 1603
-- Name: changesets_created_at_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_created_at_idx ON changesets USING btree (created_at);


--
-- TOC entry 2019 (class 1259 OID 40486)
-- Dependencies: 1603
-- Name: changesets_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_user_id_idx ON changesets USING btree (user_id);


--
-- TOC entry 2021 (class 1259 OID 40571)
-- Dependencies: 1606
-- Name: countries_code_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX countries_code_idx ON countries USING btree (code);


--
-- TOC entry 1974 (class 1259 OID 35426)
-- Dependencies: 1585
-- Name: current_nodes_tile_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_nodes_tile_idx ON current_nodes USING btree (tile);


--
-- TOC entry 1975 (class 1259 OID 35424)
-- Dependencies: 1585
-- Name: current_nodes_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_nodes_timestamp_idx ON current_nodes USING btree ("timestamp");


--
-- TOC entry 1981 (class 1259 OID 35469)
-- Dependencies: 1587 1587
-- Name: current_relation_members_member_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_relation_members_member_idx ON current_relation_members USING btree (member_type, member_id);


--
-- TOC entry 1988 (class 1259 OID 35579)
-- Dependencies: 1590
-- Name: current_relations_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_relations_timestamp_idx ON current_relations USING btree ("timestamp");


--
-- TOC entry 2001 (class 1259 OID 35533)
-- Dependencies: 1595
-- Name: current_way_nodes_node_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_way_nodes_node_idx ON current_way_nodes USING btree (node_id);


--
-- TOC entry 1932 (class 1259 OID 35578)
-- Dependencies: 1563
-- Name: current_ways_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_ways_timestamp_idx ON current_ways USING btree ("timestamp");


--
-- TOC entry 2004 (class 1259 OID 35560)
-- Dependencies: 1597 1597
-- Name: diary_comments_entry_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX diary_comments_entry_id_idx ON diary_comments USING btree (diary_entry_id, id);


--
-- TOC entry 1937 (class 1259 OID 34945)
-- Dependencies: 1567
-- Name: friends_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX friends_user_id_idx ON friends USING btree (user_id);


--
-- TOC entry 1941 (class 1259 OID 34633)
-- Dependencies: 1570
-- Name: gpx_file_tags_gpxid_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_file_tags_gpxid_idx ON gpx_file_tags USING btree (gpx_id);


--
-- TOC entry 1944 (class 1259 OID 35565)
-- Dependencies: 1570
-- Name: gpx_file_tags_tag_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_file_tags_tag_idx ON gpx_file_tags USING btree (tag);


--
-- TOC entry 1947 (class 1259 OID 34991)
-- Dependencies: 1572
-- Name: gpx_files_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_files_timestamp_idx ON gpx_files USING btree ("timestamp");


--
-- TOC entry 1948 (class 1259 OID 35564)
-- Dependencies: 1572
-- Name: gpx_files_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_files_user_id_idx ON gpx_files USING btree (user_id);


--
-- TOC entry 1949 (class 1259 OID 34650)
-- Dependencies: 1572 1572
-- Name: gpx_files_visible_public_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_files_visible_public_idx ON gpx_files USING btree (visible, public);


--
-- TOC entry 1952 (class 1259 OID 35074)
-- Dependencies: 1574
-- Name: messages_to_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX messages_to_user_id_idx ON messages USING btree (to_user_id);


--
-- TOC entry 1976 (class 1259 OID 40487)
-- Dependencies: 1586
-- Name: nodes_changeset_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX nodes_changeset_id_idx ON nodes USING btree (changeset_id);


--
-- TOC entry 1979 (class 1259 OID 35448)
-- Dependencies: 1586
-- Name: nodes_tile_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX nodes_tile_idx ON nodes USING btree (tile);


--
-- TOC entry 1980 (class 1259 OID 35446)
-- Dependencies: 1586
-- Name: nodes_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX nodes_timestamp_idx ON nodes USING btree ("timestamp");


--
-- TOC entry 1939 (class 1259 OID 34964)
-- Dependencies: 1568
-- Name: points_gpxid_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX points_gpxid_idx ON gps_points USING btree (gpx_id);


--
-- TOC entry 1940 (class 1259 OID 35411)
-- Dependencies: 1568
-- Name: points_tile_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX points_tile_idx ON gps_points USING btree (tile);


--
-- TOC entry 1989 (class 1259 OID 35495)
-- Dependencies: 1591 1591
-- Name: relation_members_member_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX relation_members_member_idx ON relation_members USING btree (member_type, member_id);


--
-- TOC entry 1994 (class 1259 OID 40489)
-- Dependencies: 1593
-- Name: relations_changeset_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX relations_changeset_id_idx ON relations USING btree (changeset_id);


--
-- TOC entry 1997 (class 1259 OID 35513)
-- Dependencies: 1593
-- Name: relations_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX relations_timestamp_idx ON relations USING btree ("timestamp");


--
-- TOC entry 1965 (class 1259 OID 35391)
-- Dependencies: 1580
-- Name: sessions_session_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX sessions_session_id_idx ON sessions USING btree (session_id);


--
-- TOC entry 1927 (class 1259 OID 34532)
-- Dependencies: 1560
-- Name: unique_schema_migrations; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX unique_schema_migrations ON schema_migrations USING btree (version);


--
-- TOC entry 1938 (class 1259 OID 34617)
-- Dependencies: 1567
-- Name: user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_id_idx ON friends USING btree (friend_user_id);


--
-- TOC entry 1970 (class 1259 OID 35408)
-- Dependencies: 1583
-- Name: user_tokens_token_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX user_tokens_token_idx ON user_tokens USING btree (token);


--
-- TOC entry 1971 (class 1259 OID 35409)
-- Dependencies: 1583
-- Name: user_tokens_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_tokens_user_id_idx ON user_tokens USING btree (user_id);


--
-- TOC entry 1953 (class 1259 OID 35324)
-- Dependencies: 1576
-- Name: users_display_name_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX users_display_name_idx ON users USING btree (display_name);


--
-- TOC entry 1954 (class 1259 OID 35323)
-- Dependencies: 1576
-- Name: users_email_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX users_email_idx ON users USING btree (email);


--
-- TOC entry 1998 (class 1259 OID 35548)
-- Dependencies: 1594
-- Name: way_nodes_node_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX way_nodes_node_idx ON way_nodes USING btree (node_id);


--
-- TOC entry 1959 (class 1259 OID 40488)
-- Dependencies: 1578
-- Name: ways_changeset_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ways_changeset_id_idx ON ways USING btree (changeset_id);


--
-- TOC entry 1962 (class 1259 OID 35379)
-- Dependencies: 1578
-- Name: ways_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ways_timestamp_idx ON ways USING btree ("timestamp");


--
-- TOC entry 2057 (class 2606 OID 40543)
-- Dependencies: 2017 1604 1603
-- Name: changeset_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY changeset_tags
    ADD CONSTRAINT changeset_tags_id_fkey FOREIGN KEY (id) REFERENCES changesets(id);


--
-- TOC entry 2056 (class 2606 OID 40493)
-- Dependencies: 1603 1955 1576
-- Name: changesets_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY changesets
    ADD CONSTRAINT changesets_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2054 (class 2606 OID 35640)
-- Dependencies: 1585 1600 1972
-- Name: current_node_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_node_tags
    ADD CONSTRAINT current_node_tags_id_fkey FOREIGN KEY (id) REFERENCES current_nodes(id);


--
-- TOC entry 2041 (class 2606 OID 35713)
-- Dependencies: 1603 1585 2017
-- Name: current_nodes_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_nodes
    ADD CONSTRAINT current_nodes_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2043 (class 2606 OID 35675)
-- Dependencies: 1986 1590 1587
-- Name: current_relation_members_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_relation_members
    ADD CONSTRAINT current_relation_members_id_fkey FOREIGN KEY (id) REFERENCES current_relations(id);


--
-- TOC entry 2044 (class 2606 OID 35670)
-- Dependencies: 1986 1590 1588
-- Name: current_relation_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_relation_tags
    ADD CONSTRAINT current_relation_tags_id_fkey FOREIGN KEY (id) REFERENCES current_relations(id);


--
-- TOC entry 2045 (class 2606 OID 35718)
-- Dependencies: 1590 1603 2017
-- Name: current_relations_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_relations
    ADD CONSTRAINT current_relations_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2050 (class 2606 OID 35655)
-- Dependencies: 1563 1930 1595
-- Name: current_way_nodes_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_way_nodes
    ADD CONSTRAINT current_way_nodes_id_fkey FOREIGN KEY (id) REFERENCES current_ways(id);


--
-- TOC entry 2051 (class 2606 OID 35690)
-- Dependencies: 1972 1595 1585
-- Name: current_way_nodes_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_way_nodes
    ADD CONSTRAINT current_way_nodes_node_id_fkey FOREIGN KEY (node_id) REFERENCES current_nodes(id);


--
-- TOC entry 2026 (class 2606 OID 35650)
-- Dependencies: 1930 1563 1561
-- Name: current_way_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_way_tags
    ADD CONSTRAINT current_way_tags_id_fkey FOREIGN KEY (id) REFERENCES current_ways(id);


--
-- TOC entry 2027 (class 2606 OID 35723)
-- Dependencies: 1563 1603 2017
-- Name: current_ways_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_ways
    ADD CONSTRAINT current_ways_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2053 (class 2606 OID 40548)
-- Dependencies: 1933 1597 1565
-- Name: diary_comments_diary_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_comments
    ADD CONSTRAINT diary_comments_diary_entry_id_fkey FOREIGN KEY (diary_entry_id) REFERENCES diary_entries(id);


--
-- TOC entry 2052 (class 2606 OID 40498)
-- Dependencies: 1597 1955 1576
-- Name: diary_comments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_comments
    ADD CONSTRAINT diary_comments_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2029 (class 2606 OID 40585)
-- Dependencies: 2024 1565 1607
-- Name: diary_entries_language_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_entries
    ADD CONSTRAINT diary_entries_language_code_fkey FOREIGN KEY (language_code) REFERENCES languages(code);


--
-- TOC entry 2028 (class 2606 OID 40503)
-- Dependencies: 1565 1955 1576
-- Name: diary_entries_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_entries
    ADD CONSTRAINT diary_entries_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2031 (class 2606 OID 40513)
-- Dependencies: 1955 1576 1567
-- Name: friends_friend_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY friends
    ADD CONSTRAINT friends_friend_user_id_fkey FOREIGN KEY (friend_user_id) REFERENCES users(id);


--
-- TOC entry 2030 (class 2606 OID 40508)
-- Dependencies: 1567 1955 1576
-- Name: friends_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY friends
    ADD CONSTRAINT friends_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2032 (class 2606 OID 40553)
-- Dependencies: 1568 1572 1945
-- Name: gps_points_gpx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gps_points
    ADD CONSTRAINT gps_points_gpx_id_fkey FOREIGN KEY (gpx_id) REFERENCES gpx_files(id);


--
-- TOC entry 2033 (class 2606 OID 40558)
-- Dependencies: 1570 1945 1572
-- Name: gpx_file_tags_gpx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gpx_file_tags
    ADD CONSTRAINT gpx_file_tags_gpx_id_fkey FOREIGN KEY (gpx_id) REFERENCES gpx_files(id);


--
-- TOC entry 2034 (class 2606 OID 40518)
-- Dependencies: 1572 1955 1576
-- Name: gpx_files_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gpx_files
    ADD CONSTRAINT gpx_files_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2035 (class 2606 OID 40523)
-- Dependencies: 1576 1955 1574
-- Name: messages_from_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT messages_from_user_id_fkey FOREIGN KEY (from_user_id) REFERENCES users(id);


--
-- TOC entry 2036 (class 2606 OID 40528)
-- Dependencies: 1576 1955 1574
-- Name: messages_to_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT messages_to_user_id_fkey FOREIGN KEY (to_user_id) REFERENCES users(id);


--
-- TOC entry 2055 (class 2606 OID 35645)
-- Dependencies: 1977 1601 1601 1586 1586
-- Name: node_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY node_tags
    ADD CONSTRAINT node_tags_id_fkey FOREIGN KEY (id, version) REFERENCES nodes(id, version);


--
-- TOC entry 2042 (class 2606 OID 35728)
-- Dependencies: 1603 1586 2017
-- Name: nodes_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT nodes_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2046 (class 2606 OID 35685)
-- Dependencies: 1591 1995 1593 1591 1593
-- Name: relation_members_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY relation_members
    ADD CONSTRAINT relation_members_id_fkey FOREIGN KEY (id, version) REFERENCES relations(id, version);


--
-- TOC entry 2047 (class 2606 OID 35680)
-- Dependencies: 1593 1592 1592 1593 1995
-- Name: relation_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY relation_tags
    ADD CONSTRAINT relation_tags_id_fkey FOREIGN KEY (id, version) REFERENCES relations(id, version);


--
-- TOC entry 2048 (class 2606 OID 35733)
-- Dependencies: 1593 1603 2017
-- Name: relations_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2039 (class 2606 OID 40533)
-- Dependencies: 1576 1955 1581
-- Name: user_preferences_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_preferences
    ADD CONSTRAINT user_preferences_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2040 (class 2606 OID 40538)
-- Dependencies: 1576 1955 1583
-- Name: user_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_tokens
    ADD CONSTRAINT user_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2049 (class 2606 OID 35665)
-- Dependencies: 1578 1960 1578 1594 1594
-- Name: way_nodes_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT way_nodes_id_fkey FOREIGN KEY (id, version) REFERENCES ways(id, version);


--
-- TOC entry 2037 (class 2606 OID 35660)
-- Dependencies: 1578 1960 1577 1577 1578
-- Name: way_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY way_tags
    ADD CONSTRAINT way_tags_id_fkey FOREIGN KEY (id, version) REFERENCES ways(id, version);


--
-- TOC entry 2038 (class 2606 OID 35738)
-- Dependencies: 2017 1603 1578
-- Name: ways_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT ways_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2095 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2009-06-08 17:57:35 EST

--
-- PostgreSQL database dump complete
--


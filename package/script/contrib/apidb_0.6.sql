--
-- PostgreSQL database dump
--

-- Started on 2010-06-24 22:44:23 BST

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

--
-- TOC entry 379 (class 0 OID 0)
-- Name: gbtreekey16; Type: SHELL TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey16;


--
-- TOC entry 20 (class 1255 OID 16396)
-- Dependencies: 6 379
-- Name: gbtreekey16_in(cstring); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey16_in(cstring) RETURNS gbtreekey16
    AS '$libdir/btree_gist', 'gbtreekey_in'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 21 (class 1255 OID 16397)
-- Dependencies: 6 379
-- Name: gbtreekey16_out(gbtreekey16); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey16_out(gbtreekey16) RETURNS cstring
    AS '$libdir/btree_gist', 'gbtreekey_out'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 378 (class 1247 OID 16395)
-- Dependencies: 20 6 21
-- Name: gbtreekey16; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey16 (
    INTERNALLENGTH = 16,
    INPUT = gbtreekey16_in,
    OUTPUT = gbtreekey16_out,
    ALIGNMENT = int4,
    STORAGE = plain
);


--
-- TOC entry 377 (class 0 OID 0)
-- Name: gbtreekey32; Type: SHELL TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey32;


--
-- TOC entry 22 (class 1255 OID 16400)
-- Dependencies: 6 377
-- Name: gbtreekey32_in(cstring); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey32_in(cstring) RETURNS gbtreekey32
    AS '$libdir/btree_gist', 'gbtreekey_in'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 23 (class 1255 OID 16401)
-- Dependencies: 6 377
-- Name: gbtreekey32_out(gbtreekey32); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey32_out(gbtreekey32) RETURNS cstring
    AS '$libdir/btree_gist', 'gbtreekey_out'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 376 (class 1247 OID 16399)
-- Dependencies: 6 23 22
-- Name: gbtreekey32; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey32 (
    INTERNALLENGTH = 32,
    INPUT = gbtreekey32_in,
    OUTPUT = gbtreekey32_out,
    ALIGNMENT = int4,
    STORAGE = plain
);


--
-- TOC entry 419 (class 0 OID 0)
-- Name: gbtreekey4; Type: SHELL TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey4;


--
-- TOC entry 24 (class 1255 OID 16404)
-- Dependencies: 6 419
-- Name: gbtreekey4_in(cstring); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey4_in(cstring) RETURNS gbtreekey4
    AS '$libdir/btree_gist', 'gbtreekey_in'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 25 (class 1255 OID 16405)
-- Dependencies: 6 419
-- Name: gbtreekey4_out(gbtreekey4); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey4_out(gbtreekey4) RETURNS cstring
    AS '$libdir/btree_gist', 'gbtreekey_out'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 418 (class 1247 OID 16403)
-- Dependencies: 6 25 24
-- Name: gbtreekey4; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey4 (
    INTERNALLENGTH = 4,
    INPUT = gbtreekey4_in,
    OUTPUT = gbtreekey4_out,
    ALIGNMENT = int4,
    STORAGE = plain
);


--
-- TOC entry 422 (class 0 OID 0)
-- Name: gbtreekey8; Type: SHELL TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey8;


--
-- TOC entry 26 (class 1255 OID 16408)
-- Dependencies: 6 422
-- Name: gbtreekey8_in(cstring); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey8_in(cstring) RETURNS gbtreekey8
    AS '$libdir/btree_gist', 'gbtreekey_in'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 27 (class 1255 OID 16409)
-- Dependencies: 6 422
-- Name: gbtreekey8_out(gbtreekey8); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey8_out(gbtreekey8) RETURNS cstring
    AS '$libdir/btree_gist', 'gbtreekey_out'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 421 (class 1247 OID 16407)
-- Dependencies: 26 6 27
-- Name: gbtreekey8; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey8 (
    INTERNALLENGTH = 8,
    INPUT = gbtreekey8_in,
    OUTPUT = gbtreekey8_out,
    ALIGNMENT = int4,
    STORAGE = plain
);


--
-- TOC entry 425 (class 0 OID 0)
-- Name: gbtreekey_var; Type: SHELL TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey_var;


--
-- TOC entry 28 (class 1255 OID 16412)
-- Dependencies: 6 425
-- Name: gbtreekey_var_in(cstring); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey_var_in(cstring) RETURNS gbtreekey_var
    AS '$libdir/btree_gist', 'gbtreekey_in'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 29 (class 1255 OID 16413)
-- Dependencies: 6 425
-- Name: gbtreekey_var_out(gbtreekey_var); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbtreekey_var_out(gbtreekey_var) RETURNS cstring
    AS '$libdir/btree_gist', 'gbtreekey_out'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 424 (class 1247 OID 16411)
-- Dependencies: 28 6 29
-- Name: gbtreekey_var; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE gbtreekey_var (
    INTERNALLENGTH = variable,
    INPUT = gbtreekey_var_in,
    OUTPUT = gbtreekey_var_out,
    ALIGNMENT = int4,
    STORAGE = extended
);


--
-- TOC entry 427 (class 1247 OID 16416)
-- Dependencies: 6
-- Name: gpx_visibility_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE gpx_visibility_enum AS ENUM (
    'private',
    'public',
    'trackable',
    'identifiable'
);


--
-- TOC entry 429 (class 1247 OID 16422)
-- Dependencies: 6
-- Name: nwr_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE nwr_enum AS ENUM (
    'Node',
    'Way',
    'Relation'
);


--
-- TOC entry 431 (class 1247 OID 16427)
-- Dependencies: 6
-- Name: user_role_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE user_role_enum AS ENUM (
    'administrator',
    'moderator'
);


--
-- TOC entry 548 (class 1247 OID 685979)
-- Dependencies: 6
-- Name: user_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE user_status_enum AS ENUM (
    'pending',
    'active',
    'confirmed',
    'suspended',
    'deleted'
);


--
-- TOC entry 30 (class 1255 OID 16430)
-- Dependencies: 6
-- Name: gbt_bit_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bit_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bit_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 31 (class 1255 OID 16431)
-- Dependencies: 6
-- Name: gbt_bit_consistent(internal, bit, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bit_consistent(internal, bit, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_bit_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 32 (class 1255 OID 16432)
-- Dependencies: 6
-- Name: gbt_bit_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bit_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bit_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 33 (class 1255 OID 16433)
-- Dependencies: 6
-- Name: gbt_bit_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bit_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bit_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 34 (class 1255 OID 16434)
-- Dependencies: 6
-- Name: gbt_bit_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bit_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bit_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 35 (class 1255 OID 16435)
-- Dependencies: 424 6
-- Name: gbt_bit_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bit_union(bytea, internal) RETURNS gbtreekey_var
    AS '$libdir/btree_gist', 'gbt_bit_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 36 (class 1255 OID 16436)
-- Dependencies: 6
-- Name: gbt_bpchar_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bpchar_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bpchar_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 37 (class 1255 OID 16437)
-- Dependencies: 6
-- Name: gbt_bpchar_consistent(internal, character, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bpchar_consistent(internal, character, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_bpchar_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 38 (class 1255 OID 16438)
-- Dependencies: 6
-- Name: gbt_bytea_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bytea_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bytea_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 39 (class 1255 OID 16439)
-- Dependencies: 6
-- Name: gbt_bytea_consistent(internal, bytea, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bytea_consistent(internal, bytea, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_bytea_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 40 (class 1255 OID 16440)
-- Dependencies: 6
-- Name: gbt_bytea_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bytea_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bytea_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 41 (class 1255 OID 16441)
-- Dependencies: 6
-- Name: gbt_bytea_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bytea_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bytea_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 42 (class 1255 OID 16442)
-- Dependencies: 6
-- Name: gbt_bytea_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bytea_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_bytea_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 43 (class 1255 OID 16443)
-- Dependencies: 6 424
-- Name: gbt_bytea_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_bytea_union(bytea, internal) RETURNS gbtreekey_var
    AS '$libdir/btree_gist', 'gbt_bytea_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 44 (class 1255 OID 16444)
-- Dependencies: 6
-- Name: gbt_cash_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_cash_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_cash_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 45 (class 1255 OID 16445)
-- Dependencies: 6
-- Name: gbt_cash_consistent(internal, money, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_cash_consistent(internal, money, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_cash_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 46 (class 1255 OID 16446)
-- Dependencies: 6
-- Name: gbt_cash_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_cash_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_cash_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 47 (class 1255 OID 16447)
-- Dependencies: 6
-- Name: gbt_cash_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_cash_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_cash_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 48 (class 1255 OID 16448)
-- Dependencies: 6
-- Name: gbt_cash_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_cash_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_cash_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 49 (class 1255 OID 16449)
-- Dependencies: 421 6
-- Name: gbt_cash_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_cash_union(bytea, internal) RETURNS gbtreekey8
    AS '$libdir/btree_gist', 'gbt_cash_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 50 (class 1255 OID 16450)
-- Dependencies: 6
-- Name: gbt_date_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_date_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_date_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 51 (class 1255 OID 16451)
-- Dependencies: 6
-- Name: gbt_date_consistent(internal, date, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_date_consistent(internal, date, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_date_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 52 (class 1255 OID 16452)
-- Dependencies: 6
-- Name: gbt_date_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_date_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_date_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 53 (class 1255 OID 16453)
-- Dependencies: 6
-- Name: gbt_date_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_date_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_date_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 54 (class 1255 OID 16454)
-- Dependencies: 6
-- Name: gbt_date_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_date_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_date_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 55 (class 1255 OID 16455)
-- Dependencies: 421 6
-- Name: gbt_date_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_date_union(bytea, internal) RETURNS gbtreekey8
    AS '$libdir/btree_gist', 'gbt_date_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 56 (class 1255 OID 16456)
-- Dependencies: 6
-- Name: gbt_decompress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_decompress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_decompress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 57 (class 1255 OID 16457)
-- Dependencies: 6
-- Name: gbt_float4_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float4_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float4_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 58 (class 1255 OID 16458)
-- Dependencies: 6
-- Name: gbt_float4_consistent(internal, real, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float4_consistent(internal, real, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_float4_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 59 (class 1255 OID 16459)
-- Dependencies: 6
-- Name: gbt_float4_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float4_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float4_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 60 (class 1255 OID 16460)
-- Dependencies: 6
-- Name: gbt_float4_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float4_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float4_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 61 (class 1255 OID 16461)
-- Dependencies: 6
-- Name: gbt_float4_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float4_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float4_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 62 (class 1255 OID 16462)
-- Dependencies: 6 421
-- Name: gbt_float4_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float4_union(bytea, internal) RETURNS gbtreekey8
    AS '$libdir/btree_gist', 'gbt_float4_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 63 (class 1255 OID 16463)
-- Dependencies: 6
-- Name: gbt_float8_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float8_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float8_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 64 (class 1255 OID 16464)
-- Dependencies: 6
-- Name: gbt_float8_consistent(internal, double precision, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float8_consistent(internal, double precision, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_float8_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 65 (class 1255 OID 16465)
-- Dependencies: 6
-- Name: gbt_float8_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float8_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float8_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 66 (class 1255 OID 16466)
-- Dependencies: 6
-- Name: gbt_float8_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float8_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float8_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 67 (class 1255 OID 16467)
-- Dependencies: 6
-- Name: gbt_float8_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float8_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_float8_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 68 (class 1255 OID 16468)
-- Dependencies: 378 6
-- Name: gbt_float8_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_float8_union(bytea, internal) RETURNS gbtreekey16
    AS '$libdir/btree_gist', 'gbt_float8_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 69 (class 1255 OID 16469)
-- Dependencies: 6
-- Name: gbt_inet_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_inet_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_inet_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 70 (class 1255 OID 16470)
-- Dependencies: 6
-- Name: gbt_inet_consistent(internal, inet, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_inet_consistent(internal, inet, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_inet_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 71 (class 1255 OID 16471)
-- Dependencies: 6
-- Name: gbt_inet_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_inet_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_inet_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 72 (class 1255 OID 16472)
-- Dependencies: 6
-- Name: gbt_inet_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_inet_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_inet_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 73 (class 1255 OID 16473)
-- Dependencies: 6
-- Name: gbt_inet_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_inet_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_inet_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 74 (class 1255 OID 16474)
-- Dependencies: 378 6
-- Name: gbt_inet_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_inet_union(bytea, internal) RETURNS gbtreekey16
    AS '$libdir/btree_gist', 'gbt_inet_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 75 (class 1255 OID 16475)
-- Dependencies: 6
-- Name: gbt_int2_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int2_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int2_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 76 (class 1255 OID 16476)
-- Dependencies: 6
-- Name: gbt_int2_consistent(internal, smallint, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int2_consistent(internal, smallint, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_int2_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 77 (class 1255 OID 16477)
-- Dependencies: 6
-- Name: gbt_int2_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int2_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int2_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 78 (class 1255 OID 16478)
-- Dependencies: 6
-- Name: gbt_int2_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int2_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int2_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 79 (class 1255 OID 16479)
-- Dependencies: 6
-- Name: gbt_int2_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int2_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int2_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 80 (class 1255 OID 16480)
-- Dependencies: 418 6
-- Name: gbt_int2_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int2_union(bytea, internal) RETURNS gbtreekey4
    AS '$libdir/btree_gist', 'gbt_int2_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 81 (class 1255 OID 16481)
-- Dependencies: 6
-- Name: gbt_int4_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int4_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int4_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 82 (class 1255 OID 16482)
-- Dependencies: 6
-- Name: gbt_int4_consistent(internal, integer, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int4_consistent(internal, integer, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_int4_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 83 (class 1255 OID 16483)
-- Dependencies: 6
-- Name: gbt_int4_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int4_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int4_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 84 (class 1255 OID 16484)
-- Dependencies: 6
-- Name: gbt_int4_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int4_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int4_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 85 (class 1255 OID 16485)
-- Dependencies: 6
-- Name: gbt_int4_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int4_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int4_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 86 (class 1255 OID 16486)
-- Dependencies: 421 6
-- Name: gbt_int4_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int4_union(bytea, internal) RETURNS gbtreekey8
    AS '$libdir/btree_gist', 'gbt_int4_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 87 (class 1255 OID 16487)
-- Dependencies: 6
-- Name: gbt_int8_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int8_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int8_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 88 (class 1255 OID 16488)
-- Dependencies: 6
-- Name: gbt_int8_consistent(internal, bigint, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int8_consistent(internal, bigint, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_int8_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 89 (class 1255 OID 16489)
-- Dependencies: 6
-- Name: gbt_int8_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int8_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int8_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 90 (class 1255 OID 16490)
-- Dependencies: 6
-- Name: gbt_int8_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int8_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int8_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 91 (class 1255 OID 16491)
-- Dependencies: 6
-- Name: gbt_int8_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int8_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_int8_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 92 (class 1255 OID 16492)
-- Dependencies: 6 378
-- Name: gbt_int8_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_int8_union(bytea, internal) RETURNS gbtreekey16
    AS '$libdir/btree_gist', 'gbt_int8_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 93 (class 1255 OID 16493)
-- Dependencies: 6
-- Name: gbt_intv_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_intv_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_intv_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 94 (class 1255 OID 16494)
-- Dependencies: 6
-- Name: gbt_intv_consistent(internal, interval, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_intv_consistent(internal, interval, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_intv_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 95 (class 1255 OID 16495)
-- Dependencies: 6
-- Name: gbt_intv_decompress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_intv_decompress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_intv_decompress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 96 (class 1255 OID 16496)
-- Dependencies: 6
-- Name: gbt_intv_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_intv_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_intv_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 97 (class 1255 OID 16497)
-- Dependencies: 6
-- Name: gbt_intv_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_intv_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_intv_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 98 (class 1255 OID 16498)
-- Dependencies: 6
-- Name: gbt_intv_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_intv_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_intv_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 99 (class 1255 OID 16499)
-- Dependencies: 6 376
-- Name: gbt_intv_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_intv_union(bytea, internal) RETURNS gbtreekey32
    AS '$libdir/btree_gist', 'gbt_intv_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 100 (class 1255 OID 16500)
-- Dependencies: 6
-- Name: gbt_macad_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_macad_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_macad_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 101 (class 1255 OID 16501)
-- Dependencies: 6
-- Name: gbt_macad_consistent(internal, macaddr, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_macad_consistent(internal, macaddr, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_macad_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 102 (class 1255 OID 16502)
-- Dependencies: 6
-- Name: gbt_macad_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_macad_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_macad_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 103 (class 1255 OID 16503)
-- Dependencies: 6
-- Name: gbt_macad_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_macad_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_macad_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 104 (class 1255 OID 16504)
-- Dependencies: 6
-- Name: gbt_macad_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_macad_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_macad_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 105 (class 1255 OID 16505)
-- Dependencies: 6 378
-- Name: gbt_macad_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_macad_union(bytea, internal) RETURNS gbtreekey16
    AS '$libdir/btree_gist', 'gbt_macad_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 106 (class 1255 OID 16506)
-- Dependencies: 6
-- Name: gbt_numeric_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_numeric_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_numeric_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 107 (class 1255 OID 16507)
-- Dependencies: 6
-- Name: gbt_numeric_consistent(internal, numeric, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_numeric_consistent(internal, numeric, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_numeric_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 108 (class 1255 OID 16508)
-- Dependencies: 6
-- Name: gbt_numeric_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_numeric_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_numeric_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 109 (class 1255 OID 16509)
-- Dependencies: 6
-- Name: gbt_numeric_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_numeric_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_numeric_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 110 (class 1255 OID 16510)
-- Dependencies: 6
-- Name: gbt_numeric_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_numeric_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_numeric_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 111 (class 1255 OID 16511)
-- Dependencies: 424 6
-- Name: gbt_numeric_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_numeric_union(bytea, internal) RETURNS gbtreekey_var
    AS '$libdir/btree_gist', 'gbt_numeric_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 112 (class 1255 OID 16512)
-- Dependencies: 6
-- Name: gbt_oid_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_oid_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_oid_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 113 (class 1255 OID 16513)
-- Dependencies: 6
-- Name: gbt_oid_consistent(internal, oid, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_oid_consistent(internal, oid, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_oid_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 114 (class 1255 OID 16514)
-- Dependencies: 6
-- Name: gbt_oid_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_oid_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_oid_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 115 (class 1255 OID 16515)
-- Dependencies: 6
-- Name: gbt_oid_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_oid_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_oid_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 116 (class 1255 OID 16516)
-- Dependencies: 6
-- Name: gbt_oid_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_oid_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_oid_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 117 (class 1255 OID 16517)
-- Dependencies: 6 421
-- Name: gbt_oid_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_oid_union(bytea, internal) RETURNS gbtreekey8
    AS '$libdir/btree_gist', 'gbt_oid_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 118 (class 1255 OID 16518)
-- Dependencies: 6
-- Name: gbt_text_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_text_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_text_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 119 (class 1255 OID 16519)
-- Dependencies: 6
-- Name: gbt_text_consistent(internal, text, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_text_consistent(internal, text, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_text_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 120 (class 1255 OID 16520)
-- Dependencies: 6
-- Name: gbt_text_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_text_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_text_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 121 (class 1255 OID 16521)
-- Dependencies: 6
-- Name: gbt_text_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_text_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_text_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 122 (class 1255 OID 16522)
-- Dependencies: 6
-- Name: gbt_text_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_text_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_text_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 123 (class 1255 OID 16523)
-- Dependencies: 6 424
-- Name: gbt_text_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_text_union(bytea, internal) RETURNS gbtreekey_var
    AS '$libdir/btree_gist', 'gbt_text_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 124 (class 1255 OID 16524)
-- Dependencies: 6
-- Name: gbt_time_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_time_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_time_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 125 (class 1255 OID 16525)
-- Dependencies: 6
-- Name: gbt_time_consistent(internal, time without time zone, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_time_consistent(internal, time without time zone, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_time_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 126 (class 1255 OID 16526)
-- Dependencies: 6
-- Name: gbt_time_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_time_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_time_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 127 (class 1255 OID 16527)
-- Dependencies: 6
-- Name: gbt_time_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_time_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_time_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 128 (class 1255 OID 16528)
-- Dependencies: 6
-- Name: gbt_time_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_time_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_time_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 129 (class 1255 OID 16529)
-- Dependencies: 378 6
-- Name: gbt_time_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_time_union(bytea, internal) RETURNS gbtreekey16
    AS '$libdir/btree_gist', 'gbt_time_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 130 (class 1255 OID 16530)
-- Dependencies: 6
-- Name: gbt_timetz_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_timetz_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_timetz_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 131 (class 1255 OID 16531)
-- Dependencies: 6
-- Name: gbt_timetz_consistent(internal, time with time zone, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_timetz_consistent(internal, time with time zone, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_timetz_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 132 (class 1255 OID 16532)
-- Dependencies: 6
-- Name: gbt_ts_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_ts_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_ts_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 133 (class 1255 OID 16533)
-- Dependencies: 6
-- Name: gbt_ts_consistent(internal, timestamp without time zone, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_ts_consistent(internal, timestamp without time zone, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_ts_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 134 (class 1255 OID 16534)
-- Dependencies: 6
-- Name: gbt_ts_penalty(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_ts_penalty(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_ts_penalty'
    LANGUAGE c IMMUTABLE STRICT;


--
-- TOC entry 135 (class 1255 OID 16535)
-- Dependencies: 6
-- Name: gbt_ts_picksplit(internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_ts_picksplit(internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_ts_picksplit'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 136 (class 1255 OID 16536)
-- Dependencies: 6
-- Name: gbt_ts_same(internal, internal, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_ts_same(internal, internal, internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_ts_same'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 137 (class 1255 OID 16537)
-- Dependencies: 6 378
-- Name: gbt_ts_union(bytea, internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_ts_union(bytea, internal) RETURNS gbtreekey16
    AS '$libdir/btree_gist', 'gbt_ts_union'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 138 (class 1255 OID 16538)
-- Dependencies: 6
-- Name: gbt_tstz_compress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_tstz_compress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_tstz_compress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 139 (class 1255 OID 16539)
-- Dependencies: 6
-- Name: gbt_tstz_consistent(internal, timestamp with time zone, smallint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_tstz_consistent(internal, timestamp with time zone, smallint) RETURNS boolean
    AS '$libdir/btree_gist', 'gbt_tstz_consistent'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 140 (class 1255 OID 16540)
-- Dependencies: 6
-- Name: gbt_var_decompress(internal); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION gbt_var_decompress(internal) RETURNS internal
    AS '$libdir/btree_gist', 'gbt_var_decompress'
    LANGUAGE c IMMUTABLE;


--
-- TOC entry 141 (class 1255 OID 620392)
-- Dependencies: 6
-- Name: maptile_for_point(bigint, bigint, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION maptile_for_point(bigint, bigint, integer) RETURNS integer
    AS '/srv/.cruise/osm_db_functions/libpgosm.so', 'maptile_for_point'
    LANGUAGE c STRICT;


--
-- TOC entry 142 (class 1255 OID 620393)
-- Dependencies: 6
-- Name: tile_for_point(integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION tile_for_point(integer, integer) RETURNS bigint
    AS '/srv/.cruise/osm_db_functions/libpgosm.so', 'tile_for_point'
    LANGUAGE c STRICT;


--
-- TOC entry 1493 (class 2753 OID 16542)
-- Dependencies: 6
-- Name: gist_bit_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_bit_ops USING gist;


--
-- TOC entry 1364 (class 2616 OID 16543)
-- Dependencies: 6 424 1493
-- Name: gist_bit_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_bit_ops
    DEFAULT FOR TYPE bit USING gist AS
    STORAGE gbtreekey_var ,
    OPERATOR 1 <(bit,bit) ,
    OPERATOR 2 <=(bit,bit) ,
    OPERATOR 3 =(bit,bit) ,
    OPERATOR 4 >=(bit,bit) ,
    OPERATOR 5 >(bit,bit) ,
    FUNCTION 1 gbt_bit_consistent(internal,bit,smallint) ,
    FUNCTION 2 gbt_bit_union(bytea,internal) ,
    FUNCTION 3 gbt_bit_compress(internal) ,
    FUNCTION 4 gbt_var_decompress(internal) ,
    FUNCTION 5 gbt_bit_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_bit_picksplit(internal,internal) ,
    FUNCTION 7 gbt_bit_same(internal,internal,internal);


--
-- TOC entry 1494 (class 2753 OID 16556)
-- Dependencies: 6
-- Name: gist_bpchar_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_bpchar_ops USING gist;


--
-- TOC entry 1365 (class 2616 OID 16557)
-- Dependencies: 6 1494 424
-- Name: gist_bpchar_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_bpchar_ops
    DEFAULT FOR TYPE character USING gist AS
    STORAGE gbtreekey_var ,
    OPERATOR 1 <(character,character) ,
    OPERATOR 2 <=(character,character) ,
    OPERATOR 3 =(character,character) ,
    OPERATOR 4 >=(character,character) ,
    OPERATOR 5 >(character,character) ,
    FUNCTION 1 gbt_bpchar_consistent(internal,character,smallint) ,
    FUNCTION 2 gbt_text_union(bytea,internal) ,
    FUNCTION 3 gbt_bpchar_compress(internal) ,
    FUNCTION 4 gbt_var_decompress(internal) ,
    FUNCTION 5 gbt_text_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_text_picksplit(internal,internal) ,
    FUNCTION 7 gbt_text_same(internal,internal,internal);


--
-- TOC entry 1495 (class 2753 OID 16570)
-- Dependencies: 6
-- Name: gist_bytea_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_bytea_ops USING gist;


--
-- TOC entry 1366 (class 2616 OID 16571)
-- Dependencies: 424 6 1495
-- Name: gist_bytea_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_bytea_ops
    DEFAULT FOR TYPE bytea USING gist AS
    STORAGE gbtreekey_var ,
    OPERATOR 1 <(bytea,bytea) ,
    OPERATOR 2 <=(bytea,bytea) ,
    OPERATOR 3 =(bytea,bytea) ,
    OPERATOR 4 >=(bytea,bytea) ,
    OPERATOR 5 >(bytea,bytea) ,
    FUNCTION 1 gbt_bytea_consistent(internal,bytea,smallint) ,
    FUNCTION 2 gbt_bytea_union(bytea,internal) ,
    FUNCTION 3 gbt_bytea_compress(internal) ,
    FUNCTION 4 gbt_var_decompress(internal) ,
    FUNCTION 5 gbt_bytea_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_bytea_picksplit(internal,internal) ,
    FUNCTION 7 gbt_bytea_same(internal,internal,internal);


--
-- TOC entry 1496 (class 2753 OID 16584)
-- Dependencies: 6
-- Name: gist_cash_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_cash_ops USING gist;


--
-- TOC entry 1367 (class 2616 OID 16585)
-- Dependencies: 6 378 1496
-- Name: gist_cash_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_cash_ops
    DEFAULT FOR TYPE money USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(money,money) ,
    OPERATOR 2 <=(money,money) ,
    OPERATOR 3 =(money,money) ,
    OPERATOR 4 >=(money,money) ,
    OPERATOR 5 >(money,money) ,
    FUNCTION 1 gbt_cash_consistent(internal,money,smallint) ,
    FUNCTION 2 gbt_cash_union(bytea,internal) ,
    FUNCTION 3 gbt_cash_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_cash_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_cash_picksplit(internal,internal) ,
    FUNCTION 7 gbt_cash_same(internal,internal,internal);


--
-- TOC entry 1497 (class 2753 OID 16598)
-- Dependencies: 6
-- Name: gist_cidr_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_cidr_ops USING gist;


--
-- TOC entry 1368 (class 2616 OID 16599)
-- Dependencies: 6 1497 378
-- Name: gist_cidr_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_cidr_ops
    DEFAULT FOR TYPE cidr USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(inet,inet) RECHECK ,
    OPERATOR 2 <=(inet,inet) RECHECK ,
    OPERATOR 3 =(inet,inet) RECHECK ,
    OPERATOR 4 >=(inet,inet) RECHECK ,
    OPERATOR 5 >(inet,inet) RECHECK ,
    FUNCTION 1 gbt_inet_consistent(internal,inet,smallint) ,
    FUNCTION 2 gbt_inet_union(bytea,internal) ,
    FUNCTION 3 gbt_inet_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_inet_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_inet_picksplit(internal,internal) ,
    FUNCTION 7 gbt_inet_same(internal,internal,internal);


--
-- TOC entry 1498 (class 2753 OID 16612)
-- Dependencies: 6
-- Name: gist_date_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_date_ops USING gist;


--
-- TOC entry 1369 (class 2616 OID 16613)
-- Dependencies: 1498 6 421
-- Name: gist_date_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_date_ops
    DEFAULT FOR TYPE date USING gist AS
    STORAGE gbtreekey8 ,
    OPERATOR 1 <(date,date) ,
    OPERATOR 2 <=(date,date) ,
    OPERATOR 3 =(date,date) ,
    OPERATOR 4 >=(date,date) ,
    OPERATOR 5 >(date,date) ,
    FUNCTION 1 gbt_date_consistent(internal,date,smallint) ,
    FUNCTION 2 gbt_date_union(bytea,internal) ,
    FUNCTION 3 gbt_date_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_date_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_date_picksplit(internal,internal) ,
    FUNCTION 7 gbt_date_same(internal,internal,internal);


--
-- TOC entry 1499 (class 2753 OID 16626)
-- Dependencies: 6
-- Name: gist_float4_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_float4_ops USING gist;


--
-- TOC entry 1370 (class 2616 OID 16627)
-- Dependencies: 1499 6 421
-- Name: gist_float4_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_float4_ops
    DEFAULT FOR TYPE real USING gist AS
    STORAGE gbtreekey8 ,
    OPERATOR 1 <(real,real) ,
    OPERATOR 2 <=(real,real) ,
    OPERATOR 3 =(real,real) ,
    OPERATOR 4 >=(real,real) ,
    OPERATOR 5 >(real,real) ,
    FUNCTION 1 gbt_float4_consistent(internal,real,smallint) ,
    FUNCTION 2 gbt_float4_union(bytea,internal) ,
    FUNCTION 3 gbt_float4_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_float4_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_float4_picksplit(internal,internal) ,
    FUNCTION 7 gbt_float4_same(internal,internal,internal);


--
-- TOC entry 1500 (class 2753 OID 16640)
-- Dependencies: 6
-- Name: gist_float8_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_float8_ops USING gist;


--
-- TOC entry 1371 (class 2616 OID 16641)
-- Dependencies: 1500 6 378
-- Name: gist_float8_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_float8_ops
    DEFAULT FOR TYPE double precision USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(double precision,double precision) ,
    OPERATOR 2 <=(double precision,double precision) ,
    OPERATOR 3 =(double precision,double precision) ,
    OPERATOR 4 >=(double precision,double precision) ,
    OPERATOR 5 >(double precision,double precision) ,
    FUNCTION 1 gbt_float8_consistent(internal,double precision,smallint) ,
    FUNCTION 2 gbt_float8_union(bytea,internal) ,
    FUNCTION 3 gbt_float8_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_float8_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_float8_picksplit(internal,internal) ,
    FUNCTION 7 gbt_float8_same(internal,internal,internal);


--
-- TOC entry 1501 (class 2753 OID 16654)
-- Dependencies: 6
-- Name: gist_inet_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_inet_ops USING gist;


--
-- TOC entry 1372 (class 2616 OID 16655)
-- Dependencies: 378 1501 6
-- Name: gist_inet_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_inet_ops
    DEFAULT FOR TYPE inet USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(inet,inet) RECHECK ,
    OPERATOR 2 <=(inet,inet) RECHECK ,
    OPERATOR 3 =(inet,inet) RECHECK ,
    OPERATOR 4 >=(inet,inet) RECHECK ,
    OPERATOR 5 >(inet,inet) RECHECK ,
    FUNCTION 1 gbt_inet_consistent(internal,inet,smallint) ,
    FUNCTION 2 gbt_inet_union(bytea,internal) ,
    FUNCTION 3 gbt_inet_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_inet_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_inet_picksplit(internal,internal) ,
    FUNCTION 7 gbt_inet_same(internal,internal,internal);


--
-- TOC entry 1502 (class 2753 OID 16668)
-- Dependencies: 6
-- Name: gist_int2_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_int2_ops USING gist;


--
-- TOC entry 1373 (class 2616 OID 16669)
-- Dependencies: 6 1502 418
-- Name: gist_int2_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_int2_ops
    DEFAULT FOR TYPE smallint USING gist AS
    STORAGE gbtreekey4 ,
    OPERATOR 1 <(smallint,smallint) ,
    OPERATOR 2 <=(smallint,smallint) ,
    OPERATOR 3 =(smallint,smallint) ,
    OPERATOR 4 >=(smallint,smallint) ,
    OPERATOR 5 >(smallint,smallint) ,
    FUNCTION 1 gbt_int2_consistent(internal,smallint,smallint) ,
    FUNCTION 2 gbt_int2_union(bytea,internal) ,
    FUNCTION 3 gbt_int2_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_int2_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_int2_picksplit(internal,internal) ,
    FUNCTION 7 gbt_int2_same(internal,internal,internal);


--
-- TOC entry 1503 (class 2753 OID 16682)
-- Dependencies: 6
-- Name: gist_int4_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_int4_ops USING gist;


--
-- TOC entry 1374 (class 2616 OID 16683)
-- Dependencies: 6 421 1503
-- Name: gist_int4_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_int4_ops
    DEFAULT FOR TYPE integer USING gist AS
    STORAGE gbtreekey8 ,
    OPERATOR 1 <(integer,integer) ,
    OPERATOR 2 <=(integer,integer) ,
    OPERATOR 3 =(integer,integer) ,
    OPERATOR 4 >=(integer,integer) ,
    OPERATOR 5 >(integer,integer) ,
    FUNCTION 1 gbt_int4_consistent(internal,integer,smallint) ,
    FUNCTION 2 gbt_int4_union(bytea,internal) ,
    FUNCTION 3 gbt_int4_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_int4_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_int4_picksplit(internal,internal) ,
    FUNCTION 7 gbt_int4_same(internal,internal,internal);


--
-- TOC entry 1504 (class 2753 OID 16696)
-- Dependencies: 6
-- Name: gist_int8_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_int8_ops USING gist;


--
-- TOC entry 1375 (class 2616 OID 16697)
-- Dependencies: 378 1504 6
-- Name: gist_int8_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_int8_ops
    DEFAULT FOR TYPE bigint USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(bigint,bigint) ,
    OPERATOR 2 <=(bigint,bigint) ,
    OPERATOR 3 =(bigint,bigint) ,
    OPERATOR 4 >=(bigint,bigint) ,
    OPERATOR 5 >(bigint,bigint) ,
    FUNCTION 1 gbt_int8_consistent(internal,bigint,smallint) ,
    FUNCTION 2 gbt_int8_union(bytea,internal) ,
    FUNCTION 3 gbt_int8_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_int8_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_int8_picksplit(internal,internal) ,
    FUNCTION 7 gbt_int8_same(internal,internal,internal);


--
-- TOC entry 1505 (class 2753 OID 16710)
-- Dependencies: 6
-- Name: gist_interval_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_interval_ops USING gist;


--
-- TOC entry 1376 (class 2616 OID 16711)
-- Dependencies: 1505 376 6
-- Name: gist_interval_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_interval_ops
    DEFAULT FOR TYPE interval USING gist AS
    STORAGE gbtreekey32 ,
    OPERATOR 1 <(interval,interval) ,
    OPERATOR 2 <=(interval,interval) ,
    OPERATOR 3 =(interval,interval) ,
    OPERATOR 4 >=(interval,interval) ,
    OPERATOR 5 >(interval,interval) ,
    FUNCTION 1 gbt_intv_consistent(internal,interval,smallint) ,
    FUNCTION 2 gbt_intv_union(bytea,internal) ,
    FUNCTION 3 gbt_intv_compress(internal) ,
    FUNCTION 4 gbt_intv_decompress(internal) ,
    FUNCTION 5 gbt_intv_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_intv_picksplit(internal,internal) ,
    FUNCTION 7 gbt_intv_same(internal,internal,internal);


--
-- TOC entry 1506 (class 2753 OID 16724)
-- Dependencies: 6
-- Name: gist_macaddr_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_macaddr_ops USING gist;


--
-- TOC entry 1377 (class 2616 OID 16725)
-- Dependencies: 6 1506 378
-- Name: gist_macaddr_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_macaddr_ops
    DEFAULT FOR TYPE macaddr USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(macaddr,macaddr) ,
    OPERATOR 2 <=(macaddr,macaddr) ,
    OPERATOR 3 =(macaddr,macaddr) ,
    OPERATOR 4 >=(macaddr,macaddr) ,
    OPERATOR 5 >(macaddr,macaddr) ,
    FUNCTION 1 gbt_macad_consistent(internal,macaddr,smallint) ,
    FUNCTION 2 gbt_macad_union(bytea,internal) ,
    FUNCTION 3 gbt_macad_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_macad_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_macad_picksplit(internal,internal) ,
    FUNCTION 7 gbt_macad_same(internal,internal,internal);


--
-- TOC entry 1507 (class 2753 OID 16738)
-- Dependencies: 6
-- Name: gist_numeric_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_numeric_ops USING gist;


--
-- TOC entry 1378 (class 2616 OID 16739)
-- Dependencies: 6 1507 424
-- Name: gist_numeric_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_numeric_ops
    DEFAULT FOR TYPE numeric USING gist AS
    STORAGE gbtreekey_var ,
    OPERATOR 1 <(numeric,numeric) ,
    OPERATOR 2 <=(numeric,numeric) ,
    OPERATOR 3 =(numeric,numeric) ,
    OPERATOR 4 >=(numeric,numeric) ,
    OPERATOR 5 >(numeric,numeric) ,
    FUNCTION 1 gbt_numeric_consistent(internal,numeric,smallint) ,
    FUNCTION 2 gbt_numeric_union(bytea,internal) ,
    FUNCTION 3 gbt_numeric_compress(internal) ,
    FUNCTION 4 gbt_var_decompress(internal) ,
    FUNCTION 5 gbt_numeric_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_numeric_picksplit(internal,internal) ,
    FUNCTION 7 gbt_numeric_same(internal,internal,internal);


--
-- TOC entry 1508 (class 2753 OID 16752)
-- Dependencies: 6
-- Name: gist_oid_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_oid_ops USING gist;


--
-- TOC entry 1379 (class 2616 OID 16753)
-- Dependencies: 421 6 1508
-- Name: gist_oid_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_oid_ops
    DEFAULT FOR TYPE oid USING gist AS
    STORAGE gbtreekey8 ,
    OPERATOR 1 <(oid,oid) ,
    OPERATOR 2 <=(oid,oid) ,
    OPERATOR 3 =(oid,oid) ,
    OPERATOR 4 >=(oid,oid) ,
    OPERATOR 5 >(oid,oid) ,
    FUNCTION 1 gbt_oid_consistent(internal,oid,smallint) ,
    FUNCTION 2 gbt_oid_union(bytea,internal) ,
    FUNCTION 3 gbt_oid_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_oid_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_oid_picksplit(internal,internal) ,
    FUNCTION 7 gbt_oid_same(internal,internal,internal);


--
-- TOC entry 1509 (class 2753 OID 16766)
-- Dependencies: 6
-- Name: gist_text_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_text_ops USING gist;


--
-- TOC entry 1380 (class 2616 OID 16767)
-- Dependencies: 424 1509 6
-- Name: gist_text_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_text_ops
    DEFAULT FOR TYPE text USING gist AS
    STORAGE gbtreekey_var ,
    OPERATOR 1 <(text,text) ,
    OPERATOR 2 <=(text,text) ,
    OPERATOR 3 =(text,text) ,
    OPERATOR 4 >=(text,text) ,
    OPERATOR 5 >(text,text) ,
    FUNCTION 1 gbt_text_consistent(internal,text,smallint) ,
    FUNCTION 2 gbt_text_union(bytea,internal) ,
    FUNCTION 3 gbt_text_compress(internal) ,
    FUNCTION 4 gbt_var_decompress(internal) ,
    FUNCTION 5 gbt_text_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_text_picksplit(internal,internal) ,
    FUNCTION 7 gbt_text_same(internal,internal,internal);


--
-- TOC entry 1510 (class 2753 OID 16780)
-- Dependencies: 6
-- Name: gist_time_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_time_ops USING gist;


--
-- TOC entry 1381 (class 2616 OID 16781)
-- Dependencies: 378 6 1510
-- Name: gist_time_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_time_ops
    DEFAULT FOR TYPE time without time zone USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(time without time zone,time without time zone) ,
    OPERATOR 2 <=(time without time zone,time without time zone) ,
    OPERATOR 3 =(time without time zone,time without time zone) ,
    OPERATOR 4 >=(time without time zone,time without time zone) ,
    OPERATOR 5 >(time without time zone,time without time zone) ,
    FUNCTION 1 gbt_time_consistent(internal,time without time zone,smallint) ,
    FUNCTION 2 gbt_time_union(bytea,internal) ,
    FUNCTION 3 gbt_time_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_time_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_time_picksplit(internal,internal) ,
    FUNCTION 7 gbt_time_same(internal,internal,internal);


--
-- TOC entry 1511 (class 2753 OID 16794)
-- Dependencies: 6
-- Name: gist_timestamp_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_timestamp_ops USING gist;


--
-- TOC entry 1382 (class 2616 OID 16795)
-- Dependencies: 6 378 1511
-- Name: gist_timestamp_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_timestamp_ops
    DEFAULT FOR TYPE timestamp without time zone USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(timestamp without time zone,timestamp without time zone) ,
    OPERATOR 2 <=(timestamp without time zone,timestamp without time zone) ,
    OPERATOR 3 =(timestamp without time zone,timestamp without time zone) ,
    OPERATOR 4 >=(timestamp without time zone,timestamp without time zone) ,
    OPERATOR 5 >(timestamp without time zone,timestamp without time zone) ,
    FUNCTION 1 gbt_ts_consistent(internal,timestamp without time zone,smallint) ,
    FUNCTION 2 gbt_ts_union(bytea,internal) ,
    FUNCTION 3 gbt_ts_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_ts_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_ts_picksplit(internal,internal) ,
    FUNCTION 7 gbt_ts_same(internal,internal,internal);


--
-- TOC entry 1512 (class 2753 OID 16808)
-- Dependencies: 6
-- Name: gist_timestamptz_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_timestamptz_ops USING gist;


--
-- TOC entry 1383 (class 2616 OID 16809)
-- Dependencies: 378 6 1512
-- Name: gist_timestamptz_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_timestamptz_ops
    DEFAULT FOR TYPE timestamp with time zone USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(timestamp with time zone,timestamp with time zone) ,
    OPERATOR 2 <=(timestamp with time zone,timestamp with time zone) ,
    OPERATOR 3 =(timestamp with time zone,timestamp with time zone) ,
    OPERATOR 4 >=(timestamp with time zone,timestamp with time zone) ,
    OPERATOR 5 >(timestamp with time zone,timestamp with time zone) ,
    FUNCTION 1 gbt_tstz_consistent(internal,timestamp with time zone,smallint) ,
    FUNCTION 2 gbt_ts_union(bytea,internal) ,
    FUNCTION 3 gbt_tstz_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_ts_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_ts_picksplit(internal,internal) ,
    FUNCTION 7 gbt_ts_same(internal,internal,internal);


--
-- TOC entry 1513 (class 2753 OID 16822)
-- Dependencies: 6
-- Name: gist_timetz_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_timetz_ops USING gist;


--
-- TOC entry 1384 (class 2616 OID 16823)
-- Dependencies: 1513 6 378
-- Name: gist_timetz_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_timetz_ops
    DEFAULT FOR TYPE time with time zone USING gist AS
    STORAGE gbtreekey16 ,
    OPERATOR 1 <(time with time zone,time with time zone) RECHECK ,
    OPERATOR 2 <=(time with time zone,time with time zone) RECHECK ,
    OPERATOR 3 =(time with time zone,time with time zone) RECHECK ,
    OPERATOR 4 >=(time with time zone,time with time zone) RECHECK ,
    OPERATOR 5 >(time with time zone,time with time zone) RECHECK ,
    FUNCTION 1 gbt_timetz_consistent(internal,time with time zone,smallint) ,
    FUNCTION 2 gbt_time_union(bytea,internal) ,
    FUNCTION 3 gbt_timetz_compress(internal) ,
    FUNCTION 4 gbt_decompress(internal) ,
    FUNCTION 5 gbt_time_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_time_picksplit(internal,internal) ,
    FUNCTION 7 gbt_time_same(internal,internal,internal);


--
-- TOC entry 1514 (class 2753 OID 16836)
-- Dependencies: 6
-- Name: gist_vbit_ops; Type: OPERATOR FAMILY; Schema: public; Owner: -
--

CREATE OPERATOR FAMILY gist_vbit_ops USING gist;


--
-- TOC entry 1385 (class 2616 OID 16837)
-- Dependencies: 1514 424 6
-- Name: gist_vbit_ops; Type: OPERATOR CLASS; Schema: public; Owner: -
--

CREATE OPERATOR CLASS gist_vbit_ops
    DEFAULT FOR TYPE bit varying USING gist AS
    STORAGE gbtreekey_var ,
    OPERATOR 1 <(bit varying,bit varying) ,
    OPERATOR 2 <=(bit varying,bit varying) ,
    OPERATOR 3 =(bit varying,bit varying) ,
    OPERATOR 4 >=(bit varying,bit varying) ,
    OPERATOR 5 >(bit varying,bit varying) ,
    FUNCTION 1 gbt_bit_consistent(internal,bit,smallint) ,
    FUNCTION 2 gbt_bit_union(bytea,internal) ,
    FUNCTION 3 gbt_bit_compress(internal) ,
    FUNCTION 4 gbt_var_decompress(internal) ,
    FUNCTION 5 gbt_bit_penalty(internal,internal,internal) ,
    FUNCTION 6 gbt_bit_picksplit(internal,internal) ,
    FUNCTION 7 gbt_bit_same(internal,internal,internal);


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1765 (class 1259 OID 16850)
-- Dependencies: 6
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
-- TOC entry 1766 (class 1259 OID 16856)
-- Dependencies: 6 1765
-- Name: acls_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE acls_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2366 (class 0 OID 0)
-- Dependencies: 1766
-- Name: acls_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE acls_id_seq OWNED BY acls.id;


--
-- TOC entry 2367 (class 0 OID 0)
-- Dependencies: 1766
-- Name: acls_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('acls_id_seq', 1, false);


--
-- TOC entry 1767 (class 1259 OID 16858)
-- Dependencies: 2090 2091 6
-- Name: changeset_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE changeset_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1768 (class 1259 OID 16866)
-- Dependencies: 2092 6
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
-- TOC entry 1769 (class 1259 OID 16870)
-- Dependencies: 6 1768
-- Name: changesets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE changesets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2368 (class 0 OID 0)
-- Dependencies: 1769
-- Name: changesets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE changesets_id_seq OWNED BY changesets.id;


--
-- TOC entry 2369 (class 0 OID 0)
-- Dependencies: 1769
-- Name: changesets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('changesets_id_seq', 1, false);


--
-- TOC entry 1770 (class 1259 OID 16872)
-- Dependencies: 2094 2095 2096 2097 2098 2099 6
-- Name: client_applications; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE client_applications (
    id integer NOT NULL,
    name character varying(255),
    url character varying(255),
    support_url character varying(255),
    callback_url character varying(255),
    key character varying(50),
    secret character varying(50),
    user_id integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    allow_read_prefs boolean DEFAULT false NOT NULL,
    allow_write_prefs boolean DEFAULT false NOT NULL,
    allow_write_diary boolean DEFAULT false NOT NULL,
    allow_write_api boolean DEFAULT false NOT NULL,
    allow_read_gpx boolean DEFAULT false NOT NULL,
    allow_write_gpx boolean DEFAULT false NOT NULL
);


--
-- TOC entry 1771 (class 1259 OID 16884)
-- Dependencies: 1770 6
-- Name: client_applications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE client_applications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2370 (class 0 OID 0)
-- Dependencies: 1771
-- Name: client_applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE client_applications_id_seq OWNED BY client_applications.id;


--
-- TOC entry 2371 (class 0 OID 0)
-- Dependencies: 1771
-- Name: client_applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('client_applications_id_seq', 1, false);


--
-- TOC entry 1772 (class 1259 OID 16886)
-- Dependencies: 6
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
-- TOC entry 1773 (class 1259 OID 16889)
-- Dependencies: 1772 6
-- Name: countries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE countries_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2372 (class 0 OID 0)
-- Dependencies: 1773
-- Name: countries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE countries_id_seq OWNED BY countries.id;


--
-- TOC entry 2373 (class 0 OID 0)
-- Dependencies: 1773
-- Name: countries_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('countries_id_seq', 246, true);


--
-- TOC entry 1774 (class 1259 OID 16891)
-- Dependencies: 2102 2103 6
-- Name: current_node_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_node_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1775 (class 1259 OID 16899)
-- Dependencies: 6
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
-- TOC entry 1776 (class 1259 OID 16902)
-- Dependencies: 1775 6
-- Name: current_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE current_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2374 (class 0 OID 0)
-- Dependencies: 1776
-- Name: current_nodes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE current_nodes_id_seq OWNED BY current_nodes.id;


--
-- TOC entry 2375 (class 0 OID 0)
-- Dependencies: 1776
-- Name: current_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('current_nodes_id_seq', 1, false);


--
-- TOC entry 1777 (class 1259 OID 16904)
-- Dependencies: 2105 429 6
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
-- TOC entry 1778 (class 1259 OID 16908)
-- Dependencies: 2106 2107 6
-- Name: current_relation_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_relation_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1779 (class 1259 OID 16916)
-- Dependencies: 6
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
-- TOC entry 1780 (class 1259 OID 16919)
-- Dependencies: 1779 6
-- Name: current_relations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE current_relations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2376 (class 0 OID 0)
-- Dependencies: 1780
-- Name: current_relations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE current_relations_id_seq OWNED BY current_relations.id;


--
-- TOC entry 2377 (class 0 OID 0)
-- Dependencies: 1780
-- Name: current_relations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('current_relations_id_seq', 1, false);


--
-- TOC entry 1781 (class 1259 OID 16921)
-- Dependencies: 6
-- Name: current_way_nodes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_way_nodes (
    id bigint NOT NULL,
    node_id bigint NOT NULL,
    sequence_id bigint NOT NULL
);


--
-- TOC entry 1782 (class 1259 OID 16924)
-- Dependencies: 2109 2110 6
-- Name: current_way_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE current_way_tags (
    id bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1783 (class 1259 OID 16932)
-- Dependencies: 6
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
-- TOC entry 1784 (class 1259 OID 16935)
-- Dependencies: 1783 6
-- Name: current_ways_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE current_ways_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2378 (class 0 OID 0)
-- Dependencies: 1784
-- Name: current_ways_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE current_ways_id_seq OWNED BY current_ways.id;


--
-- TOC entry 2379 (class 0 OID 0)
-- Dependencies: 1784
-- Name: current_ways_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('current_ways_id_seq', 1, false);


--
-- TOC entry 1785 (class 1259 OID 16937)
-- Dependencies: 2112 6
-- Name: diary_comments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE diary_comments (
    id bigint NOT NULL,
    diary_entry_id bigint NOT NULL,
    user_id bigint NOT NULL,
    body text NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    visible boolean DEFAULT true NOT NULL
);


--
-- TOC entry 1786 (class 1259 OID 16944)
-- Dependencies: 6 1785
-- Name: diary_comments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE diary_comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2380 (class 0 OID 0)
-- Dependencies: 1786
-- Name: diary_comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE diary_comments_id_seq OWNED BY diary_comments.id;


--
-- TOC entry 2381 (class 0 OID 0)
-- Dependencies: 1786
-- Name: diary_comments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('diary_comments_id_seq', 1, false);


--
-- TOC entry 1787 (class 1259 OID 16946)
-- Dependencies: 2114 6
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
    language_code character varying(255),
    visible boolean DEFAULT true NOT NULL
);


--
-- TOC entry 1788 (class 1259 OID 16953)
-- Dependencies: 6 1787
-- Name: diary_entries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE diary_entries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2382 (class 0 OID 0)
-- Dependencies: 1788
-- Name: diary_entries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE diary_entries_id_seq OWNED BY diary_entries.id;


--
-- TOC entry 2383 (class 0 OID 0)
-- Dependencies: 1788
-- Name: diary_entries_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('diary_entries_id_seq', 1, false);


--
-- TOC entry 1789 (class 1259 OID 16955)
-- Dependencies: 6
-- Name: friends; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE friends (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    friend_user_id bigint NOT NULL
);


--
-- TOC entry 1790 (class 1259 OID 16958)
-- Dependencies: 6 1789
-- Name: friends_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE friends_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2384 (class 0 OID 0)
-- Dependencies: 1790
-- Name: friends_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE friends_id_seq OWNED BY friends.id;


--
-- TOC entry 2385 (class 0 OID 0)
-- Dependencies: 1790
-- Name: friends_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('friends_id_seq', 1, false);


--
-- TOC entry 1791 (class 1259 OID 16960)
-- Dependencies: 6
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
-- TOC entry 1792 (class 1259 OID 16963)
-- Dependencies: 2117 6
-- Name: gpx_file_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE gpx_file_tags (
    gpx_id bigint DEFAULT 0 NOT NULL,
    tag character varying(255) NOT NULL,
    id bigint NOT NULL
);


--
-- TOC entry 1793 (class 1259 OID 16967)
-- Dependencies: 1792 6
-- Name: gpx_file_tags_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE gpx_file_tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2386 (class 0 OID 0)
-- Dependencies: 1793
-- Name: gpx_file_tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE gpx_file_tags_id_seq OWNED BY gpx_file_tags.id;


--
-- TOC entry 2387 (class 0 OID 0)
-- Dependencies: 1793
-- Name: gpx_file_tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('gpx_file_tags_id_seq', 1, false);


--
-- TOC entry 1794 (class 1259 OID 16969)
-- Dependencies: 2119 2120 2121 2122 6 427
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
    description character varying(255) DEFAULT ''::character varying NOT NULL,
    inserted boolean NOT NULL,
    visibility gpx_visibility_enum DEFAULT 'public'::gpx_visibility_enum NOT NULL
);


--
-- TOC entry 1795 (class 1259 OID 16979)
-- Dependencies: 1794 6
-- Name: gpx_files_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE gpx_files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2388 (class 0 OID 0)
-- Dependencies: 1795
-- Name: gpx_files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE gpx_files_id_seq OWNED BY gpx_files.id;


--
-- TOC entry 2389 (class 0 OID 0)
-- Dependencies: 1795
-- Name: gpx_files_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('gpx_files_id_seq', 1, false);


--
-- TOC entry 1796 (class 1259 OID 16981)
-- Dependencies: 6
-- Name: languages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE languages (
    code character varying(255) NOT NULL,
    english_name character varying(255) NOT NULL,
    native_name character varying(255)
);


--
-- TOC entry 1797 (class 1259 OID 16987)
-- Dependencies: 2124 2125 2126 6
-- Name: messages; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE messages (
    id bigint NOT NULL,
    from_user_id bigint NOT NULL,
    title character varying(255) NOT NULL,
    body text NOT NULL,
    sent_on timestamp without time zone NOT NULL,
    message_read boolean DEFAULT false NOT NULL,
    to_user_id bigint NOT NULL,
    to_user_visible boolean DEFAULT true NOT NULL,
    from_user_visible boolean DEFAULT true NOT NULL
);


--
-- TOC entry 1798 (class 1259 OID 16996)
-- Dependencies: 6 1797
-- Name: messages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2390 (class 0 OID 0)
-- Dependencies: 1798
-- Name: messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE messages_id_seq OWNED BY messages.id;


--
-- TOC entry 2391 (class 0 OID 0)
-- Dependencies: 1798
-- Name: messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('messages_id_seq', 1, false);


--
-- TOC entry 1799 (class 1259 OID 16998)
-- Dependencies: 2128 2129 6
-- Name: node_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE node_tags (
    id bigint NOT NULL,
    version bigint NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 1800 (class 1259 OID 17006)
-- Dependencies: 6
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
-- TOC entry 1801 (class 1259 OID 17009)
-- Dependencies: 6
-- Name: oauth_nonces; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oauth_nonces (
    id integer NOT NULL,
    nonce character varying(255),
    "timestamp" integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


--
-- TOC entry 1802 (class 1259 OID 17012)
-- Dependencies: 6 1801
-- Name: oauth_nonces_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE oauth_nonces_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2392 (class 0 OID 0)
-- Dependencies: 1802
-- Name: oauth_nonces_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE oauth_nonces_id_seq OWNED BY oauth_nonces.id;


--
-- TOC entry 2393 (class 0 OID 0)
-- Dependencies: 1802
-- Name: oauth_nonces_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('oauth_nonces_id_seq', 1, false);


--
-- TOC entry 1803 (class 1259 OID 17014)
-- Dependencies: 2131 2132 2133 2134 2135 2136 6
-- Name: oauth_tokens; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oauth_tokens (
    id integer NOT NULL,
    user_id integer,
    type character varying(20),
    client_application_id integer,
    token character varying(50),
    secret character varying(50),
    authorized_at timestamp without time zone,
    invalidated_at timestamp without time zone,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    allow_read_prefs boolean DEFAULT false NOT NULL,
    allow_write_prefs boolean DEFAULT false NOT NULL,
    allow_write_diary boolean DEFAULT false NOT NULL,
    allow_write_api boolean DEFAULT false NOT NULL,
    allow_read_gpx boolean DEFAULT false NOT NULL,
    allow_write_gpx boolean DEFAULT false NOT NULL
);


--
-- TOC entry 1804 (class 1259 OID 17023)
-- Dependencies: 6 1803
-- Name: oauth_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE oauth_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2394 (class 0 OID 0)
-- Dependencies: 1804
-- Name: oauth_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE oauth_tokens_id_seq OWNED BY oauth_tokens.id;


--
-- TOC entry 2395 (class 0 OID 0)
-- Dependencies: 1804
-- Name: oauth_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('oauth_tokens_id_seq', 1, false);


--
-- TOC entry 1805 (class 1259 OID 17025)
-- Dependencies: 2138 2139 2140 429 6
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
-- TOC entry 1806 (class 1259 OID 17031)
-- Dependencies: 2141 2142 2143 6
-- Name: relation_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE relation_tags (
    id bigint DEFAULT 0 NOT NULL,
    k character varying(255) DEFAULT ''::character varying NOT NULL,
    v character varying(255) DEFAULT ''::character varying NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1807 (class 1259 OID 17040)
-- Dependencies: 2144 2145 6
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
-- TOC entry 1808 (class 1259 OID 17045)
-- Dependencies: 6
-- Name: schema_migrations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE schema_migrations (
    version character varying(255) NOT NULL
);


--
-- TOC entry 1809 (class 1259 OID 17048)
-- Dependencies: 6
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
-- TOC entry 1810 (class 1259 OID 17054)
-- Dependencies: 6 1809
-- Name: sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2396 (class 0 OID 0)
-- Dependencies: 1810
-- Name: sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE sessions_id_seq OWNED BY sessions.id;


--
-- TOC entry 2397 (class 0 OID 0)
-- Dependencies: 1810
-- Name: sessions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('sessions_id_seq', 1, false);


--
-- TOC entry 1811 (class 1259 OID 17056)
-- Dependencies: 2147 6
-- Name: user_blocks; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_blocks (
    id integer NOT NULL,
    user_id bigint NOT NULL,
    creator_id bigint NOT NULL,
    reason text NOT NULL,
    ends_at timestamp without time zone NOT NULL,
    needs_view boolean DEFAULT false NOT NULL,
    revoker_id bigint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone
);


--
-- TOC entry 1812 (class 1259 OID 17063)
-- Dependencies: 6 1811
-- Name: user_blocks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_blocks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2398 (class 0 OID 0)
-- Dependencies: 1812
-- Name: user_blocks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_blocks_id_seq OWNED BY user_blocks.id;


--
-- TOC entry 2399 (class 0 OID 0)
-- Dependencies: 1812
-- Name: user_blocks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_blocks_id_seq', 1, false);


--
-- TOC entry 1813 (class 1259 OID 17065)
-- Dependencies: 6
-- Name: user_preferences; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_preferences (
    user_id bigint NOT NULL,
    k character varying(255) NOT NULL,
    v character varying(255) NOT NULL
);


--
-- TOC entry 1814 (class 1259 OID 17071)
-- Dependencies: 6 431
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_roles (
    id integer NOT NULL,
    user_id bigint NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    role user_role_enum NOT NULL,
    granter_id bigint NOT NULL
);


--
-- TOC entry 1815 (class 1259 OID 17074)
-- Dependencies: 6 1814
-- Name: user_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2400 (class 0 OID 0)
-- Dependencies: 1815
-- Name: user_roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_roles_id_seq OWNED BY user_roles.id;


--
-- TOC entry 2401 (class 0 OID 0)
-- Dependencies: 1815
-- Name: user_roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_roles_id_seq', 1, false);


--
-- TOC entry 1816 (class 1259 OID 17076)
-- Dependencies: 6
-- Name: user_tokens; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE user_tokens (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token character varying(255) NOT NULL,
    expiry timestamp without time zone NOT NULL,
    referer text
);


--
-- TOC entry 1817 (class 1259 OID 17082)
-- Dependencies: 6 1816
-- Name: user_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2402 (class 0 OID 0)
-- Dependencies: 1817
-- Name: user_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_tokens_id_seq OWNED BY user_tokens.id;


--
-- TOC entry 2403 (class 0 OID 0)
-- Dependencies: 1817
-- Name: user_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user_tokens_id_seq', 1, false);


--
-- TOC entry 1818 (class 1259 OID 17084)
-- Dependencies: 2151 2152 2153 2154 2155 2156 2158 2159 6 548
-- Name: users; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE users (
    email character varying(255) NOT NULL,
    id bigint NOT NULL,
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
    email_valid boolean DEFAULT false NOT NULL,
    new_email character varying(255),
    creation_ip character varying(255),
    languages character varying(255),
    status user_status_enum DEFAULT 'pending'::user_status_enum NOT NULL,
    terms_agreed timestamp without time zone,
    consider_pd boolean DEFAULT false NOT NULL
);


--
-- TOC entry 1819 (class 1259 OID 17098)
-- Dependencies: 1818 6
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- TOC entry 2404 (class 0 OID 0)
-- Dependencies: 1819
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- TOC entry 2405 (class 0 OID 0)
-- Dependencies: 1819
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('users_id_seq', 1, false);


--
-- TOC entry 1820 (class 1259 OID 17100)
-- Dependencies: 6
-- Name: way_nodes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE way_nodes (
    id bigint NOT NULL,
    node_id bigint NOT NULL,
    version bigint NOT NULL,
    sequence_id bigint NOT NULL
);


--
-- TOC entry 1821 (class 1259 OID 17103)
-- Dependencies: 2160 6
-- Name: way_tags; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE way_tags (
    id bigint DEFAULT 0 NOT NULL,
    k character varying(255) NOT NULL,
    v character varying(255) NOT NULL,
    version bigint NOT NULL
);


--
-- TOC entry 1822 (class 1259 OID 17110)
-- Dependencies: 2161 2162 6
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
-- TOC entry 2089 (class 2604 OID 17115)
-- Dependencies: 1766 1765
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE acls ALTER COLUMN id SET DEFAULT nextval('acls_id_seq'::regclass);


--
-- TOC entry 2093 (class 2604 OID 17116)
-- Dependencies: 1769 1768
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE changesets ALTER COLUMN id SET DEFAULT nextval('changesets_id_seq'::regclass);


--
-- TOC entry 2100 (class 2604 OID 17117)
-- Dependencies: 1771 1770
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE client_applications ALTER COLUMN id SET DEFAULT nextval('client_applications_id_seq'::regclass);


--
-- TOC entry 2101 (class 2604 OID 17118)
-- Dependencies: 1773 1772
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE countries ALTER COLUMN id SET DEFAULT nextval('countries_id_seq'::regclass);


--
-- TOC entry 2104 (class 2604 OID 17119)
-- Dependencies: 1776 1775
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE current_nodes ALTER COLUMN id SET DEFAULT nextval('current_nodes_id_seq'::regclass);


--
-- TOC entry 2108 (class 2604 OID 17120)
-- Dependencies: 1780 1779
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE current_relations ALTER COLUMN id SET DEFAULT nextval('current_relations_id_seq'::regclass);


--
-- TOC entry 2111 (class 2604 OID 17121)
-- Dependencies: 1784 1783
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE current_ways ALTER COLUMN id SET DEFAULT nextval('current_ways_id_seq'::regclass);


--
-- TOC entry 2113 (class 2604 OID 17122)
-- Dependencies: 1786 1785
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE diary_comments ALTER COLUMN id SET DEFAULT nextval('diary_comments_id_seq'::regclass);


--
-- TOC entry 2115 (class 2604 OID 17123)
-- Dependencies: 1788 1787
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE diary_entries ALTER COLUMN id SET DEFAULT nextval('diary_entries_id_seq'::regclass);


--
-- TOC entry 2116 (class 2604 OID 17124)
-- Dependencies: 1790 1789
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE friends ALTER COLUMN id SET DEFAULT nextval('friends_id_seq'::regclass);


--
-- TOC entry 2118 (class 2604 OID 17125)
-- Dependencies: 1793 1792
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE gpx_file_tags ALTER COLUMN id SET DEFAULT nextval('gpx_file_tags_id_seq'::regclass);


--
-- TOC entry 2123 (class 2604 OID 17126)
-- Dependencies: 1795 1794
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE gpx_files ALTER COLUMN id SET DEFAULT nextval('gpx_files_id_seq'::regclass);


--
-- TOC entry 2127 (class 2604 OID 17127)
-- Dependencies: 1798 1797
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE messages ALTER COLUMN id SET DEFAULT nextval('messages_id_seq'::regclass);


--
-- TOC entry 2130 (class 2604 OID 17128)
-- Dependencies: 1802 1801
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE oauth_nonces ALTER COLUMN id SET DEFAULT nextval('oauth_nonces_id_seq'::regclass);


--
-- TOC entry 2137 (class 2604 OID 17129)
-- Dependencies: 1804 1803
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE oauth_tokens ALTER COLUMN id SET DEFAULT nextval('oauth_tokens_id_seq'::regclass);


--
-- TOC entry 2146 (class 2604 OID 17130)
-- Dependencies: 1810 1809
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE sessions ALTER COLUMN id SET DEFAULT nextval('sessions_id_seq'::regclass);


--
-- TOC entry 2148 (class 2604 OID 17131)
-- Dependencies: 1812 1811
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE user_blocks ALTER COLUMN id SET DEFAULT nextval('user_blocks_id_seq'::regclass);


--
-- TOC entry 2149 (class 2604 OID 17132)
-- Dependencies: 1815 1814
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE user_roles ALTER COLUMN id SET DEFAULT nextval('user_roles_id_seq'::regclass);


--
-- TOC entry 2150 (class 2604 OID 17133)
-- Dependencies: 1817 1816
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE user_tokens ALTER COLUMN id SET DEFAULT nextval('user_tokens_id_seq'::regclass);


--
-- TOC entry 2157 (class 2604 OID 17134)
-- Dependencies: 1819 1818
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- TOC entry 2323 (class 0 OID 16850)
-- Dependencies: 1765
-- Data for Name: acls; Type: TABLE DATA; Schema: public; Owner: -
--

COPY acls (id, address, netmask, k, v) FROM stdin;
\.


--
-- TOC entry 2324 (class 0 OID 16858)
-- Dependencies: 1767
-- Data for Name: changeset_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY changeset_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2325 (class 0 OID 16866)
-- Dependencies: 1768
-- Data for Name: changesets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY changesets (id, user_id, created_at, min_lat, max_lat, min_lon, max_lon, closed_at, num_changes) FROM stdin;
\.


--
-- TOC entry 2326 (class 0 OID 16872)
-- Dependencies: 1770
-- Data for Name: client_applications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY client_applications (id, name, url, support_url, callback_url, key, secret, user_id, created_at, updated_at, allow_read_prefs, allow_write_prefs, allow_write_diary, allow_write_api, allow_read_gpx, allow_write_gpx) FROM stdin;
\.


--
-- TOC entry 2327 (class 0 OID 16886)
-- Dependencies: 1772
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
-- TOC entry 2328 (class 0 OID 16891)
-- Dependencies: 1774
-- Data for Name: current_node_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_node_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2329 (class 0 OID 16899)
-- Dependencies: 1775
-- Data for Name: current_nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_nodes (id, latitude, longitude, changeset_id, visible, "timestamp", tile, version) FROM stdin;
\.


--
-- TOC entry 2330 (class 0 OID 16904)
-- Dependencies: 1777
-- Data for Name: current_relation_members; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_relation_members (id, member_id, member_role, member_type, sequence_id) FROM stdin;
\.


--
-- TOC entry 2331 (class 0 OID 16908)
-- Dependencies: 1778
-- Data for Name: current_relation_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_relation_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2332 (class 0 OID 16916)
-- Dependencies: 1779
-- Data for Name: current_relations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_relations (id, changeset_id, "timestamp", visible, version) FROM stdin;
\.


--
-- TOC entry 2333 (class 0 OID 16921)
-- Dependencies: 1781
-- Data for Name: current_way_nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_way_nodes (id, node_id, sequence_id) FROM stdin;
\.


--
-- TOC entry 2334 (class 0 OID 16924)
-- Dependencies: 1782
-- Data for Name: current_way_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_way_tags (id, k, v) FROM stdin;
\.


--
-- TOC entry 2335 (class 0 OID 16932)
-- Dependencies: 1783
-- Data for Name: current_ways; Type: TABLE DATA; Schema: public; Owner: -
--

COPY current_ways (id, changeset_id, "timestamp", visible, version) FROM stdin;
\.


--
-- TOC entry 2336 (class 0 OID 16937)
-- Dependencies: 1785
-- Data for Name: diary_comments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY diary_comments (id, diary_entry_id, user_id, body, created_at, updated_at, visible) FROM stdin;
\.


--
-- TOC entry 2337 (class 0 OID 16946)
-- Dependencies: 1787
-- Data for Name: diary_entries; Type: TABLE DATA; Schema: public; Owner: -
--

COPY diary_entries (id, user_id, title, body, created_at, updated_at, latitude, longitude, language_code, visible) FROM stdin;
\.


--
-- TOC entry 2338 (class 0 OID 16955)
-- Dependencies: 1789
-- Data for Name: friends; Type: TABLE DATA; Schema: public; Owner: -
--

COPY friends (id, user_id, friend_user_id) FROM stdin;
\.


--
-- TOC entry 2339 (class 0 OID 16960)
-- Dependencies: 1791
-- Data for Name: gps_points; Type: TABLE DATA; Schema: public; Owner: -
--

COPY gps_points (altitude, trackid, latitude, longitude, gpx_id, "timestamp", tile) FROM stdin;
\.


--
-- TOC entry 2340 (class 0 OID 16963)
-- Dependencies: 1792
-- Data for Name: gpx_file_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY gpx_file_tags (gpx_id, tag, id) FROM stdin;
\.


--
-- TOC entry 2341 (class 0 OID 16969)
-- Dependencies: 1794
-- Data for Name: gpx_files; Type: TABLE DATA; Schema: public; Owner: -
--

COPY gpx_files (id, user_id, visible, name, size, latitude, longitude, "timestamp", description, inserted, visibility) FROM stdin;
\.


--
-- TOC entry 2342 (class 0 OID 16981)
-- Dependencies: 1796
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
-- TOC entry 2343 (class 0 OID 16987)
-- Dependencies: 1797
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: -
--

COPY messages (id, from_user_id, title, body, sent_on, message_read, to_user_id, to_user_visible, from_user_visible) FROM stdin;
\.


--
-- TOC entry 2344 (class 0 OID 16998)
-- Dependencies: 1799
-- Data for Name: node_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY node_tags (id, version, k, v) FROM stdin;
\.


--
-- TOC entry 2345 (class 0 OID 17006)
-- Dependencies: 1800
-- Data for Name: nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY nodes (id, latitude, longitude, changeset_id, visible, "timestamp", tile, version) FROM stdin;
\.


--
-- TOC entry 2346 (class 0 OID 17009)
-- Dependencies: 1801
-- Data for Name: oauth_nonces; Type: TABLE DATA; Schema: public; Owner: -
--

COPY oauth_nonces (id, nonce, "timestamp", created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 2347 (class 0 OID 17014)
-- Dependencies: 1803
-- Data for Name: oauth_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY oauth_tokens (id, user_id, type, client_application_id, token, secret, authorized_at, invalidated_at, created_at, updated_at, allow_read_prefs, allow_write_prefs, allow_write_diary, allow_write_api, allow_read_gpx, allow_write_gpx) FROM stdin;
\.


--
-- TOC entry 2348 (class 0 OID 17025)
-- Dependencies: 1805
-- Data for Name: relation_members; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relation_members (id, member_id, member_role, version, member_type, sequence_id) FROM stdin;
\.


--
-- TOC entry 2349 (class 0 OID 17031)
-- Dependencies: 1806
-- Data for Name: relation_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relation_tags (id, k, v, version) FROM stdin;
\.


--
-- TOC entry 2350 (class 0 OID 17040)
-- Dependencies: 1807
-- Data for Name: relations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY relations (id, changeset_id, "timestamp", version, visible) FROM stdin;
\.


--
-- TOC entry 2351 (class 0 OID 17045)
-- Dependencies: 1808
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
38
36
37
39
40
41
42
43
44
45
46
47
48
49
50
51
52
20100513171259
\.


--
-- TOC entry 2352 (class 0 OID 17048)
-- Dependencies: 1809
-- Data for Name: sessions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sessions (id, session_id, data, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 2353 (class 0 OID 17056)
-- Dependencies: 1811
-- Data for Name: user_blocks; Type: TABLE DATA; Schema: public; Owner: -
--

COPY user_blocks (id, user_id, creator_id, reason, ends_at, needs_view, revoker_id, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 2354 (class 0 OID 17065)
-- Dependencies: 1813
-- Data for Name: user_preferences; Type: TABLE DATA; Schema: public; Owner: -
--

COPY user_preferences (user_id, k, v) FROM stdin;
\.


--
-- TOC entry 2355 (class 0 OID 17071)
-- Dependencies: 1814
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: -
--

COPY user_roles (id, user_id, created_at, updated_at, role, granter_id) FROM stdin;
\.


--
-- TOC entry 2356 (class 0 OID 17076)
-- Dependencies: 1816
-- Data for Name: user_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY user_tokens (id, user_id, token, expiry, referer) FROM stdin;
\.


--
-- TOC entry 2357 (class 0 OID 17084)
-- Dependencies: 1818
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY users (email, id, pass_crypt, creation_time, display_name, data_public, description, home_lat, home_lon, home_zoom, nearby, pass_salt, image, email_valid, new_email, creation_ip, languages, status, terms_agreed, consider_pd) FROM stdin;
\.


--
-- TOC entry 2358 (class 0 OID 17100)
-- Dependencies: 1820
-- Data for Name: way_nodes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY way_nodes (id, node_id, version, sequence_id) FROM stdin;
\.


--
-- TOC entry 2359 (class 0 OID 17103)
-- Dependencies: 1821
-- Data for Name: way_tags; Type: TABLE DATA; Schema: public; Owner: -
--

COPY way_tags (id, k, v, version) FROM stdin;
\.


--
-- TOC entry 2360 (class 0 OID 17110)
-- Dependencies: 1822
-- Data for Name: ways; Type: TABLE DATA; Schema: public; Owner: -
--

COPY ways (id, changeset_id, "timestamp", version, visible) FROM stdin;
\.


--
-- TOC entry 2165 (class 2606 OID 17136)
-- Dependencies: 1765 1765
-- Name: acls_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY acls
    ADD CONSTRAINT acls_pkey PRIMARY KEY (id);


--
-- TOC entry 2171 (class 2606 OID 17138)
-- Dependencies: 1768 1768
-- Name: changesets_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY changesets
    ADD CONSTRAINT changesets_pkey PRIMARY KEY (id);


--
-- TOC entry 2175 (class 2606 OID 17140)
-- Dependencies: 1770 1770
-- Name: client_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY client_applications
    ADD CONSTRAINT client_applications_pkey PRIMARY KEY (id);


--
-- TOC entry 2179 (class 2606 OID 17142)
-- Dependencies: 1772 1772
-- Name: countries_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);


--
-- TOC entry 2181 (class 2606 OID 17144)
-- Dependencies: 1774 1774 1774
-- Name: current_node_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_node_tags
    ADD CONSTRAINT current_node_tags_pkey PRIMARY KEY (id, k);


--
-- TOC entry 2183 (class 2606 OID 17146)
-- Dependencies: 1775 1775
-- Name: current_nodes_pkey1; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_nodes
    ADD CONSTRAINT current_nodes_pkey1 PRIMARY KEY (id);


--
-- TOC entry 2188 (class 2606 OID 17148)
-- Dependencies: 1777 1777 1777 1777 1777 1777
-- Name: current_relation_members_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_relation_members
    ADD CONSTRAINT current_relation_members_pkey PRIMARY KEY (id, member_type, member_id, member_role, sequence_id);


--
-- TOC entry 2190 (class 2606 OID 17150)
-- Dependencies: 1778 1778 1778
-- Name: current_relation_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_relation_tags
    ADD CONSTRAINT current_relation_tags_pkey PRIMARY KEY (id, k);


--
-- TOC entry 2192 (class 2606 OID 17152)
-- Dependencies: 1779 1779
-- Name: current_relations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_relations
    ADD CONSTRAINT current_relations_pkey PRIMARY KEY (id);


--
-- TOC entry 2196 (class 2606 OID 17154)
-- Dependencies: 1781 1781 1781
-- Name: current_way_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_way_nodes
    ADD CONSTRAINT current_way_nodes_pkey PRIMARY KEY (id, sequence_id);


--
-- TOC entry 2198 (class 2606 OID 17156)
-- Dependencies: 1782 1782 1782
-- Name: current_way_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_way_tags
    ADD CONSTRAINT current_way_tags_pkey PRIMARY KEY (id, k);


--
-- TOC entry 2200 (class 2606 OID 17158)
-- Dependencies: 1783 1783
-- Name: current_ways_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY current_ways
    ADD CONSTRAINT current_ways_pkey PRIMARY KEY (id);


--
-- TOC entry 2205 (class 2606 OID 17160)
-- Dependencies: 1785 1785
-- Name: diary_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY diary_comments
    ADD CONSTRAINT diary_comments_pkey PRIMARY KEY (id);


--
-- TOC entry 2207 (class 2606 OID 17162)
-- Dependencies: 1787 1787
-- Name: diary_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY diary_entries
    ADD CONSTRAINT diary_entries_pkey PRIMARY KEY (id);


--
-- TOC entry 2212 (class 2606 OID 17164)
-- Dependencies: 1789 1789
-- Name: friends_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY friends
    ADD CONSTRAINT friends_pkey PRIMARY KEY (id);


--
-- TOC entry 2219 (class 2606 OID 17166)
-- Dependencies: 1792 1792
-- Name: gpx_file_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY gpx_file_tags
    ADD CONSTRAINT gpx_file_tags_pkey PRIMARY KEY (id);


--
-- TOC entry 2222 (class 2606 OID 17168)
-- Dependencies: 1794 1794
-- Name: gpx_files_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY gpx_files
    ADD CONSTRAINT gpx_files_pkey PRIMARY KEY (id);


--
-- TOC entry 2227 (class 2606 OID 17170)
-- Dependencies: 1796 1796
-- Name: languages_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY languages
    ADD CONSTRAINT languages_pkey PRIMARY KEY (code);


--
-- TOC entry 2230 (class 2606 OID 17172)
-- Dependencies: 1797 1797
-- Name: messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- TOC entry 2233 (class 2606 OID 17174)
-- Dependencies: 1799 1799 1799 1799
-- Name: node_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY node_tags
    ADD CONSTRAINT node_tags_pkey PRIMARY KEY (id, version, k);


--
-- TOC entry 2236 (class 2606 OID 17176)
-- Dependencies: 1800 1800 1800
-- Name: nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT nodes_pkey PRIMARY KEY (id, version);


--
-- TOC entry 2241 (class 2606 OID 17178)
-- Dependencies: 1801 1801
-- Name: oauth_nonces_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oauth_nonces
    ADD CONSTRAINT oauth_nonces_pkey PRIMARY KEY (id);


--
-- TOC entry 2244 (class 2606 OID 17180)
-- Dependencies: 1803 1803
-- Name: oauth_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oauth_tokens
    ADD CONSTRAINT oauth_tokens_pkey PRIMARY KEY (id);


--
-- TOC entry 2247 (class 2606 OID 17182)
-- Dependencies: 1805 1805 1805 1805 1805 1805 1805
-- Name: relation_members_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relation_members
    ADD CONSTRAINT relation_members_pkey PRIMARY KEY (id, version, member_type, member_id, member_role, sequence_id);


--
-- TOC entry 2249 (class 2606 OID 17184)
-- Dependencies: 1806 1806 1806 1806
-- Name: relation_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relation_tags
    ADD CONSTRAINT relation_tags_pkey PRIMARY KEY (id, version, k);


--
-- TOC entry 2252 (class 2606 OID 17186)
-- Dependencies: 1807 1807 1807
-- Name: relations_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_pkey PRIMARY KEY (id, version);


--
-- TOC entry 2256 (class 2606 OID 17188)
-- Dependencies: 1809 1809
-- Name: sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (id);


--
-- TOC entry 2260 (class 2606 OID 17190)
-- Dependencies: 1811 1811
-- Name: user_blocks_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_blocks
    ADD CONSTRAINT user_blocks_pkey PRIMARY KEY (id);


--
-- TOC entry 2262 (class 2606 OID 17192)
-- Dependencies: 1813 1813 1813
-- Name: user_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_preferences
    ADD CONSTRAINT user_preferences_pkey PRIMARY KEY (user_id, k);


--
-- TOC entry 2265 (class 2606 OID 17194)
-- Dependencies: 1814 1814
-- Name: user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id);


--
-- TOC entry 2267 (class 2606 OID 17196)
-- Dependencies: 1816 1816
-- Name: user_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY user_tokens
    ADD CONSTRAINT user_tokens_pkey PRIMARY KEY (id);


--
-- TOC entry 2273 (class 2606 OID 17198)
-- Dependencies: 1818 1818
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 2276 (class 2606 OID 17200)
-- Dependencies: 1820 1820 1820 1820
-- Name: way_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT way_nodes_pkey PRIMARY KEY (id, version, sequence_id);


--
-- TOC entry 2278 (class 2606 OID 17202)
-- Dependencies: 1821 1821 1821 1821
-- Name: way_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY way_tags
    ADD CONSTRAINT way_tags_pkey PRIMARY KEY (id, version, k);


--
-- TOC entry 2281 (class 2606 OID 17204)
-- Dependencies: 1822 1822 1822
-- Name: ways_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT ways_pkey PRIMARY KEY (id, version);


--
-- TOC entry 2163 (class 1259 OID 17205)
-- Dependencies: 1765
-- Name: acls_k_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX acls_k_idx ON acls USING btree (k);


--
-- TOC entry 2166 (class 1259 OID 17206)
-- Dependencies: 1767
-- Name: changeset_tags_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changeset_tags_id_idx ON changeset_tags USING btree (id);


--
-- TOC entry 2167 (class 1259 OID 17207)
-- Dependencies: 1374 1374 1768 1374 1768 1374 1768 1768
-- Name: changesets_bbox_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_bbox_idx ON changesets USING gist (min_lat, max_lat, min_lon, max_lon);


--
-- TOC entry 2168 (class 1259 OID 17208)
-- Dependencies: 1768
-- Name: changesets_closed_at_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_closed_at_idx ON changesets USING btree (closed_at);


--
-- TOC entry 2169 (class 1259 OID 17209)
-- Dependencies: 1768
-- Name: changesets_created_at_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_created_at_idx ON changesets USING btree (created_at);


--
-- TOC entry 2172 (class 1259 OID 748508)
-- Dependencies: 1768 1768
-- Name: changesets_user_id_created_at_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_user_id_created_at_idx ON changesets USING btree (user_id, created_at);


--
-- TOC entry 2173 (class 1259 OID 115968)
-- Dependencies: 1768 1768
-- Name: changesets_user_id_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX changesets_user_id_id_idx ON changesets USING btree (user_id, id);


--
-- TOC entry 2177 (class 1259 OID 17211)
-- Dependencies: 1772
-- Name: countries_code_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX countries_code_idx ON countries USING btree (code);


--
-- TOC entry 2184 (class 1259 OID 17212)
-- Dependencies: 1775
-- Name: current_nodes_tile_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_nodes_tile_idx ON current_nodes USING btree (tile);


--
-- TOC entry 2185 (class 1259 OID 17213)
-- Dependencies: 1775
-- Name: current_nodes_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_nodes_timestamp_idx ON current_nodes USING btree ("timestamp");


--
-- TOC entry 2186 (class 1259 OID 17214)
-- Dependencies: 1777 1777
-- Name: current_relation_members_member_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_relation_members_member_idx ON current_relation_members USING btree (member_type, member_id);


--
-- TOC entry 2193 (class 1259 OID 17215)
-- Dependencies: 1779
-- Name: current_relations_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_relations_timestamp_idx ON current_relations USING btree ("timestamp");


--
-- TOC entry 2194 (class 1259 OID 17216)
-- Dependencies: 1781
-- Name: current_way_nodes_node_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_way_nodes_node_idx ON current_way_nodes USING btree (node_id);


--
-- TOC entry 2201 (class 1259 OID 17217)
-- Dependencies: 1783
-- Name: current_ways_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX current_ways_timestamp_idx ON current_ways USING btree ("timestamp");


--
-- TOC entry 2202 (class 1259 OID 685977)
-- Dependencies: 1785 1785
-- Name: diary_comment_user_id_created_at_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX diary_comment_user_id_created_at_index ON diary_comments USING btree (user_id, created_at);


--
-- TOC entry 2203 (class 1259 OID 17218)
-- Dependencies: 1785 1785
-- Name: diary_comments_entry_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX diary_comments_entry_id_idx ON diary_comments USING btree (diary_entry_id, id);


--
-- TOC entry 2208 (class 1259 OID 53818)
-- Dependencies: 1787
-- Name: diary_entry_created_at_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX diary_entry_created_at_index ON diary_entries USING btree (created_at);


--
-- TOC entry 2209 (class 1259 OID 53820)
-- Dependencies: 1787 1787
-- Name: diary_entry_language_code_created_at_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX diary_entry_language_code_created_at_index ON diary_entries USING btree (language_code, created_at);


--
-- TOC entry 2210 (class 1259 OID 53819)
-- Dependencies: 1787 1787
-- Name: diary_entry_user_id_created_at_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX diary_entry_user_id_created_at_index ON diary_entries USING btree (user_id, created_at);


--
-- TOC entry 2213 (class 1259 OID 17219)
-- Dependencies: 1789
-- Name: friends_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX friends_user_id_idx ON friends USING btree (user_id);


--
-- TOC entry 2217 (class 1259 OID 17220)
-- Dependencies: 1792
-- Name: gpx_file_tags_gpxid_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_file_tags_gpxid_idx ON gpx_file_tags USING btree (gpx_id);


--
-- TOC entry 2220 (class 1259 OID 17221)
-- Dependencies: 1792
-- Name: gpx_file_tags_tag_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_file_tags_tag_idx ON gpx_file_tags USING btree (tag);


--
-- TOC entry 2223 (class 1259 OID 17222)
-- Dependencies: 1794
-- Name: gpx_files_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_files_timestamp_idx ON gpx_files USING btree ("timestamp");


--
-- TOC entry 2224 (class 1259 OID 17223)
-- Dependencies: 1794
-- Name: gpx_files_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_files_user_id_idx ON gpx_files USING btree (user_id);


--
-- TOC entry 2225 (class 1259 OID 17224)
-- Dependencies: 1794 1794
-- Name: gpx_files_visible_visibility_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX gpx_files_visible_visibility_idx ON gpx_files USING btree (visible, visibility);


--
-- TOC entry 2176 (class 1259 OID 17225)
-- Dependencies: 1770
-- Name: index_client_applications_on_key; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX index_client_applications_on_key ON client_applications USING btree (key);


--
-- TOC entry 2239 (class 1259 OID 17226)
-- Dependencies: 1801 1801
-- Name: index_oauth_nonces_on_nonce_and_timestamp; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX index_oauth_nonces_on_nonce_and_timestamp ON oauth_nonces USING btree (nonce, "timestamp");


--
-- TOC entry 2242 (class 1259 OID 17227)
-- Dependencies: 1803
-- Name: index_oauth_tokens_on_token; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX index_oauth_tokens_on_token ON oauth_tokens USING btree (token);


--
-- TOC entry 2258 (class 1259 OID 17228)
-- Dependencies: 1811
-- Name: index_user_blocks_on_user_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_user_blocks_on_user_id ON user_blocks USING btree (user_id);


--
-- TOC entry 2228 (class 1259 OID 17229)
-- Dependencies: 1797
-- Name: messages_from_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX messages_from_user_id_idx ON messages USING btree (from_user_id);


--
-- TOC entry 2231 (class 1259 OID 17230)
-- Dependencies: 1797
-- Name: messages_to_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX messages_to_user_id_idx ON messages USING btree (to_user_id);


--
-- TOC entry 2234 (class 1259 OID 17231)
-- Dependencies: 1800
-- Name: nodes_changeset_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX nodes_changeset_id_idx ON nodes USING btree (changeset_id);


--
-- TOC entry 2237 (class 1259 OID 17232)
-- Dependencies: 1800
-- Name: nodes_tile_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX nodes_tile_idx ON nodes USING btree (tile);


--
-- TOC entry 2238 (class 1259 OID 17233)
-- Dependencies: 1800
-- Name: nodes_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX nodes_timestamp_idx ON nodes USING btree ("timestamp");


--
-- TOC entry 2215 (class 1259 OID 17234)
-- Dependencies: 1791
-- Name: points_gpxid_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX points_gpxid_idx ON gps_points USING btree (gpx_id);


--
-- TOC entry 2216 (class 1259 OID 17235)
-- Dependencies: 1791
-- Name: points_tile_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX points_tile_idx ON gps_points USING btree (tile);


--
-- TOC entry 2245 (class 1259 OID 17236)
-- Dependencies: 1805 1805
-- Name: relation_members_member_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX relation_members_member_idx ON relation_members USING btree (member_type, member_id);


--
-- TOC entry 2250 (class 1259 OID 17237)
-- Dependencies: 1807
-- Name: relations_changeset_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX relations_changeset_id_idx ON relations USING btree (changeset_id);


--
-- TOC entry 2253 (class 1259 OID 17238)
-- Dependencies: 1807
-- Name: relations_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX relations_timestamp_idx ON relations USING btree ("timestamp");


--
-- TOC entry 2257 (class 1259 OID 17239)
-- Dependencies: 1809
-- Name: sessions_session_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX sessions_session_id_idx ON sessions USING btree (session_id);


--
-- TOC entry 2254 (class 1259 OID 17240)
-- Dependencies: 1808
-- Name: unique_schema_migrations; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX unique_schema_migrations ON schema_migrations USING btree (version);


--
-- TOC entry 2214 (class 1259 OID 17241)
-- Dependencies: 1789
-- Name: user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_id_idx ON friends USING btree (friend_user_id);


--
-- TOC entry 2263 (class 1259 OID 17242)
-- Dependencies: 1814 1814
-- Name: user_roles_id_role_unique; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX user_roles_id_role_unique ON user_roles USING btree (user_id, role);


--
-- TOC entry 2268 (class 1259 OID 17243)
-- Dependencies: 1816
-- Name: user_tokens_token_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX user_tokens_token_idx ON user_tokens USING btree (token);


--
-- TOC entry 2269 (class 1259 OID 17244)
-- Dependencies: 1816
-- Name: user_tokens_user_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_tokens_user_id_idx ON user_tokens USING btree (user_id);


--
-- TOC entry 2270 (class 1259 OID 17245)
-- Dependencies: 1818
-- Name: users_display_name_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX users_display_name_idx ON users USING btree (display_name);


--
-- TOC entry 2271 (class 1259 OID 17246)
-- Dependencies: 1818
-- Name: users_email_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX users_email_idx ON users USING btree (email);


--
-- TOC entry 2274 (class 1259 OID 17247)
-- Dependencies: 1820
-- Name: way_nodes_node_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX way_nodes_node_idx ON way_nodes USING btree (node_id);


--
-- TOC entry 2279 (class 1259 OID 17248)
-- Dependencies: 1822
-- Name: ways_changeset_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ways_changeset_id_idx ON ways USING btree (changeset_id);


--
-- TOC entry 2282 (class 1259 OID 17249)
-- Dependencies: 1822
-- Name: ways_timestamp_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ways_timestamp_idx ON ways USING btree ("timestamp");


--
-- TOC entry 2283 (class 2606 OID 17250)
-- Dependencies: 2170 1767 1768
-- Name: changeset_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY changeset_tags
    ADD CONSTRAINT changeset_tags_id_fkey FOREIGN KEY (id) REFERENCES changesets(id);


--
-- TOC entry 2284 (class 2606 OID 17255)
-- Dependencies: 1818 1768 2272
-- Name: changesets_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY changesets
    ADD CONSTRAINT changesets_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2285 (class 2606 OID 17260)
-- Dependencies: 1818 2272 1770
-- Name: client_applications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY client_applications
    ADD CONSTRAINT client_applications_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2286 (class 2606 OID 17265)
-- Dependencies: 1774 1775 2182
-- Name: current_node_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_node_tags
    ADD CONSTRAINT current_node_tags_id_fkey FOREIGN KEY (id) REFERENCES current_nodes(id);


--
-- TOC entry 2287 (class 2606 OID 17270)
-- Dependencies: 1768 1775 2170
-- Name: current_nodes_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_nodes
    ADD CONSTRAINT current_nodes_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2288 (class 2606 OID 17275)
-- Dependencies: 2191 1779 1777
-- Name: current_relation_members_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_relation_members
    ADD CONSTRAINT current_relation_members_id_fkey FOREIGN KEY (id) REFERENCES current_relations(id);


--
-- TOC entry 2289 (class 2606 OID 17280)
-- Dependencies: 1779 1778 2191
-- Name: current_relation_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_relation_tags
    ADD CONSTRAINT current_relation_tags_id_fkey FOREIGN KEY (id) REFERENCES current_relations(id);


--
-- TOC entry 2290 (class 2606 OID 17285)
-- Dependencies: 2170 1779 1768
-- Name: current_relations_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_relations
    ADD CONSTRAINT current_relations_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2291 (class 2606 OID 17290)
-- Dependencies: 1781 1783 2199
-- Name: current_way_nodes_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_way_nodes
    ADD CONSTRAINT current_way_nodes_id_fkey FOREIGN KEY (id) REFERENCES current_ways(id);


--
-- TOC entry 2292 (class 2606 OID 17295)
-- Dependencies: 1781 2182 1775
-- Name: current_way_nodes_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_way_nodes
    ADD CONSTRAINT current_way_nodes_node_id_fkey FOREIGN KEY (node_id) REFERENCES current_nodes(id);


--
-- TOC entry 2293 (class 2606 OID 17300)
-- Dependencies: 1782 1783 2199
-- Name: current_way_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_way_tags
    ADD CONSTRAINT current_way_tags_id_fkey FOREIGN KEY (id) REFERENCES current_ways(id);


--
-- TOC entry 2294 (class 2606 OID 17305)
-- Dependencies: 2170 1783 1768
-- Name: current_ways_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY current_ways
    ADD CONSTRAINT current_ways_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2295 (class 2606 OID 17310)
-- Dependencies: 1787 2206 1785
-- Name: diary_comments_diary_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_comments
    ADD CONSTRAINT diary_comments_diary_entry_id_fkey FOREIGN KEY (diary_entry_id) REFERENCES diary_entries(id);


--
-- TOC entry 2296 (class 2606 OID 17315)
-- Dependencies: 1818 1785 2272
-- Name: diary_comments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_comments
    ADD CONSTRAINT diary_comments_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2297 (class 2606 OID 17320)
-- Dependencies: 1787 1796 2226
-- Name: diary_entries_language_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_entries
    ADD CONSTRAINT diary_entries_language_code_fkey FOREIGN KEY (language_code) REFERENCES languages(code);


--
-- TOC entry 2298 (class 2606 OID 17325)
-- Dependencies: 1787 2272 1818
-- Name: diary_entries_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY diary_entries
    ADD CONSTRAINT diary_entries_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2299 (class 2606 OID 17330)
-- Dependencies: 1818 1789 2272
-- Name: friends_friend_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY friends
    ADD CONSTRAINT friends_friend_user_id_fkey FOREIGN KEY (friend_user_id) REFERENCES users(id);


--
-- TOC entry 2300 (class 2606 OID 17335)
-- Dependencies: 1789 1818 2272
-- Name: friends_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY friends
    ADD CONSTRAINT friends_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2301 (class 2606 OID 17340)
-- Dependencies: 2221 1791 1794
-- Name: gps_points_gpx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gps_points
    ADD CONSTRAINT gps_points_gpx_id_fkey FOREIGN KEY (gpx_id) REFERENCES gpx_files(id);


--
-- TOC entry 2302 (class 2606 OID 17345)
-- Dependencies: 2221 1792 1794
-- Name: gpx_file_tags_gpx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gpx_file_tags
    ADD CONSTRAINT gpx_file_tags_gpx_id_fkey FOREIGN KEY (gpx_id) REFERENCES gpx_files(id);


--
-- TOC entry 2303 (class 2606 OID 17350)
-- Dependencies: 1818 2272 1794
-- Name: gpx_files_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gpx_files
    ADD CONSTRAINT gpx_files_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2304 (class 2606 OID 17355)
-- Dependencies: 2272 1797 1818
-- Name: messages_from_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT messages_from_user_id_fkey FOREIGN KEY (from_user_id) REFERENCES users(id);


--
-- TOC entry 2305 (class 2606 OID 17360)
-- Dependencies: 1818 2272 1797
-- Name: messages_to_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT messages_to_user_id_fkey FOREIGN KEY (to_user_id) REFERENCES users(id);


--
-- TOC entry 2306 (class 2606 OID 17365)
-- Dependencies: 1799 1800 2235 1799 1800
-- Name: node_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY node_tags
    ADD CONSTRAINT node_tags_id_fkey FOREIGN KEY (id, version) REFERENCES nodes(id, version);


--
-- TOC entry 2307 (class 2606 OID 17370)
-- Dependencies: 1768 2170 1800
-- Name: nodes_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY nodes
    ADD CONSTRAINT nodes_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2308 (class 2606 OID 17375)
-- Dependencies: 2174 1770 1803
-- Name: oauth_tokens_client_application_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY oauth_tokens
    ADD CONSTRAINT oauth_tokens_client_application_id_fkey FOREIGN KEY (client_application_id) REFERENCES client_applications(id);


--
-- TOC entry 2309 (class 2606 OID 17380)
-- Dependencies: 1803 1818 2272
-- Name: oauth_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY oauth_tokens
    ADD CONSTRAINT oauth_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2310 (class 2606 OID 17385)
-- Dependencies: 1805 1805 1807 2251 1807
-- Name: relation_members_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY relation_members
    ADD CONSTRAINT relation_members_id_fkey FOREIGN KEY (id, version) REFERENCES relations(id, version);


--
-- TOC entry 2311 (class 2606 OID 17390)
-- Dependencies: 1807 2251 1807 1806 1806
-- Name: relation_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY relation_tags
    ADD CONSTRAINT relation_tags_id_fkey FOREIGN KEY (id, version) REFERENCES relations(id, version);


--
-- TOC entry 2312 (class 2606 OID 17395)
-- Dependencies: 2170 1807 1768
-- Name: relations_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY relations
    ADD CONSTRAINT relations_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2313 (class 2606 OID 17400)
-- Dependencies: 1818 1811 2272
-- Name: user_blocks_moderator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_blocks
    ADD CONSTRAINT user_blocks_moderator_id_fkey FOREIGN KEY (creator_id) REFERENCES users(id);


--
-- TOC entry 2314 (class 2606 OID 17405)
-- Dependencies: 1818 1811 2272
-- Name: user_blocks_revoker_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_blocks
    ADD CONSTRAINT user_blocks_revoker_id_fkey FOREIGN KEY (revoker_id) REFERENCES users(id);


--
-- TOC entry 2315 (class 2606 OID 17410)
-- Dependencies: 2272 1811 1818
-- Name: user_blocks_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_blocks
    ADD CONSTRAINT user_blocks_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2316 (class 2606 OID 17415)
-- Dependencies: 2272 1813 1818
-- Name: user_preferences_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_preferences
    ADD CONSTRAINT user_preferences_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2317 (class 2606 OID 17420)
-- Dependencies: 1818 1814 2272
-- Name: user_roles_granter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_roles
    ADD CONSTRAINT user_roles_granter_id_fkey FOREIGN KEY (granter_id) REFERENCES users(id);


--
-- TOC entry 2318 (class 2606 OID 17425)
-- Dependencies: 1818 2272 1814
-- Name: user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2319 (class 2606 OID 17430)
-- Dependencies: 2272 1818 1816
-- Name: user_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_tokens
    ADD CONSTRAINT user_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2320 (class 2606 OID 17435)
-- Dependencies: 1822 2280 1820 1820 1822
-- Name: way_nodes_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY way_nodes
    ADD CONSTRAINT way_nodes_id_fkey FOREIGN KEY (id, version) REFERENCES ways(id, version);


--
-- TOC entry 2321 (class 2606 OID 17440)
-- Dependencies: 1821 1822 1822 2280 1821
-- Name: way_tags_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY way_tags
    ADD CONSTRAINT way_tags_id_fkey FOREIGN KEY (id, version) REFERENCES ways(id, version);


--
-- TOC entry 2322 (class 2606 OID 17445)
-- Dependencies: 2170 1768 1822
-- Name: ways_changeset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT ways_changeset_id_fkey FOREIGN KEY (changeset_id) REFERENCES changesets(id);


--
-- TOC entry 2365 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2010-06-24 22:44:25 BST

--
-- PostgreSQL database dump complete
--


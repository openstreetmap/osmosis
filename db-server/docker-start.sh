#!/bin/bash

DATADIR="/var/lib/pgsql/data"

# test if DATADIR has content
if [ ! "$(ls -A $DATADIR)" ]; then
	# Create the en_US.UTF-8 locale.  We need UTF-8 support in the database.
	localedef -v -c -i en_US -f UTF-8 en_US.UTF-8

	echo "Initializing Postgres Database at $DATADIR"
	su postgres sh -lc "initdb --encoding=UTF-8 --locale=en_US.UTF-8"

	su postgres sh -lc "postgres --single -jE" <<-EOSQL
		CREATE USER osm WITH SUPERUSER PASSWORD 'password';
	EOSQL

	# Allow the osm user to connect remotely with a password.
	echo "listen_addresses = '*'" >> "${DATADIR}/postgresql.conf"
	echo "host all osm 0.0.0.0/0 md5" >> "${DATADIR}/pg_hba.conf"

	# Create the pgsimple database owned by osm.
	su postgres sh -lc "postgres --single -jE" <<-EOSQL
		CREATE DATABASE pgosmsimp06_test OWNER osm;
	EOSQL

	# Create the pgsnapshot database owned by osm.
	su postgres sh -lc "postgres --single -jE" <<-EOSQL
		CREATE DATABASE pgosmsnap06_test OWNER osm;
	EOSQL

	# Create the apidb database owned by osm.
	su postgres sh -lc "postgres --single -jE" <<-EOSQL
		CREATE DATABASE api06_test OWNER osm;
	EOSQL

	# Start the database server temporarily while we configure the databases.
	su postgres sh -lc "pg_ctl -w start"

	# Configure the pgosmsimp06_test database as the OSM user.
	su postgres sh -lc "psql -U osm pgosmsimp06_test" <<-EOSQL
		CREATE EXTENSION postgis;
		\i /install/script/pgsimple_schema_0.6.sql
		\i /install/script/pgsimple_schema_0.6_action.sql
		\i /install/script/pgsimple_schema_0.6_bbox.sql
		\i /install/script/pgsimple_schema_0.6_linestring.sql
	EOSQL

	# Configure the pgosmsnap06_test database as the OSM user.
	su postgres sh -lc "psql -U osm pgosmsnap06_test" <<-EOSQL
		CREATE EXTENSION hstore;
		CREATE EXTENSION postgis;
		\i /install/script/pgsnapshot_schema_0.6.sql
		\i /install/script/pgsnapshot_schema_0.6_action.sql
		\i /install/script/pgsnapshot_schema_0.6_bbox.sql
		\i /install/script/pgsnapshot_schema_0.6_linestring.sql
	EOSQL

	# Configure the api06_test database as the OSM user.
	su postgres sh -lc "psql -U osm api06_test" <<-EOSQL
		\i /install/script/contrib/apidb_0.6.sql
		\i /install/script/contrib/apidb_0.6_osmosis_xid_indexing.sql
	EOSQL

	# Stop the database.
	su postgres sh -lc "pg_ctl -w stop"
fi

SHUTDOWN_COMMAND="echo \"Shutting down postgres\"; su postgres sh -lc \"pg_ctl -w stop\""
trap "${SHUTDOWN_COMMAND}" SIGTERM
trap "${SHUTDOWN_COMMAND}" SIGINT

# Start the database server.
su postgres sh -lc "pg_ctl -w start"

echo "Docker container startup complete"

# Wait for the server to exit.
while test -e "/var/lib/pgsql/data/postmaster.pid"; do
	sleep 0.5
done

#!/bin/bash
set -ex

 # Create 'openstreetmap' user
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" <<-EOSQL
    CREATE USER osm PASSWORD 'password';
    GRANT ALL PRIVILEGES ON DATABASE api06_test TO osm;
EOSQL

 # Create btree_gist extensions
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -c "CREATE EXTENSION btree_gist" api06_test

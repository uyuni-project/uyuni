#!/bin/bash

# Data directory empty? Init DB.
if [ -z "$(ls -A /var/lib/pgsql/data)" ]; then
    chown postgres:postgres /var/lib/pgsql/data

    su - postgres -c "pg_ctl initdb -o --encoding=UTF8 -o --locale=en_US.UTF-8"
    su - postgres -c "pg_ctl start"

    su - postgres -c "createdb $DB_NAME ; echo \"CREATE ROLE $DB_USER PASSWORD '$DB_PASSWORD' SUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;\" | psql"
    echo "listen_addresses = '*'" >> /var/lib/pgsql/data/postgresql.conf
    echo "host all  all 0.0.0.0/0   md5
    host all  all ::/0  md5
    " >> /var/lib/pgsql/data/pg_hba.conf

    su - postgres -c "pg_ctl stop"
fi

su postgres -c 'postgres -D /var/lib/pgsql/data'

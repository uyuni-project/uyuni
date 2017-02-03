#!/bin/bash
set -e

MANAGER_USER="spacewalk"
MANAGER_PASS="spacewalk"
MANAGER_DB_NAME="susemanager"

su - postgres -c "/usr/lib/postgresql-init start"

su - postgres -c "createdb $MANAGER_DB_NAME ; echo \"CREATE ROLE $MANAGER_USER PASSWORD '$MANAGER_PASS' SUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;\" | psql"
echo "listen_addresses = '*'" >> /var/lib/pgsql/data/postgresql.conf
echo "local $MANAGER_DB_NAME $MANAGER_USER md5
host $MANAGER_DB_NAME $MANAGER_USER 127.0.0.1/8 md5
host $MANAGER_DB_NAME $MANAGER_USER ::1/128 md5
" > /tmp/pg_hba.conf
cat /var/lib/pgsql/data/pg_hba.conf >> /tmp/pg_hba.conf
mv /var/lib/pgsql/data/pg_hba.conf /var/lib/pgsql/data/pg_hba.conf.bak
mv /tmp/pg_hba.conf /var/lib/pgsql/data/pg_hba.conf

# HACK: allow less than 400 connections
sed -i 's/        conn_lowest = 400/        conn_lowest = 100/' /usr/lib/python2.7/site-packages/smdba/postgresqlgate.py
cp /root/rhn.conf /etc/rhn/

smdba system-check autotuning --max_connections=100

#rm /etc/rhn/rhn.conf
su - postgres -c "/usr/lib/postgresql-init stop"

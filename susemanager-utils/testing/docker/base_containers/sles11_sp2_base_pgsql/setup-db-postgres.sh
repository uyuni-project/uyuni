#!/bin/bash

# Halt on error
set -e

if ! chkconfig -c postgresql ; then
  insserv postgresql
fi

rcpostgresql start
su - postgres -c "createdb susemanager ; echo \"CREATE ROLE spacewalk PASSWORD 'spacewalk' SUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;\" | psql"
# "createlang plpgsql $MANAGER_DB_NAME" not needed on SUSE. plpgsql is already enabled

# Update psql configuration
cp /var/lib/pgsql/data/pg_hba.conf /var/lib/pgsql/data/pg_hba.conf.bak
echo "local susemanager spacewalk md5
host susemanager spacewalk 127.0.0.1/8 md5
host susemanager spacewalk ::1/128 md5
" > /var/lib/pgsql/data/pg_hba.conf
cat /var/lib/pgsql/data/pg_hba.conf.bak >> /var/lib/pgsql/data/pg_hba.conf

if [ -x "/usr/bin/pgtune" ]; then
  mv /var/lib/pgsql/data/postgresql.conf /var/lib/pgsql/data/postgresql.conf.orig
  /usr/bin/pgtune -T Mixed -i /var/lib/pgsql/data/postgresql.conf.orig -o /var/lib/pgsql/data/postgresql.conf
fi

rcpostgresql stop

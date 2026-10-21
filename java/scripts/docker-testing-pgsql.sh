#!/bin/bash -x

set -e

TARGET="test"

while getopts "r" opts; do
  case "${opts}" in
    r) REAL_COBBLER="-Dtests.use.real.cobbler=true" ;;
  esac
done
shift $((OPTIND-1))

if [ -n "$1" ]; then
    TARGET="$1"
fi

cd /manager/susemanager-utils/testing/docker/scripts/
# Move Postgres database to tmpfs to speed initialization and testing up
if [ ! -z $PG_TMPFS_DIR ]; then
    trap "umount $PG_TMPFS_DIR" EXIT INT TERM
    ./docker-testing-pgsql-move-data-to-tmpfs.sh $PG_TMPFS_DIR
fi

# Database schema creation
./reset_pgsql_database.sh

# SUSE Multi-Linux Manager initialization
cp /root/rhn.conf /etc/rhn/rhn.conf

if [ -n "${REAL_COBBLER}" ]; then
    # Cobbler init and startup
    ./setup-cobbler-test.sh
fi

# Resolve libs and run tests
cd /manager/java
ant -f manager-build.xml ivy

cp buildconf/test/rhn.conf.postgresql-example buildconf/test/rhn.conf
ant -f manager-build.xml refresh-branding-jar $REAL_COBBLER $TARGET

# Postgres shutdown (avoid stale memory by shmget())
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl -D /var/lib/pgsql/data stop" ||:

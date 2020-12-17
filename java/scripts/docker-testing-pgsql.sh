#!/bin/bash

set -e

TARGET="test"
if [ -n "$1" ]; then
    TARGET=$1
fi

cd /manager/susemanager-utils/testing/docker/scripts/
# Move Postgres database to tmpfs to speed initialization and testing up
if [ ! -z $PG_TMPFS_DIR ]; then
    trap "umount $PG_TMPFS_DIR" EXIT INT TERM
    ./docker-testing-pgsql-move-data-to-tmpfs.sh $PG_TMPFS_DIR
fi

# Database schema creation
./reset_pgsql_database.sh

# SUSE Manager initialization
cp /root/rhn.conf /etc/rhn/rhn.conf

# Resolve libs and run tests
cd /manager/java
ant -f manager-build.xml ivy


cp buildconf/test/rhn.conf.postgresql-example buildconf/test/rhn.conf

# wait here
echo "rut this: ant -f manager-build.xml refresh-branding-jar test"

# todo: get rid of these (build new containers)
zypper rm -y ant-junit
zypper in -y ant-junit5
zypper ar https://download.opensuse.org/tumbleweed/repo/oss/ factory
zypper up -y ant-junit5

/bin/bash

ant -f manager-build.xml refresh-branding-jar $TARGET

# Postgres shutdown (avoid stale memory by shmget())
su - postgres -c "/usr/lib/postgresql12/bin/pg_ctl stop" ||:

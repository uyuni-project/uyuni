#!/bin/bash

set -e

cd /manager/susemanager-utils/testing/docker/scripts/

# Move Postgres database to tmpfs to speed initialization and testing up
if [ ! -z $PG_TMPFS_DIR ]; then
    trap "umount $PG_TMPFS_DIR" EXIT INT TERM
    ./docker-testing-pgsql-move-data-to-tmpfs.sh $PG_TMPFS_DIR
fi

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset pgsql database

echo $PATH
echo $PERLLIB

export SYSTEMD_NO_WRAP=1
#sysctl -w kernel.shmmax=18446744073709551615
rcpostgresql restart

# this copy the latest schema from the git into the system
./build-schema.sh

################################################
####### START COMMENT OUT
####### IF A FIXED DESTINATION IS WANTED
################################################

RPMVERSION=`rpm -q --qf "%{version}" --specfile /manager/schema/spacewalk/susemanager-schema.spec`
NEXTVERSION=`echo $RPMVERSION | awk '{ pre=post=$0; gsub("[0-9]+$","",pre); gsub(".*\\\\.","",post); print pre post+1; }'`

if [ -d /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-$RPMVERSION-to-susemanager-schema-$NEXTVERSION ]; then
    export SUMA_TEST_SCHEMA_VERSION=$NEXTVERSION

else
    export SUMA_TEST_SCHEMA_VERSION=$RPMVERSION
fi

###############################################
####### END
#export SUMA_TEST_SCHEMA_VERSION="3.0"
###############################################

# run the schema upgrade from git repo
if ! /manager/schema/spacewalk/spacewalk-schema-upgrade -y; then
    cat /var/log/spacewalk/schema-upgrade/schema-from-*.log
    rcpostgresql stop
    exit 1
fi
rcpostgresql stop
su - postgres -c '/usr/lib/postgresql94/bin/postgres -D /var/lib/pgsql/data'

